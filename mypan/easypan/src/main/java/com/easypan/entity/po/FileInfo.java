package com.easypan.entity.po;

import java.io.Serializable;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.easypan.entity.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description: 
 * @Author: false
 * @Date: 2024/06/18 11:25:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file_info")
public class FileInfo implements Serializable {
	/**
 	 * 文件ID
 	 */
	@TableId
	private String fileId;

	@TableField(exist = false)
	private String[] fileIdArray;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	/**
 	 * 用户ID
 	 */
	private String userId;

	/**
 	 * 文件MD5值
 	 */
	private String fileMd5;

	/**
 	 * 父级ID
 	 */
	private String filePid;

	/**
 	 * 文件大小
 	 */
	private Long fileSize;

	/**
 	 * 文件名
 	 */
	private String fileName;

	/**
 	 * 封面
 	 */
	private String fileCover;

	/**
 	 * 文件路径
 	 */
	private String filePath;
	@TableField(exist = false)
	private String nickName;


	/**
 	 * 创建时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
 	 * 最后更新时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;

	/**
 	 * 0文件 1目录
 	 */
	private Integer folderType;

	/**
 	 * 1视频 2音频 3图片 4文档 5其他
 	 */
	private Integer fileCategory;

	/**
 	 * 1视频 2音频 3图片 4pdf 5doc 6excel 7txt 8code 9zip 10其他 
 	 */
	private Integer fileType;

	/**
 	 * 0转码中 1转码失败 2转码成功
 	 */
	@JsonIgnore
	private Integer status;

	/**
 	 * 进入回收站时间
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recoveryTime;

	/**
 	 * 0删除 1回收站 2正常
 	 */
	private Integer delFlag;


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

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFilePid(String filePid) {
		this.filePid = filePid;
	}

	public String getFilePid() {
		return filePid;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileCover(String fileCover) {
		this.fileCover = fileCover;
	}

	public String getFileCover() {
		return fileCover;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setFolderType(Integer folderType) {
		this.folderType = folderType;
	}

	public Integer getFolderType() {
		return folderType;
	}

	public void setFileCategory(Integer fileCategory) {
		this.fileCategory = fileCategory;
	}

	public Integer getFileCategory() {
		return fileCategory;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}

	public Integer getFileType() {
		return fileType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setRecoveryTime(Date recoveryTime) {
		this.recoveryTime = recoveryTime;
	}

	public Date getRecoveryTime() {
		return recoveryTime;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public Integer getDelFlag() {
		return delFlag;
	}
	@Override
	public String toString() {
		return "文件ID:" + (fileId == null ? "空" : fileId) + "," + 
				"用户ID:" + (userId == null ? "空" : userId) + "," + 
				"文件MD5值:" + (fileMd5 == null ? "空" : fileMd5) + "," + 
				"父级ID:" + (filePid == null ? "空" : filePid) + "," + 
				"文件大小:" + (fileSize == null ? "空" : fileSize) + "," + 
				"文件名:" + (fileName == null ? "空" : fileName) + "," + 
				"封面:" + (fileCover == null ? "空" : fileCover) + "," + 
				"文件路径:" + (filePath == null ? "空" : filePath) + "," + 
				"创建时间:" + (createTime == null ? "空" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," +
				"最后更新时间:" + (lastUpdateTime == null ? "空" : DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				"0文件 1目录:" + (folderType == null ? "空" : folderType) + "," + 
				"1视频 2音频 3图片 4文档 5其他:" + (fileCategory == null ? "空" : fileCategory) + "," + 
				"1视频 2音频 3图片 4pdf 5doc 6excel 7txt 8code 9zip 10其他 :" + (fileType == null ? "空" : fileType) + "," + 
				"0转码中 1转码失败 2转码成功:" + (status == null ? "空" : status) + "," + 
				"进入回收站时间:" + (recoveryTime == null ? "空" : DateUtils.format(recoveryTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," +
				"0删除 1回收站 2正常:" + (delFlag == null ? "空" : delFlag);
		}
}