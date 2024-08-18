package com.easypan.entity.po;

import java.io.Serializable;

import java.util.Date;

import com.easypan.entity.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 
 * @Author: false
 * @Date: 2024/06/27 16:01:13
 *
 */
@Data
public class FileShare implements Serializable {
	/**
 	 * 
 	 */
	private String shareId;

	/**
 	 * 
 	 */
	private String fileId;

	/**
 	 * 
 	 */
	private String userId;

	/**
 	 * 
 	 */
	private Integer validType;

	/**
 	 * 
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

	/**
 	 * 
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date shareTime;

	/**
 	 * 
 	 */
	private String code;

	/**
 	 * 
 	 */
	private Integer showCount;
	private String fileName;

	/**
	 * 0:文件 1:目录
	 */
	private Integer folderType;

	/**
	 * 1:视频 2:音频  3:图片 4:文档 5:其他
	 */
	private Integer fileCategory;

	/**
	 * 1:视频 2:音频  3:图片 4:pdf 5:doc 6:excel 7:txt 8:code 9:zip 10:其他
	 */
	private Integer fileType;

	/**
	 * 封面
	 */
	private String fileCover;


	public void setShareId(String shareId) {
		this.shareId = shareId;
	}

	public String getShareId() {
		return shareId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setValidType(Integer validType) {
		this.validType = validType;
	}

	public Integer getValidType() {
		return validType;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setShareTime(Date shareTime) {
		this.shareTime = shareTime;
	}

	public Date getShareTime() {
		return shareTime;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setShowCount(Integer showCount) {
		this.showCount = showCount;
	}

	public Integer getShowCount() {
		return showCount;
	}
	@Override
	public String toString() {
		return ":" + (shareId == null ? "空" : shareId) + "," + 
				":" + (fileId == null ? "空" : fileId) + "," + 
				":" + (userId == null ? "空" : userId) + "," + 
				":" + (validType == null ? "空" : validType) + "," + 
				":" + (expireTime == null ? "空" : DateUtils.format(expireTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				":" + (shareTime == null ? "空" : DateUtils.format(shareTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," +
				":" + (code == null ? "空" : code) + "," + 
				":" + (showCount == null ? "空" : showCount);
		}
}