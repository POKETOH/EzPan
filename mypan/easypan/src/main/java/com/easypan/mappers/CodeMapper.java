package com.easypan.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easypan.entity.po.EmailCode;
import org.apache.ibatis.annotations.Param;

/**
 * 邮箱验证码 数据库操作接口
 */
public interface CodeMapper extends BaseMapper<EmailCode> {

    /**
     * 根据EmailAndCode删除
     */
    Integer deleteByEmailAndCode(@Param("email") String email, @Param("code") String code);


    /**
     * 根据EmailAndCode获取对象
     */
    EmailCode selectByEmailAndCode(@Param("email") String email, @Param("code") String code);

    void disableEmailCode(@Param("email") String email);

}
