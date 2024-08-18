package com.easypan.service;


import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.vo.PaginationResultVO;

/**
 * @Description:  Service
 * @Author: false
 * @Date: 2024/06/11 22:25:02
 */
public interface EmailCodeService extends IService<EmailCode> {
	void sendEmailCode(String email,Integer type);
	void checkCode(String email,String code);
}