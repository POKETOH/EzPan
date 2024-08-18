package com.easypan.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }
    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }
    public static String encodeByMd5(String originStirng){
        return isEmpty(originStirng)?null: DigestUtils.md5Hex(originStirng);
    }
    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    public static String rename(String fileName) {
        String fileNameReal=getFileNameNoSuffix(fileName);
        String suffix=getFileSuffix(fileName);
        return fileNameReal+"_"+getRandomNumber(5)+suffix;
    }

    public static String getFileSuffix(String fileName) {
        Integer index=fileName.lastIndexOf(".");
        if(index==-1)
            return "";
        fileName=fileName.substring(index,fileName.length());
        return fileName;
    }

    public static String getFileNameNoSuffix(String fileName) {
        Integer index=fileName.lastIndexOf(".");
        if(index==-1)return fileName;
        fileName=fileName.substring(0,index);
        return fileName;
    }


}
