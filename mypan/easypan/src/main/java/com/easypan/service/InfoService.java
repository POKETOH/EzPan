package com.easypan.service;


import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.query.UserInfoQuery;
/**
 * @Description:  Service
 * @Author: false
 * @Date: 2024/06/08 21:49:50
 */
public interface InfoService extends IService<UserInfo> {

    SessionWebUserDto login(String email, String password) ;

    void register(String email, String nickName, String password, String emailCode, String checkCode) ;

//    /**
// 	 * 根据条件查询列表
// 	 */
//	List<UserInfo> findListByParam(UserInfoQuery query);



	void resetPwd(String email, String password, String emailCode, String checkCode);

    void changeUserSpace(String userId, Integer changeSpace);

	void updateUserStatus(String userId, Integer status);

    PaginationResultVO getUserList(UserInfoQuery userInfoQuery);
}