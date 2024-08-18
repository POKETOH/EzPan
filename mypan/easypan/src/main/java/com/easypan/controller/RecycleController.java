package com.easypan.controller;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.impl.FileInfoServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;

@RestController("recycleController")
@RequestMapping("/recycle")
public class RecycleController extends ABaseController{
    @Resource
    private FileInfoServiceImpl fileInfoService;
    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycleList(HttpSession session,Integer pageNo,Integer pageSize){
        FileInfoQuery fileInfoQuery=new FileInfoQuery();
        fileInfoQuery.setUserId(getUserInfoFromSession(session).getUserId());
        fileInfoQuery.setPageNo(pageNo);
        fileInfoQuery.setPageSize(pageSize);
        fileInfoQuery.setOrderBy("recovery_time desc");
        fileInfoQuery.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        PaginationResultVO resultVO = fileInfoService.loadFileList(fileInfoQuery);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }
    @RequestMapping("/recoverFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.recoverFileBatch(webUserDto.getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true)String fileIds) throws UnsupportedEncodingException {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.delFileBatch(webUserDto.getUserId(),fileIds,false);
        return getSuccessResponseVO(null);
    }
}
