package com.easypan.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.DownloadFileDto;
import com.easypan.entity.dto.SysSettingsDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.mappers.InfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("redisComponent")
public class RedisComponent {
    @Resource
     private RedisUtils redisUtils;

    @Resource
    FileInfoMapper fileInfoMapper;
    @Resource
    InfoMapper infoMapper;
    public SysSettingsDto getSysSettingsDto(){
        SysSettingsDto sysSettingsDto= (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SETTING);
        if(sysSettingsDto==null){
            sysSettingsDto=new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }
    public UserSpaceDto getUserSpaceUse(String userId){
        UserSpaceDto spaceDto= (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE+userId);
        if(spaceDto==null){
            spaceDto=new UserSpaceDto();
            spaceDto.setUseSpace(fileInfoMapper.selectUseSpace(userId));
            spaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace()*Constants.MB);
        }
        return spaceDto;
    }

    public void saveFileTempSize(String userId,String fileId,Long fileSize){
        Long curSize=getFileTempSize(userId,fileId);
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId,curSize+fileSize,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }

    public Long getFileTempSize(String userId, String fileId) {
        Long curSize=getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId);
        return curSize;
    }

    private Long getFileSizeFromRedis(String key){
        Object sizeObj=redisUtils.get(key);
        if(sizeObj==null)
            return 0L;
        if(sizeObj instanceof Integer){
            return ((Integer)sizeObj).longValue();
        }
        if(sizeObj instanceof Long){
            return (Long)sizeObj;
        }
        return 0L;
    }

    public void saveDownloadCode(String code, DownloadFileDto fileDto) {
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD_CODE+code,fileDto,Constants.REDIS_KEY_EXPIRES_ONE_MIN*10);
    }
    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD_CODE+code);
    }

    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
    }

    public UserSpaceDto  resetUserSpaceUse(String userId) {
        UserSpaceDto spaceDto=new UserSpaceDto();
        Long useSpace=fileInfoMapper.selectUseSpace(userId);
        spaceDto.setUseSpace(useSpace);
//        UserInfo userInfo= infoMapper.selectByUserId(userId);
        LambdaQueryWrapper<UserInfo> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId,userId);
        UserInfo userInfo= infoMapper.selectOne(queryWrapper);
        spaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,spaceDto,Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return spaceDto;
    }
}
