package com.easypan.service;


import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.ShareQuery;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ShareInfoVO;

import java.util.List;
/**
 * @Description:  Service
 * @Author: false
 * @Date: 2024/06/27 16:01:13
 */
public interface ShareService{

	/**
 	 * 根据条件查询列表
 	 */
	List<FileShare> findListByParam(ShareQuery query);

	/**
 	 * 根据条件查询数量
 	 */
	Integer findCountByParam(ShareQuery query);

	/**
 	 * 分页查询
 	 */
	PaginationResultVO<FileShare> findListByPage(ShareQuery query);

	/**
 	 * 新增
 	 */
	Integer add(FileShare bean);

	/**
 	 * 批量新增
 	 */
	Integer addBatch(List<FileShare> listBean);

	/**
 	 * 批量新增或修改
 	 */
	Integer addOrUpdateBatch(List<FileShare> listBean);

	void saveShare(FileShare fileShare);

	void delFileShareBatch(String userId, String[] fileIds);
	FileShare getFileShareByShareId(String shareId);

	SessionShareDto checkShareCode(String shareId, String code);

}