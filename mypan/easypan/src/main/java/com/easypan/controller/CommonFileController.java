package com.easypan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileFolderTypeEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.service.impl.FileInfoServiceImpl;
import com.easypan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class CommonFileController extends ABaseController {
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private FileInfoServiceImpl fileInfoService;
    @Resource
    private FileInfoMapper fileInfoMapper;

    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringUtils.isBlank(imageName)) {
            return;
        }
        String imageSuffix = StringTools.getFileSuffix(imageName);
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", "");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, filePath);
    }

    public ResponseVO getFile(HttpServletResponse response, String fileId, String userId) {
        String target = null;
        if (fileId.endsWith(".ts")) {
            String[] str = fileId.split("_");
            String realId =str[0];
//            FileInfo fileInfo = fileInfoService.getInfoByFileId(realId);
            FileInfo fileInfo = fileInfoMapper.selectById(realId);
            target=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileInfo.getFilePath().substring(0,fileInfo.getFilePath().indexOf("."))+"/"+fileId;
        } else {
            //查询视频文件文件夹位置
//            FileInfo fileInfo = fileInfoService.getInfoByFileId(fileId);
            FileInfo fileInfo = fileInfoMapper.selectById(fileId);
            if(FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                // 返回m3u8索引文件
                target = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath().substring(0, fileInfo.getFilePath().indexOf(".")) + "/" + Constants.M3U8_NAME;
            }
            else {
                target=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileInfo.getFilePath();
                System.out.println("");
            }
            readFile(response, target);
        }
        readFile(response,target);
        return getSuccessResponseVO(null);
    }

    protected ResponseVO getFolderInfo(String path, String userId) {
        //可能存在多个路径
        String[] pathArrary=path.split("/");
        //查询路径下的文件
//        FileInfoQuery infoQuery=new FileInfoQuery();
//        infoQuery.setUserId(userId);
//        infoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
//        infoQuery.setFileIdArray(pathArrary);
        String orderBy="field(file_id,\""+ StringUtils.join(pathArrary,"\",\"")+"\")";
//        infoQuery.setOrderBy(orderBy);
        List<FileInfo> fileInfoList = fileInfoMapper.findFolderInfo(userId,FileFolderTypeEnums.FOLDER.getType(),pathArrary,"file_id");

        return getSuccessResponseVO(fileInfoList);
    }

    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        DownloadFileDto fileDto = redisComponent.getDownloadCode(code);
        if(fileDto==null)return;
        String filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileDto.getFilePath();
        String fileName=fileDto.getFileName();
        response.setContentType("application/x-msdownload;charset=UTF-8");
        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0){
            fileName= URLEncoder.encode(fileName,"UTF-8");
        }else{
            fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
        }
        response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
        readFile(response,filePath);
    }
    protected ResponseVO createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code = StringTools.getRandomNumber(5);
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setDownloadCode(code);
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        downloadFileDto.setFileName(fileInfo.getFileName());

        redisComponent.saveDownloadCode(code, downloadFileDto);

        return getSuccessResponseVO(code);
    }
}