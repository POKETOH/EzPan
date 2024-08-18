package com.easypan.entity.enums;

public enum FileStatusEnums {
    TRANSFER(0,"转码中"),
    TRSNSFER_FALL(1,"转码失败"),
    USING(2,"使用中"),
    RECOVERY(3,"回收站"),
    DEL(4,"已删除");
    private Integer status;
    private String desc;
    FileStatusEnums(Integer status,String desc){
        this.status=status;
        this.desc=desc;
    }
    public Integer getStatus(){
        return status;
    }public String getDesc(){
        return desc;
    }
}
