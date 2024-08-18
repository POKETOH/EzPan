package com.easypan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.FileInfoService;
import com.easypan.service.impl.FileInfoServiceImpl;
import com.easypan.service.impl.InfoServiceImpl;
import com.easypan.service.impl.ShareServiceImpl;
import com.easypan.utils.CopyTools;
import com.easypan.utils.StringTools;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Date;
@RestController("webShareController")
@RequestMapping("/showShare")
public class WebShareController extends CommonFileController{
    @Resource
    private FileInfoServiceImpl fileInfoService;
    @Resource
    private InfoMapper infoMapper;
    @Resource
    private ShareServiceImpl shareService;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @RequestMapping("/getShareInfo")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO getShareInfo(@VerifyParam(required = true) String shareId) {
        return getSuccessResponseVO(getShareInfoCommon(shareId));
    }

    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareLoginInfo(HttpSession session,
                                   @VerifyParam(required = true)String shareId){
        SessionShareDto webUserDto = getSessionShareFromSession(session,shareId);
        if(webUserDto==null){
            return getSuccessResponseVO(null);
        }
        ShareInfoVO shareInfo=getShareInfoCommon(shareId);
        if(webUserDto!=null&&shareInfo.getUserId().equals(webUserDto.getShareUserId())){
            shareInfo.setCurrentUser(true);
        }
        else{
            shareInfo.setCurrentUser(false);
        }
        return getSuccessResponseVO(shareInfo);
    }
    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,
                                     @VerifyParam(required = true)String shareId,
                                     @VerifyParam(required = true)String code){
        SessionShareDto shareDto=shareService.checkShareCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,shareDto);
        return getSuccessResponseVO(shareDto);
    }

    private ShareInfoVO getShareInfoCommon(String shareId) {
        FileShare share = shareService.getFileShareByShareId(shareId);
        if (null == share || (share.getExpireTime() != null && new Date().after(share.getExpireTime()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        ShareInfoVO shareInfoVO = CopyTools.copy(share, ShareInfoVO.class);
        FileInfo fileInfo = fileInfoMapper.selectById(share.getFileId());
        if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        shareInfoVO.setFileName(fileInfo.getFileName());

//        UserInfo userInfo = infoService.getInfoByUserId(share.getUserId());
        LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId,share.getUserId());
        UserInfo userInfo = infoMapper.selectOne(queryWrapper);
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getAvatar());
        shareInfoVO.setUserId(userInfo.getUserId());
        return shareInfoVO;
    }
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO loadFileList (HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                    String filePid) {
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        FileInfoQuery query = new FileInfoQuery();
        if (!StringTools.isEmpty(filePid) && !Constants.ZERO_STRING.equals(filePid)) {
            fileInfoService.checkRootFilePid(sessionShareDto.getFileId(), sessionShareDto.getShareUserId(), filePid);
            query.setFilePid(filePid);
        } else {
            query.setFileId(sessionShareDto.getFileId());
        }
        query.setUserId(sessionShareDto.getShareUserId());
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        query.setOrderBy("last_update_time desc");
        PaginationResultVO resultVO=fileInfoService.loadFileList(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }

    /**
     * 校验分享是否失效
     *
     * @param session
     * @param shareId
     * @return
     */
    private SessionShareDto checkShare(HttpSession session, String shareId) {
        SessionShareDto shareSessionDto = getSessionShareFromSession(session, shareId);
        if (shareSessionDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if (shareSessionDto.getExpireTime() != null && new Date().after(shareSessionDto.getExpireTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return shareSessionDto;
    }
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                    @VerifyParam(required = true) String path){
        SessionShareDto shareDto=checkShare(session,shareId);
        return super.getFolderInfo(path,shareDto.getShareUserId());
    }
//    @RequestMapping("/getFile/{shareId}/{fileId}")
//    @GlobalInterceptor(checkParams = true,checkLogin = false)
//    public ResponseVO getFolderInfo(HttpServletResponse response,HttpSession session,
//                                    @PathVariable("shareId") String shareId,
//                                    @PathVariable("fileId") String fileId){
//        SessionShareDto shareDto=checkShare(session,shareId);
//        return super.getFile(response,fileId,shareDto.getShareUserId());
//    }
//
@RequestMapping("/ts/getVideoInfo/{shareId}/{fileId}")
public void getVideoInfo(HttpServletResponse response,
                         HttpSession session,
                         @PathVariable("shareId") @VerifyParam(required = true) String shareId,
                         @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
    SessionShareDto shareSessionDto = checkShare(session, shareId);
    super.getFile(response, fileId, shareSessionDto.getShareUserId());
}

    @RequestMapping("/getFile/{shareId}/{fileId}")
    public void getFile(HttpServletResponse response,HttpSession session,
                        @PathVariable("shareId") @VerifyParam(required = true) String shareId,
                        @PathVariable("fileId") @VerifyParam(required = true) String fileId){
        SessionShareDto webUserDto=checkShare(session,shareId);
        super.getFile(response,fileId, webUserDto.getShareId());
    }
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable("code")String code) throws UnsupportedEncodingException {
        super.download(request,response,code);
    }
    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @PathVariable("shareId") @VerifyParam(required = true) String shareId,
                                        @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        SessionShareDto shareSessionDto = checkShare(session, shareId);
        return super.createDownloadUrl(fileId, shareSessionDto.getShareUserId());
    }

    @RequestMapping("/saveShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO saveShare(HttpSession session,
                          @VerifyParam(required = true) String shareId,
                          @VerifyParam(required = true) String shareFileIds,
                          @VerifyParam(required = true) String myFolderId) throws UnsupportedEncodingException {
        //判断分享人和当前用户是否一样
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        SessionShareDto sessionShareDto = checkShare(session, shareId);
        if(sessionShareDto.getShareUserId().equals(webUserDto.getUserId())){
            throw new BusinessException("不能保存自己分享的文件");
        }
        fileInfoService.saveShare(sessionShareDto.getFileId(),shareFileIds,myFolderId,sessionShareDto.getShareUserId(),webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
