package com.easypan.service.impl;


import java.util.Date;
import java.util.List;

import com.easypan.entity.dto.SessionShareDto;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.enums.PageSize;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.enums.ShareValidTypeEnums;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.po.FileShare;
import com.easypan.entity.query.ShareQuery;
import com.easypan.entity.query.SimplePage;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ShareInfoVO;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.ShareMapper;
import com.easypan.service.ShareService;
import com.easypan.utils.DateUtils;
import com.easypan.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
/**
 * @Description:  业务接口实现
 * @Author: false
 * @Date: 2024/06/27 16:01:13
 */
@Service("ShareMapper")
public class ShareServiceImpl implements ShareService {

	@Resource
	private ShareMapper<FileShare, ShareQuery> shareMapper;

	/**
 	 * 根据条件查询列表
 	 */
	@Override
	public List<FileShare> findListByParam(ShareQuery query) {
		return this.shareMapper.selectList(query);	}

	/**
 	 * 根据条件查询数量
 	 */
	@Override
	public Integer findCountByParam(ShareQuery query) {
		return this.shareMapper.selectCount(query);	}

	/**
 	 * 分页查询
 	 */
	@Override
	public PaginationResultVO<FileShare> findListByPage(ShareQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileShare> list = this.findListByParam(query);
		PaginationResultVO<FileShare> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
 	 * 新增
 	 */
	@Override
	public Integer add(FileShare bean) {
		return this.shareMapper.insert(bean);
	}

	/**
 	 * 批量新增
 	 */
	@Override
	public Integer addBatch(List<FileShare> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.shareMapper.insertBatch(listBean);
	}

	/**
 	 * 批量新增或修改
 	 */
	@Override
	public Integer addOrUpdateBatch(List<FileShare> listBean) {
		if ((listBean == null) || listBean.isEmpty()) {
			return 0;
		}
			return this.shareMapper.insertOrUpdateBatch(listBean);
	}

	@Override
	public void saveShare(FileShare fileShare) {
		//检查分享数据状态
		if(fileShare.getValidType()==null){
			throw  new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//如果不是永久就需要设置过期时间
		if(fileShare.getValidType()!= ShareValidTypeEnums.FOREVER.getType()){
			fileShare.setExpireTime(DateUtils.getAfterDate(fileShare.getValidType()));
		}
		fileShare.setShareTime(new Date());
		if(fileShare.getCode()==null){
			String code= StringTools.getRandomNumber(5);
			fileShare.setCode(code);
		}
		fileShare.setShareId(StringTools.getRandomNumber(5));
		fileShare.setShowCount(0);
		//插入数据库
		shareMapper.insert(fileShare);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileShareBatch(String userId, String[] fileIds) {
		Integer count=shareMapper.deleteFileShareBatch(fileIds,userId);
		if(count!=fileIds.length)
		{
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}
	@Override
	public FileShare getFileShareByShareId(String shareId) {
		return this.shareMapper.selectByShareId(shareId);
	}

	@Override
	public SessionShareDto checkShareCode(String shareId, String code) {
		//获取share数据
		FileShare fileShare=shareMapper.selectByShareId(shareId);
		//判断是否过期
		if(fileShare==null||(fileShare.getExpireTime()!=null&&new Date().after(fileShare.getExpireTime()))){
			throw new BusinessException(ResponseCodeEnum.CODE_902);
		}
		//判断提取码是否正确
		if(!fileShare.getCode().equals(code)){
			throw new BusinessException("分享码错误");
		}
		//更新浏览次数
		shareMapper.updateShareShowCount(shareId);
		SessionShareDto sessionShareDto=new SessionShareDto();
		sessionShareDto.setShareUserId(shareId);
		sessionShareDto.setShareUserId(fileShare.getUserId());
		sessionShareDto.setFileId(fileShare.getFileId());
		sessionShareDto.setShareId(shareId);
		sessionShareDto.setExpireTime(fileShare.getExpireTime());
		return sessionShareDto;
	}




}