package com.easypan.service.impl;


import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.UploadStatusEnums;
import com.easypan.entity.enums.UserStatusEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.enums.PageSize;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.InfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.InfoService;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.utils.StringTools;
import org.apache.catalina.User;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.easypan.utils.StringTools.encodeByMd5;

/**
 * @Description: 业务接口实现
 * @Author: false
 * @Date: 2024/06/08 21:49:50
 */
@Service("InfoMapper")
public class InfoServiceImpl extends ServiceImpl<InfoMapper, UserInfo> implements InfoService {
    @Resource
    private InfoMapper infoMapper;
    @Resource
    RedisComponent redisComponent;
    @Resource
    EmailCodeService emailCodeService;
    @Resource
    AppConfig appConfig;
    @Resource
    FileInfoMapper fileInfoMapper;
    @Resource
    FileInfoServiceImpl fileInfoService;

    @Override
    public SessionWebUserDto login(String email, String password) {

//		UserInfo userInfo = this.infoMapper.selectByEmail(email);
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, email);
        UserInfo userInfo = infoMapper.selectOne(queryWrapper);
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userInfo.getUserId());
        UserInfo updateInfo = infoMapper.selectOne(queryWrapper);
        updateInfo.setLastLoginTime(new Date());
        this.infoMapper.updateById(updateInfo);
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setUserId(userInfo.getUserId());
        if (ArrayUtils.contains(appConfig.getAdminEmail().split(","), email)) {
            sessionWebUserDto.setAdmin(true);
        } else {
            sessionWebUserDto.setAdmin(false);
        }
        //用户空间
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        userSpaceDto.setUseSpace(fileInfoService.getUserUseSpace(userInfo.getUserId()));
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
        return sessionWebUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, String nickname, String emailCode, String checkCode) {
//		UserInfo userinfo=this.infoMapper.selectByEmail(email);
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, email);
        UserInfo userinfo = infoMapper.selectOne(queryWrapper);
        if (userinfo != null) {
            throw new BusinessException("邮箱账号已存在");
        }
        emailCodeService.checkCode(email, emailCode);
        String userid = StringTools.getRandomNumber(10);
        userinfo = new UserInfo();
        userinfo.setEmail(email);
        userinfo.setPassword(encodeByMd5(password));
        userinfo.setUserId(userid);
        userinfo.setJoinTime(new Date());
        userinfo.setUseSpace(0L);
        Long useSpace = fileInfoMapper.selectUseSpace(userinfo.getUserId());
        userinfo.setUseSpace(useSpace);
        userinfo.setNickName(nickname);
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        userinfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userinfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace() * Constants.MB);
        this.infoMapper.insert(userinfo);
    }

    /**
     * 分页查询
     */
//	@Override
//	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
//		Integer count = this.findCountByParam(query);
//		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
//		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
//		query.setSimplePage(page);
//		List<UserInfo> list = this.findListByParam(query);
//		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
//		return result;
//	}
    @Override
    public void resetPwd(String email, String password, String emailCode, String checkCode) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, email);
        UserInfo userInfo = infoMapper.selectOne(queryWrapper);
        if (userInfo == null) {
            throw new BusinessException("邮箱不存在");
        }
        emailCodeService.checkCode(email, emailCode);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        this.infoMapper.updateById(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserSpace(String userId, Integer changeSpace) {
        Long space = changeSpace * Constants.MB;
        infoMapper.updateUserSpace(userId, null, space);
        redisComponent.resetUserSpaceUse(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String userId, Integer status) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userId);
        UserInfo userInfo = infoMapper.selectOne(queryWrapper);
//		UserInfo userInfo=new UserInfo();
        userInfo.setStatus(status);
        if (status.equals(UserStatusEnum.DISABLE.getStatus())) {
            userInfo.setUseSpace(0L);
            LambdaQueryWrapper<FileInfo> queryFile=new LambdaQueryWrapper<>();
            queryFile.eq(FileInfo::getUserId,userId);
            fileInfoMapper.delete(queryFile);
//            fileInfoMapper.deleteByFileId(userId);
        }
        infoMapper.updateById(userInfo);
    }

    @Override
    public PaginationResultVO getUserList(UserInfoQuery userInfoQuery) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UserInfo::getJoinTime);
        List<UserInfo> userInfos = infoMapper.selectList(queryWrapper);
        Page<UserInfo> page = new Page<>();
        page.setTotal(userInfos.size());
        if (null != userInfoQuery.getPageNo())
            page.setCurrent(userInfoQuery.getPageNo());
        page.setSize(userInfoQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : userInfoQuery.getPageSize());
        page(page, queryWrapper);
        PaginationResultVO resultVO = new PaginationResultVO(userInfos.size(), (int) page.getSize(), (int) page.getCurrent(), page.getRecords());
        return resultVO;
    }
}