package com.easypan.controller;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.po.UserInfo;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.InfoService;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Description: Controller
 * @Author: false
 * @Date: 2024/06/08 21:49:50
 */
@RestController("userInfoController")
public class AccountController extends ABaseController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

    @Resource
    private InfoService infoService;

    @Resource
    private EmailCodeService emailCodeService;
    @Resource
    AppConfig appConfig;

    @Resource
    RedisComponent redisComponent;
    @Resource
    InfoMapper infoMapper;

    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        // 定义图形验证码的长和宽
        LineCaptcha vCode = CaptchaUtil.createLineCaptcha(130, 38, 5, 10);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpge");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        // 图形验证码写出，可以写出到文件，也可以写出到流
        vCode.write(response.getOutputStream());
        response.getOutputStream().close();
    }

    @RequestMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true) String email, @VerifyParam(required = true) String checkCode, @VerifyParam(required = true) Integer type) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码不正确!");
            }
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    @RequestMapping("/register")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL) String email,
                               @VerifyParam(required = true) String nickName,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
                               @VerifyParam(required = true)  String checkCode,
                               @VerifyParam(required = true)  String emailCode
                               ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确!");
            }
            infoService.register(email,nickName,password,emailCode,checkCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }
    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO login(HttpSession session,
                               @VerifyParam(required = true) String email,
                               @VerifyParam(required = true) String password,
                               @VerifyParam(required = true)  String checkCode
    ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            SessionWebUserDto sessionWebUserDto=infoService.login(email,password);
            session.setAttribute(Constants.SESSION_KEY,sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @RequestMapping("/resetPwd")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL) String email,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
                               @VerifyParam(required = true)  String checkCode,
                               @VerifyParam(required = true)  String emailCode
    ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确!");
            }
            infoService.resetPwd(email,password,emailCode,checkCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }
    @RequestMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true)@PathVariable("userId")String userId){
        //头像目录
        String avatarFolderName=Constants.FILE_FOLDER_FILE+Constants.FILE_FOLDER_AVARAE_NAME;
        File folder=new File(appConfig.getProjectFolder()+avatarFolderName);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String avatarPath=appConfig.getProjectFolder()+avatarFolderName+userId+Constants.AVARAE_SUFFIX;
        File file=new File(avatarPath);
        if(!file.exists()){
            if(!new File(appConfig.getProjectFolder()+avatarFolderName+Constants.AVARAE_DEFAULT+Constants.AVARAE_SUFFIX).exists()){
                printNoDefaultImage(response);
            }
            avatarPath=appConfig.getProjectFolder()+avatarFolderName+Constants.AVARAE_DEFAULT+Constants.AVARAE_SUFFIX;
        }
        response.setContentType("image/jpg");
        readFile(response,avatarPath);
    }

    //获取用户信息
    @RequestMapping("/getUseInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getUseInfo(HttpSession session){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        return getSuccessResponseVO(webUserDto);
    }
    //获取用户使用空间
    @RequestMapping("/getUseSpace")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getUseSpace(HttpSession session){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        UserSpaceDto spaceDto=redisComponent.getUserSpaceUse(webUserDto.getUserId());
        return getSuccessResponseVO(spaceDto);
    }
    //退出登录
    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session){
        session.invalidate();
        return getSuccessResponseVO(null);
    }
    //更新用户头像
    @RequestMapping("/updateUserAvatar")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar){
        String baseFolder=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
        //判断文件目录
        if(!new File(baseFolder+Constants.FILE_FOLDER_AVARAE_NAME).exists())
            new File(baseFolder+Constants.FILE_FOLDER_AVARAE_NAME).mkdirs();
        //获取用户id
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        File target=new File(baseFolder+Constants.FILE_FOLDER_AVARAE_NAME+webUserDto.getUserId()+Constants.AVARAE_SUFFIX);
        //上传头像
        try {
            avatar.transferTo(target);
        } catch (IOException e) {
            logger.error("头像上传失败");
        }
        //更新用户信息
        UserInfo userInfo=new UserInfo();
        //TODO设置QQ头像为空
        return getSuccessResponseVO(null);
    }
    //修改密码
    @RequestMapping("/updatePassword")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updatePassword(HttpSession session,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password
    ) {
        SessionWebUserDto sessionWebUserDto=getUserInfoFromSession(session);
        LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId,sessionWebUserDto.getUserId());
        UserInfo userInfo=infoMapper.selectOne(queryWrapper);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        infoMapper.updateById(userInfo);
        return getSuccessResponseVO(null);
    }
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
            writer.close();
        } catch (Exception e) {
            logger.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }
}