package com.easypan.controller;


import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.ShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.impl.ShareServiceImpl;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @Description: Controller
 * @Author: false
 * @Date: 2024/06/27 16:01:13
 */
@RestController
@RequestMapping("/share")
public class ShareController extends ABaseController {

    @Resource
    private ShareServiceImpl shareService;

    @RequestMapping("/loadShareList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadShareList(HttpSession session, ShareQuery query) {
        query.setOrderBy("share_time desc");
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setQueryFileName(true);
        PaginationResultVO resultVO = shareService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session,
                                @VerifyParam(required = true) String fileId,
                                @VerifyParam(required = true) Integer validType,
                                String code) {
        FileShare fileShare = new FileShare();
        fileShare.setFileId(fileId);
        fileShare.setUserId(getUserInfoFromSession(session).getUserId());
        fileShare.setCode(code);
        fileShare.setValidType(validType);
        shareService.saveShare(fileShare);
        return getSuccessResponseVO(fileShare);
    }

    @RequestMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session,
                                  @VerifyParam(required = true) String shareIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        shareService.delFileShareBatch(webUserDto.getUserId(), shareIds.split(","));
        return getSuccessResponseVO(null);
    }


    /**
     * 新增
     */
    @RequestMapping("/add")
    public ResponseVO add(FileShare bean) {
        Integer result = this.shareService.add(bean);
        return getSuccessResponseVO(null);
    }

    /**
     * 批量新增
     */
    @RequestMapping("/addBatch")
    public ResponseVO addBatch(@RequestBody List<FileShare> listBean) {
        this.shareService.addBatch(listBean);
        return getSuccessResponseVO(null);
    }

    /**
     * 批量新增或修改
     */
    @RequestMapping("/addOrUpdateBatch")
    public ResponseVO addOrUpdateBatch(@RequestBody List<FileShare> listBean) {
        this.shareService.addOrUpdateBatch(listBean);
        return getSuccessResponseVO(null);
    }
}