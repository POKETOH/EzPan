package com.easypan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
    boolean checkParams() default false;
    //检查是否登录
    boolean checkLogin() default true;
    //检查是否为管理员
    boolean checkAdmin() default false;
}


