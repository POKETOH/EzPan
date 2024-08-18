package com.easypan.service.impl;


import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.enums.PageSize;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.CodeMapper;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @Description: 业务接口实现
 * @Author: false
 * @Date: 2024/06/11 22:25:02
 */
@Service("CodeMapper")
public class CodeServiceImpl extends ServiceImpl<CodeMapper, EmailCode> implements EmailCodeService {
    private static final Logger logger = LoggerFactory.getLogger(CodeServiceImpl.class);
    @Resource
    RedisComponent redisComponent;
    @Resource
    private CodeMapper codeMapper;
    @Resource
    private InfoMapper userInfoMapper;

    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private AppConfig appConfig;
//
//    /**
//     * 分页查询
//     */
//    @Override
//    public PaginationResultVO<com.easypan.entity.po.EmailCode> findListByPage(com.easypan.entity.query.EmaliCodeQuery query) {
//        Integer count = this.findCountByParam(query);
//        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
//        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
//        query.setSimplePage(page);
//        List<com.easypan.entity.po.EmailCode> list = this.findListByParam(query);
//        PaginationResultVO<com.easypan.entity.po.EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
//        return result;
//    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String email, Integer type) {
        if (type == Constants.ZERO) {
//            UserInfo userInfo = userInfoMapper.selectByEmail(email);
            LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(UserInfo::getEmail,email);
            UserInfo userInfo=userInfoMapper.selectOne(queryWrapper);
            if (null != userInfo) {
                throw new BusinessException("邮箱已存在");
            }
        }
        String code = StringTools.getRandomNumber(Constants.Length_5);
        sendEmailCode(email,code);
        //将之前的验证码置为无效
        codeMapper.disableEmailCode(email);
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(email);
        emailCode.setStatus(Constants.ZERO);
        emailCode.setCreateTime(new Date());
        codeMapper.insert(emailCode);
    }

    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode=this.codeMapper.selectByEmailAndCode(email,code);
        if(emailCode==null){
            throw new BusinessException("邮箱验证码错误");
        }
        if(emailCode.getStatus()==1|| System.currentTimeMillis()-emailCode.getCreateTime().getTime()>60*15*1999){
            throw new BusinessException("邮箱验证码已过期");
        }
        codeMapper.disableEmailCode(email);
    }

    private void sendEmailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(appConfig.getFromEmail());
            helper.setTo(toEmail);
            SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
            helper.setSubject(sysSettingsDto.getRegisterEmailTitle());
            helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败");
        }
    }
}