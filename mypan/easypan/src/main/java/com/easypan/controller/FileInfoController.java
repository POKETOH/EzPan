package com.easypan.controller;


import java.io.UnsupportedEncodingException;
import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Description: Controller
 * @Author: false
 * @Date: 2024/06/18 11:25:12
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends CommonFileController {

    @Resource
    private FileInfoService fileInfoService;

    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true )
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {
        FileCategoryEnums categoryEnums = FileCategoryEnums.getByCode(category);
        if (categoryEnums != null) {
            query.setFileCategory(categoryEnums.getCategory());
        }
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO result = fileInfoService.loadFileList(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks
    ) {
        //前端已经分片，传入参数是分片文件的个数和第几片
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(resultDto);
    }

    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        System.out.println(imageFolder);
        System.out.println(imageName);
        super.getImage(response, imageFolder, imageName);
    }
    @RequestMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getgetVideoInfo(HttpServletResponse response,HttpSession session,@PathVariable("fileId")String fileId){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        super.getFile(response,fileId, webUserDto.getUserId());
    }
    @RequestMapping("/getFile/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getFile(HttpServletResponse response,HttpSession session,@PathVariable("fileId")String fileId){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        super.getFile(response,fileId, webUserDto.getUserId());
    }
    @RequestMapping("/newFoloder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO newFoloder(HttpSession session,@VerifyParam(required = true) String filePid,@VerifyParam(required = true)String fileName){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        FileInfo fileInfo=fileInfoService.newFoloder(filePid,webUserDto.getUserId(),fileName);
        return getSuccessResponseVO(fileInfo);
    }
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session,@VerifyParam(required = true) String path){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        return super.getFolderInfo(path,webUserDto.getUserId());
    }
    @RequestMapping("/rename")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO rename(HttpSession session,@VerifyParam(required = true) String fileId,@VerifyParam(required = true) String fileName){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        FileInfo fileInfo =fileInfoService.rename(webUserDto.getUserId(),fileId,fileName);
        return getSuccessResponseVO(fileInfo);
    }
    @RequestMapping("/loadAllFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadAllFolder(HttpSession session,@VerifyParam(required = true) String filePid, String currentFildIds){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        List<FileInfo> fileInfo =fileInfoService.loadAllFolder(webUserDto.getUserId(),filePid,currentFildIds);
        return getSuccessResponseVO(fileInfo);
    }
    @RequestMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO changeFileFolder(HttpSession session,@VerifyParam(required = true) String fileIds,@VerifyParam(required = true) String filePid){
        //TODO 批量移动时存在问题
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        fileInfoService.changeFileFolder(webUserDto.getUserId(),fileIds,filePid);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session,@PathVariable("fileId")String fileId){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        String code=fileInfoService.createDownloadUrl(webUserDto.getUserId(),fileId);
        return getSuccessResponseVO(code);
    }
    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }


}