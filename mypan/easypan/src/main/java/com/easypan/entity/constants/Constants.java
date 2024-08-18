package com.easypan.entity.constants;

public class Constants {
    public static final Integer ZERO =0;
    public static final String ZERO_STRING ="0";
    public static final Integer Length_5 =5;
    public static final Integer Length_10 =10;
    public static final Integer LENGTH_150 =150;
    public static final String CHECK_CODE_KEY="check_code_key";
    public static final String CHECK_CODE_KEY_EMAIL="check_code_key_email";
    public static final String SESSION_KEY="session_key";
    public static final String REDIS_KEY_SETTING="easy:syssetting:";
    public static final String SESSION_SHARE_KEY = "session_share_key_";
    public static final Long MB=1024*1024L;
    public static final String IMAGE_PNG_SUFFIX = ".png";
    //文件默认存放目录
    public static final String FILE_FOLDER_FILE="/file/";
    //头像默认存放目录
    public static final String FILE_FOLDER_AVARAE_NAME="avatar/";
    public static final String TEMP_FILE_FOLDER="/temp/";
    public static final String AVARAE_SUFFIX=".jpg";
    public static final String AVARAE_DEFAULT="default";
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN=60;
    public static final Integer REDIS_KEY_EXPIRES_ONE_DAY=REDIS_KEY_EXPIRES_ONE_MIN*50*24;
    public static final Integer REDIS_KEY_EXPIRES_ONE_HOUR=REDIS_KEY_EXPIRES_ONE_MIN*50;
    public static final String REDIS_KEY_SYS_SETTING="easypan:syssetting:";
    public static final String REDIS_KEY_USER_SPACE_USE="easypan:user:spaceuse:";
    public static final String REDIS_KEY_USER_FILE_TEMP_SIZE="easypan:user:temp:size";
    public static final String REDIS_KEY_DOWNLOAD_CODE="easypan:file:download:code";
    public static final String TS_NAME="index.ts";
    public static final String M3U8_NAME="index.m3u8";
}
