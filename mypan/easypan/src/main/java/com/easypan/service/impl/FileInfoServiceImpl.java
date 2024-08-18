package com.easypan.service.impl;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.*;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.utils.DateUtils;
import com.easypan.utils.ProcessUtils;
import com.easypan.utils.ScaleFilter;
import com.easypan.utils.StringTools;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @Description: 业务接口实现
 * @Author: false
 * @Date: 2024/06/18 11:25:12
 */
@Service("FileInfoMapper")
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper,FileInfo> implements FileInfoService {
    //    @Resource
    private static final Logger logger = LoggerFactory.getLogger(FileInfoServiceImpl.class);

    @Resource
    private FileInfoMapper fileinfoMapper;
    @Resource
    private InfoMapper userInfoMapper;

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private InfoMapper infoMapper;
    @Resource
    private AppConfig appConfig;
    @Resource
    @Lazy
    private FileInfoServiceImpl fileInfoService;

//    /**
//     * 根据条件查询列表
//     */
//    @Override
//    public List<FileInfo> findListByParam(FileInfoQuery query) {
//        return this.fileinfoMapper.selectList(query);
//    }

//    /**
//     * 分页查询
//     */
//    @Override
//    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
//        Integer count = this.findCountByParam(query);
//        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
//        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
//        query.setSimplePage(page);
//        List<FileInfo> list = this.findListByParam(query);
//        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
//        return result;
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
        UploadResultDto resultDto = new UploadResultDto();
        //如果是第一次上传，没有确定文件id，则新建一个文件id
        if (StringTools.isEmpty(fileId))
            fileId = StringTools.getRandomNumber(10);
        resultDto.setFileId(fileId);
        //创建当前系统时间
        Date curtime = new Date();
        //通过webdto中获取的用户id,从redis中获取用户使用的存储空间信息useSpaceDto
        UserSpaceDto userSpaceUse = redisComponent.getUserSpaceUse(webUserDto.getUserId());
        //判断是否为第一个分块
        File tempFile = null;
        boolean uploadSuccess = true;
        try {
            if (chunkIndex == 0) {
                //判断数据库中是否已经存在这个文件，存在则实现秒传
//                FileInfoQuery infoQuery = new FileInfoQuery();
//                infoQuery.setFileMd5(fileMd5);
//                infoQuery.setSimplePage(new SimplePage(0, 1));
//                infoQuery.setStatus(FileStatusEnums.USING.getStatus());
                LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(FileInfo::getFileMd5,fileMd5);
                queryWrapper.eq(FileInfo::getStatus,FileStatusEnums.USING.getStatus());
                List<FileInfo> dbFileList=list(queryWrapper);
//                List<FileInfo> dbFileList = fileinfoMapper.selectList(infoQuery);
                if (!dbFileList.isEmpty()) {
                    FileInfo dbFile = dbFileList.get(0);
                    //判断文件大小
                    if (dbFile.getFileSize() + userSpaceUse.getUseSpace() > userSpaceUse.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }
                    //文件文件赋值
                    dbFile.setCreateTime(curtime);
                    dbFile.setLastUpdateTime(curtime);
                    dbFile.setUserId(webUserDto.getUserId());
                    dbFile.setFileId(fileId);
                    dbFile.setStatus(FileStatusEnums.USING.getStatus());
                    dbFile.setFilePid(filePid);
                    dbFile.setFileMd5(fileMd5);
                    dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    //文件重命名
                    fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
                    dbFile.setFileName(fileName);
                    //文件插入数据库
                    fileinfoMapper.insert(dbFile);
                    //更新用户使用空间
                    updateUserSpace(webUserDto, dbFile.getFileSize());
                    return resultDto;
                }
            }
            //判断磁盘空间（从redis中获取
            //创建临时文件目录
            String tempFileFolder = appConfig.getProjectFolder() + Constants.TEMP_FILE_FOLDER + webUserDto.getUserId() + fileId;
            tempFile = new File(tempFileFolder);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            Long currentSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
            if (file.getSize() + currentSize > userSpaceUse.getTotalSpace())
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            //暂存临时目录
            File newFile = new File(tempFile.getPath() + "/" + chunkIndex);
            file.transferTo(newFile);
            //如果保存还没完成，返回前端继续保存信息
            if (chunkIndex < chunks - 1) {
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());
                return resultDto;
            }
            redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());
            //最后一个分片上传完成，记录数据库，异步合并分片
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
            String fileSuffix = StringTools.getFileSuffix(fileName);
            //真实文件名
            String realFileName = webUserDto.getUserId() + fileId + fileSuffix;
            //重命名文件
            FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            //插入数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(fileName);
            fileInfo.setFilePid(filePid);
            fileInfo.setUserId(webUserDto.getUserId());
            fileInfo.setFileCategory(fileTypeBySuffix.getCategory().getCategory());
            fileInfo.setFileId(fileId);
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFilePath(month + "/" + realFileName);
            fileInfo.setFileSize(redisComponent.getFileTempSize(webUserDto.getUserId(), fileId));
            fileInfo.setLastUpdateTime(new Date());
            fileInfo.setCreateTime(new Date());
            fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
            fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
            fileInfo.setRecoveryTime(new Date());
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            fileInfo.setFileType(fileTypeBySuffix.getType());
            this.fileinfoMapper.insert(fileInfo);
            Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
            updateUserSpace(webUserDto, totalSize);

            resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
            //事务提交后调用异步方法
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        fileInfoService.transferFile(fileInfo.getFileId(), webUserDto);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return resultDto;
        } catch (BusinessException e) {
            uploadSuccess = false;
            logger.error("文件上传失败", e);
            throw e;
        } catch (Exception e) {
            uploadSuccess = false;
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        } finally {
            //如果上传失败，清除临时目录
            if (tempFile != null && !uploadSuccess) {
                try {
                    FileUtils.deleteDirectory(tempFile);
                } catch (IOException e) {
                    logger.error("删除临时目录失败");
                }
            }
        }
    }

    @Override
    public FileInfo newFoloder(String filePid, String userId, String fileName) {
        //检查文件名是否已经存在
        checkFileName(filePid, userId, fileName);
        //插入文件夹文件
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(StringTools.getRandomNumber(10));
        fileInfo.setUserId(userId);
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setCreateTime(new Date());
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setLastUpdateTime(new Date());
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(fileName);
        fileinfoMapper.insert(fileInfo);
        return fileInfo;
    }

    @Override
    public FileInfo rename(String userId, String fileId, String fileName) {
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId,fileId);
        FileInfo fileInfo = fileinfoMapper.selectOne(queryWrapper);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        Date curTime = new Date();
        fileInfo.setLastUpdateTime(curTime);
        String suffix = StringTools.getFileSuffix(fileName);
        fileInfo.setFileName(fileName + suffix);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setRecoveryTime(curTime);
        fileinfoMapper.updateById(fileInfo);
        return fileInfo;
    }

    @Override
    public List<FileInfo> loadAllFolder(String userId, String filePid, String currentFildIds) {
//        FileInfoQuery fileInfoQuery = new FileInfoQuery();
//        fileInfoQuery.setFilePid(filePid);
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
//        fileInfoQuery.setStatus(FileStatusEnums.USING.getStatus());
//        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
//        fileInfoQuery.setOrderBy("create_time desc");
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFilePid,filePid);
        queryWrapper.eq(FileInfo::getUserId,userId);
        queryWrapper.eq(FileInfo::getFolderType,FileFolderTypeEnums.FOLDER.getType());
        queryWrapper.eq(FileInfo::getStatus,FileStatusEnums.USING.getStatus());
        queryWrapper.eq(FileInfo::getDelFlag,FileDelFlagEnums.USING.getFlag());
        queryWrapper.orderByDesc(FileInfo::getCreateTime);
        List<FileInfo> fileInfoList = list(queryWrapper);
        return fileInfoList;
    }

    @Override
    public void changeFileFolder(String userId, String fileIds, String filePid) {
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        //如果不是主目录。需要判断目标文件夹状态是否正常
        if (!filePid.equals(Constants.ZERO_STRING)) {
            queryWrapper.eq(FileInfo::getFileId,filePid);
            FileInfo fileInfo = fileinfoMapper.selectOne(queryWrapper);
            if (fileInfo == null || !fileInfo.getDelFlag().equals(FileDelFlagEnums.USING.getFlag())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        String[] fileArrays=fileIds.split(",");
        //判断是否有重名文件
        //查询目标文件夹下文件
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFilePid,filePid);
        queryWrapper.eq(FileInfo::getUserId,userId);
//        FileInfoQuery fileInfoQuery = new FileInfoQuery();
//        fileInfoQuery.setFilePid(filePid);
//        fileInfoQuery.setUserId(userId);
        List<FileInfo> fileInfoList = list(queryWrapper);
        //获取传入文件列表
//        fileInfoQuery = new FileInfoQuery();
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFileIdArray(fileArrays);
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        //TODO 这里是逐个查找添加 希望改成一起查找
//        List<FileInfo> selectFileList = findListByParam(fileInfoQuery);
        List<FileInfo> selectFileList=new LinkedList<>();
        for (String fileArray : fileArrays) {
            selectFileList.add(getFileInfo(queryWrapper,fileArray));
        }
        Map<String ,FileInfo> map=fileInfoList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(data1,data2)->data2));
        //将重命文件重命名
        for (FileInfo fileInfo : selectFileList) {
            if(map.containsKey(fileInfo.getFileName())){
                String newName = StringTools.rename(fileInfo.getFileName());
                fileInfo.setFileName(newName);
            }
            fileInfo.setFilePid(filePid);
            fileinfoMapper.updateById(fileInfo);
        }
    }
    public FileInfo getFileInfo(LambdaQueryWrapper<FileInfo> queryWrapper,String fileArray){
        queryWrapper.eq(FileInfo::getFileId,fileArray);
        return fileinfoMapper.selectOne(queryWrapper);
    }
    @Override
    public String createDownloadUrl(String userId, String fileId) {
        //查询文件
//        FileInfo fileInfo = fileInfoService.getInfoByFileId(fileId);
        //TODO 总是用lqw有点麻烦 有没有简单的方法呢
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId,fileId);
        FileInfo fileInfo = fileinfoMapper.selectOne(queryWrapper);
        if(fileInfo==null){
            throw new BusinessException("文件不存在，请检查下载文件");
        }
        //创建文件下载随机码
        String code = StringTools.getRandomNumber(Constants.Length_10);
        //保存到redis中
        DownloadFileDto fileDto = new DownloadFileDto();
        fileDto.setDownloadCode(code);
        fileDto.setFileId(fileId);
        fileDto.setFileName(fileInfo.getFileName());
        fileDto.setFilePath(fileInfo.getFilePath());
        redisComponent.saveDownloadCode(code,fileDto);
        //返回随机码
        return code;
    }
    @Override
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        String[] pathArrays=fileIds.split(",");
//        FileInfoQuery fileInfoQuery=new FileInfoQuery();
//        fileInfoQuery.setFileIdArray(pathArrays);
//        fileInfoQuery.setUserId(userId);
//        //用户选中的可能是一个文件夹，地址下可能有多个文件
//        List<FileInfo> fileInfoList=  fileInfoService.findListByParam(fileInfoQuery);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        List<FileInfo> fileInfoList=new LinkedList<>();
        for (String pathArray : pathArrays) {
            fileInfoList.add(getFileInfo(queryWrapper,pathArray));
        }
        if(fileInfoList.isEmpty())return;
        //递归枚举需要查询的文件和文件夹
        List<String> delFileList=new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            findAllDelFile(delFileList,userId,fileInfo.getFileId(),FileDelFlagEnums.USING.getFlag());
        }
        //将查询到的文件设为删除状态
        if(!fileInfoList.isEmpty()){
            FileInfo updateInfo=new FileInfo();
            updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
            this.fileinfoMapper.updateFileDelFlagBatch(updateInfo,userId,delFileList,null,FileDelFlagEnums.USING.getFlag());
        }
        //将选中的文件文件设为回收状态
        List<String>delFileIdList=Arrays.asList(pathArrays);
        FileInfo fileInfo=new FileInfo();
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        fileinfoMapper.updateFileDelFlagBatch(fileInfo,userId,null,delFileIdList,FileDelFlagEnums.USING.getFlag());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recoverFileBatch(String userId, String fileIds) {
        //分割所有要还原的文件
        String[] fileArrays=fileIds.split(",");
        //查询目标文件
//        FileInfoQuery fileInfoQuery=new FileInfoQuery();
//        fileInfoQuery.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFileIdArray(fileArrays);
//        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        //查询目标文件和子目录下的文件
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getDelFlag,FileDelFlagEnums.RECYCLE.getFlag());
        queryWrapper.eq(FileInfo::getUserId,userId);
        List<FileInfo> fileInfoList =new LinkedList<>();
        for (String fileArray : fileArrays) {
            fileInfoList.add(getFileInfo(queryWrapper,fileArray));
        }
        List<String> recoverFileList=new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            findAllDelFile(recoverFileList,userId,fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
        }
        //查询根目录下的文件
//        fileInfoQuery=new FileInfoQuery();
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFilePid(Constants.ZERO_STRING);
//        List<FileInfo> rootFileList= fileInfoService.findListByParam(fileInfoQuery);
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        queryWrapper.eq(FileInfo::getFilePid,Constants.ZERO_STRING);
        List<FileInfo> rootFileList=list(queryWrapper);
        Map<String ,FileInfo> map=rootFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(data1,data2)->data2));
        //查询所有文件 将目录下的所有删除文件更新为使用中
        if(!recoverFileList.isEmpty()){
            FileInfo fileInfo=new FileInfo();
            fileInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
            fileInfo.setUserId(userId);
            this.fileinfoMapper.updateFileDelFlagBatch(fileInfo,userId,null,recoverFileList,FileDelFlagEnums.DEL.getFlag());
        }
        //将选中文件更新为正常 且父目录到根目录
        List<String>delFileIdList=Arrays.asList(fileArrays);
        if(!delFileIdList.isEmpty()){
            FileInfo fileInfo=new FileInfo();
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            fileInfo.setUserId(userId);
            fileInfo.setLastUpdateTime(new Date());
            fileInfo.setFilePid(Constants.ZERO_STRING);
            this.fileinfoMapper.updateFileDelFlagBatch(fileInfo,userId,null,delFileIdList,FileDelFlagEnums.RECYCLE.getFlag());
        }
        //将文件名已经存在的文件重命名
        for (FileInfo fileInfo : fileInfoList) {
            if(map.containsKey(fileInfo.getFileName())){
                String newName=StringTools.rename(fileInfo.getFileName());
                fileInfo.setFileName(newName);
                fileinfoMapper.updateById(fileInfo);
            }
        }
    }

    @Override
    public void delFileBatch(String userId, String fileIdss, boolean adminOp) {
        //查询所有需要删除文件
        String[]fileIds=fileIdss.split(",");
//        FileInfoQuery fileInfoQuery=new FileInfoQuery();
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFileIdArray(fileId);
//        List<FileInfo> delFileList = fileInfoService.findListByParam(fileInfoQuery);
        List<FileInfo> delFileList=new LinkedList<>();
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        for (String fileId : fileIds) {
            delFileList.add(getFileInfo(queryWrapper,fileId));
        }
        //找到所选文件子目录文件id
        List<String> fileIdList=new ArrayList<>();
        for (FileInfo fileInfo : delFileList) {
            findAllDelFile(fileIdList,userId,fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
        }
        //删除所选文件和子文件
        fileinfoMapper.delFileBatch(userId,null,fileIdList,FileDelFlagEnums.DEL.getFlag());
        fileinfoMapper.delFileBatch(userId,null,fileIdList,FileDelFlagEnums.RECYCLE.getFlag());
        //更新用户使用空间
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        List<FileInfo> fileInfoList=list(queryWrapper);
        //TODO 求和是不是有更好的方法
        Long useSpace=0L;
        for (FileInfo fileInfo : fileInfoList) {
            useSpace+=fileInfo.getFileSize();
        }
//        UserInfo userInfo=new UserInfo();
        LambdaQueryWrapper<UserInfo> userQueryWrapper=new LambdaQueryWrapper<>();
        userQueryWrapper.eq(UserInfo::getUserId,userId);
        UserInfo userInfo = infoMapper.selectOne(userQueryWrapper);
        userInfo.setUseSpace(useSpace);
        userInfoMapper.updateById(userInfo);
        //更新redis信息
        UserSpaceDto userSpaceDto=redisComponent.getUserSpaceUse(userId);
        userSpaceDto.setUseSpace(useSpace);
        redisComponent.saveUserSpaceUse(userId,userSpaceDto);
    }

    private void findAllDelFile( List<String> delFileList,String userId, String fileId,Integer delFlag) {
        delFileList.add(fileId);
        FileInfoQuery fileInfoQuery=new FileInfoQuery();
        fileInfoQuery.setFilePid(fileId);
        fileInfoQuery.setUserId(userId);
        //用户选中的可能是一个文件夹，地址下可能有多个文件
//        List<FileInfo> fileInfoList=  fileInfoService.findListByParam(fileInfoQuery);
        List<FileInfo> fileInfoList=  new LinkedList<>();
        if(fileInfoList.isEmpty())return;
        for (FileInfo fileInfo : fileInfoList) {
            findAllDelFile(delFileList,userId,fileInfo.getFileId(),delFlag);
        }
    }

//    @Override
//    public void download(HttpServletRequest request, HttpServletResponse response, String code) {
//        //从redis中获取下载文件信息（dto）
//        DownloadFileDto fileDto = redisComponent.getDownloadCode(code);
//        if(fileDto==null)return;
//        String filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileDto.getFilePath();
//        String fileName=fileDto.getFileName();
//        response.setContentType("application/x-msdownload;charset=UTF-8");
//        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0){
//            fileName= URLEncoder.encode(fileName,"UTF-8");
//        }else{
//            fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
//        }
//        response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
//        readFile(response,filePath);
//    }

    private void checkFileName(String filePid, String userId, String fileName) {
//        FileInfoQuery fileInfoQuery = new FileInfoQuery();
//        fileInfoQuery.setUserId(userId);
//        fileInfoQuery.setFilePid(filePid);
//        fileInfoQuery.setFileName(fileName);
//        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        queryWrapper.eq(FileInfo::getFilePid,filePid);
        queryWrapper.eq(FileInfo::getFileName,fileName);
        queryWrapper.eq(FileInfo::getFolderType,FileFolderTypeEnums.FOLDER.getType());
        int cnt = fileinfoMapper.selectCount(queryWrapper);
        if (cnt > 0) {
            throw new BusinessException("已存在同名文件夹，请重新命名");
        }
    }

    public static void union(String dirPath, String toFilePath, String fileName, boolean delSource) throws BusinessException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        File fileList[] = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    logger.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            logger.error("合并文件:{}失败", fileName, e);
            throw new BusinessException("合并文件" + fileName + "出错了");
        } finally {
            try {
                if (null != writeFile) {
                    writeFile.close();
                }
            } catch (IOException e) {
                logger.error("关闭流失败", e);
            }
            if (delSource) {
                if (dir.exists()) {
                    try {
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Async
    public void transferFile(String fileId, SessionWebUserDto webUserDto) throws IOException {
        Boolean transferSuccess = true;
        //如果不存在这个文件，或者文件的状态不是转码中直接返回

//        FileInfo fileInfo = fileinfoMapper.selectByFileId(fileId);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId,fileId);
        FileInfo fileInfo=fileinfoMapper.selectOne(queryWrapper);
        if (fileInfo == null || !(fileInfo.getStatus().equals(FileStatusEnums.TRANSFER.getStatus()))) {
            return;
        }
        String realname = webUserDto.getUserId() + fileId;
        String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
        //获取临时文件的文件目录
        String sourcePath = appConfig.getProjectFolder() + Constants.TEMP_FILE_FOLDER + realname;
        //获取目标文件的目录
        String cover = null;
        String month = DateUtils.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYYMM.getPattern());
        String targetFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month + "/";
        File targetFolderFile = new File(targetFolder);
        if (!targetFolderFile.exists()) {
            targetFolderFile.mkdirs();
        }
        String target = targetFolder + realname + fileSuffix;
        union(sourcePath, target, fileInfo.getFileName(), true);
        //获取文件类型
        FileTypeEnums fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
        //视频文件切割
        if (FileTypeEnums.VIDEO == fileTypeEnum) {
            cutFile4Video(fileId, target);
            //生成视频缩略图
            cover = month + "/" + realname + "_" + Constants.IMAGE_PNG_SUFFIX;
            String coverPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + cover;
            ScaleFilter.createCover4Video(new File(target), Constants.LENGTH_150, new File(coverPath));
            cover = month + "/" + realname + "_" + Constants.IMAGE_PNG_SUFFIX;
        } else if (FileTypeEnums.IMAGE == fileTypeEnum) {
            //生成缩略图
            cover = month + "/" + realname + fileSuffix.replace(".", "_.");
            String coverPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + cover;
            Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(target), Constants.LENGTH_150, new File(coverPath), false);
            if (!created) {
                FileUtils.copyFile(new File(target), new File(coverPath));
            }
        }
//        FileInfo updateInfo = new FileInfo();
//        updateInfo.setFileSize(new File(target).length());
//        updateInfo.setFileCover(cover);
//        updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRSNSFER_FALL.getStatus());
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId,fileId);
        queryWrapper.eq(FileInfo::getUserId,webUserDto.getUserId());
        queryWrapper.eq(FileInfo::getStatus, FileStatusEnums.TRANSFER.getStatus());
        FileInfo updateInfo=fileinfoMapper.selectOne(queryWrapper);
        updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRSNSFER_FALL.getStatus());
        fileinfoMapper.updateById(updateInfo);
    }

    private void cutFile4Video(String fileId, String videoFilePath) {
        //创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        //vbsf改成-bsf
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -bsf:v h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //生成.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        //生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        //删除index.ts
        new File(tsPath).delete();
    }

    private void updateUserSpace(SessionWebUserDto webUserDto, Long fileSize) {
        UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
        Long totalSpace = spaceDto.getTotalSpace();
        long useSpace = spaceDto.getUseSpace();
        if (useSpace + fileSize > totalSpace) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
//        UserInfo userInfo = (UserInfo) infoMapper.selectByUserId(webUserDto.getUserId());
        spaceDto.setUseSpace(useSpace + fileSize);
        redisComponent.saveUserSpaceUse(webUserDto.getUserId(), spaceDto);
    }

    private String autoRename(String filePid, String userId, String fileName) {
        //判断是否为第一次上传
//        FileInfoQuery infoQuery = new FileInfoQuery();
//        infoQuery.setStatus(FileStatusEnums.USING.getStatus());
//        infoQuery.setFilePid(filePid);
//        infoQuery.setUserId(userId);
//        infoQuery.setFileName(fileName);
//        Integer cnt = fileinfoMapper.selectCount(infoQuery);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getStatus,FileStatusEnums.USING.getStatus());
        queryWrapper.eq(FileInfo::getFilePid,filePid);
        queryWrapper.eq(FileInfo::getUserId,userId);
        queryWrapper.eq(FileInfo::getFileName,fileName);
        Integer cnt = fileinfoMapper.selectCount(queryWrapper);
        if (cnt > 0) {
            fileName = StringTools.rename(fileName);
        }
        return fileName;
    }
    @Override
    public Long getUserUseSpace(String userId) {
        LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId,userId);
//        FileInfo fileInfo=this.fileinfoMapper.selectList(queryWrapper);
        return infoMapper.selectOne(queryWrapper).getUseSpace();
    }

    @Override
    public void checkRootFilePid(String rootFilePid, String userId, String fileId) {
        if (StringTools.isEmpty(fileId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (rootFilePid.equals(fileId)) {
            return;
        }
        checkFilePid(rootFilePid, fileId, userId);
    }

    @Override
    public void saveShare(String fileId, String shareFileIds, String myFolderId, String shareUserId, String userId) {
        //获取保存目录的原文件列表
        FileInfoQuery fileInfoQuery=new FileInfoQuery();
        fileInfoQuery.setFilePid(myFolderId);
        fileInfoQuery.setUserId(userId);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFilePid,myFolderId);
        queryWrapper.eq(FileInfo::getUserId,userId);
        List<FileInfo>  currentList = list(queryWrapper);
        //查询保存文件列表
        String[] fileArrays=shareFileIds.split(",");
//        fileInfoQuery=new FileInfoQuery();
//        fileInfoQuery.setFileIdArray(fileArrays);
//        fileInfoQuery.setUserId(userId);
//        List<FileInfo>  shareList = fileinfoMapper.selectList(fileInfoQuery);
        List<FileInfo>  shareList = new LinkedList<>();
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUserId,userId);
        for (String fileArray : fileArrays) {
            shareList.add(getFileInfo(queryWrapper,fileArray));
        }
        Map<String ,FileInfo> map=currentList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(data1,data2)->data2));
        List<FileInfo> copyList=new ArrayList<>();
        for (FileInfo fileInfo : shareList) {
            if(map.containsKey(fileInfo.getFileName())){
                fileInfo.setFileName(StringTools.rename(fileInfo.getFileName()));
            }
            findAllFile(copyList,fileInfo,shareUserId,userId,new Date(),myFolderId);
        }
    }

    @Override
    public PaginationResultVO loadFileList(FileInfoQuery fileInfoQuery) {
        //按照最后更新时间排序
//        fileInfoQuery.setOrderBy("last_update_time desc");
//        fileInfoQuery.setQueryNickName(true);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(FileInfo::getLastUpdateTime);
        if(fileInfoQuery.getOrderBy()!=null&&fileInfoQuery.getOrderBy().equals("recovery_time desc")){
            queryWrapper.orderByDesc(FileInfo::getRecoveryTime);
        }
        if(fileInfoQuery.getUserId()!=null){
            queryWrapper.eq(FileInfo::getUserId,fileInfoQuery.getUserId());
        }
        if(fileInfoQuery.getFileCategory()!=null){
            queryWrapper.eq(FileInfo::getFileCategory,fileInfoQuery.getFileCategory());
        }
        if( fileInfoQuery.getDelFlag()!=null){
            queryWrapper.eq(FileInfo::getDelFlag,fileInfoQuery.getDelFlag());
        }
        if(fileInfoQuery.getFilePid()!=null){
            queryWrapper.eq(FileInfo::getFilePid,fileInfoQuery.getFilePid());
        }
        if(fileInfoQuery.getFileId()!=null){
            queryWrapper.eq(FileInfo::getFileId,fileInfoQuery.getFileId());
        }
        List<FileInfo> fileInfoList=fileinfoMapper.selectList(queryWrapper);
//        PaginationResultVO resultVO=fileInfoService.findListByPage(fileInfoQuery);
        Page<FileInfo> page = new Page<>();
        page.setTotal(fileInfoList.size());
        if (null != fileInfoQuery.getPageNo())
            page.setCurrent(fileInfoQuery.getPageNo());
        page.setSize(fileInfoQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : fileInfoQuery.getPageSize());
        page(page,queryWrapper);
        return new PaginationResultVO(fileInfoList.size(), (int) page.getSize(), (int) page.getCurrent(), page.getRecords());
    }

    private void findAllFile(List<FileInfo> copyList, FileInfo fileInfo, String shareUserId, String userId, Date date, String myFolderId) {
        String pid=fileInfo.getFileId();
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(myFolderId);
        fileInfo.setCreateTime(new Date());
        fileInfo.setFileId(StringTools.getRandomNumber(10));
        copyList.add(fileInfo);
        if(fileInfo.getFileType().equals(FileFolderTypeEnums.FOLDER)){
//            FileInfoQuery query=new FileInfoQuery();
//            query.setFilePid(pid);
//            query.setUserId(userId);
            LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getFilePid,pid);
            queryWrapper.eq(FileInfo::getUserId,userId);
            List<FileInfo>  shareList = list(queryWrapper);
            for (FileInfo info : shareList) {
                findAllFile(copyList,info,shareUserId,userId,date,info.getFilePid());
            }
        }
    }

    private void checkFilePid(String rootFilePid, String fileId, String userId) {

//        FileInfo fileInfo = this.fileinfoMapper.selectByFileId(fileId);
        LambdaQueryWrapper<FileInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileId,fileId);
        FileInfo fileInfo = fileinfoMapper.selectOne(queryWrapper);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (Constants.ZERO_STRING.equals(fileInfo.getFilePid())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (fileInfo.getFilePid().equals(rootFilePid)) {
            return;
        }
        checkFilePid(rootFilePid, fileInfo.getFilePid(), userId);
    }

}