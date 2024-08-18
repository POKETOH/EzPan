package com.easypan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.enums.PageSize;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.impl.FileInfoServiceImpl;
import com.easypan.service.impl.InfoServiceImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends CommonFileController {
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private InfoServiceImpl infoService;

    @Resource
    private InfoMapper infoMapper;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private FileInfoServiceImpl fileInfoService;
    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO getSysSettings(){
        return getSuccessResponseVO(redisComponent.getSysSettingsDto());
    }
    @RequestMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)

    public ResponseVO saveSysSettings(@VerifyParam(required = true)String registerEmailTitle,
                                      @VerifyParam(required = true)String registerEmailContent,
                                      @VerifyParam(required = true)Integer userInitUseSpace){
        SysSettingsDto sysSettingsDto=new SysSettingsDto();
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setUserInitUseSpace(userInitUseSpace);
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        redisComponent.saveSysSettingsDto(sysSettingsDto);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery){
        //按照注册时间排序
//        userInfoQuery.setOrderBy("join_time desc");
//        //TODO 获取所以用户
//        LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(UserInfo::getJoinTime);
//        List<UserInfo> userInfos = infoMapper.selectList(queryWrapper);
//        PaginationResultVO resultVO=infoService.findListByPage(userInfoQuery);
        PaginationResultVO resultVO=infoService.getUserList(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer status){
        infoService.updateUserStatus(userId,status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer changeSpace){
        //按照注册时间排序
        infoService.changeUserSpace(userId,changeSpace);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery fileInfoQuery){
        PaginationResultVO resultVO =fileInfoService.loadFileList(fileInfoQuery);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkLogin = false,checkAdmin = true, checkParams = true)
    public ResponseVO getFolderInfo(@VerifyParam(required = true) String path) {
        return super.getFolderInfo(path, null);
    }


    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO getFile(HttpServletResponse response,
                              @PathVariable("userId") @VerifyParam(required = true) String userId,
                              @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        super.getFile(response, fileId, userId);
        return null;
    }


    @RequestMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public void getVideoInfo(HttpServletResponse response,
                             @PathVariable("userId") @VerifyParam(required = true) String userId,
                             @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        super.getFile(response, fileId, userId);
    }

    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO createDownloadUrl(@PathVariable("userId") @VerifyParam(required = true) String userId,
                                        @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        return super.createDownloadUrl(fileId, userId);
    }

    /**
     * 下载
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable("code") @VerifyParam(required = true) String code) throws UnsupportedEncodingException {
        super.download(request, response, code);
    }


    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO delFile(@VerifyParam(required = true) String fileIdAndUserIds) {
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        for (String fileIdAndUserId : fileIdAndUserIdArray) {
            String[] itemArray = fileIdAndUserId.split("_");
            fileInfoService.delFileBatch(itemArray[0], itemArray[1], true);
        }
        return getSuccessResponseVO(null);
    }
}
