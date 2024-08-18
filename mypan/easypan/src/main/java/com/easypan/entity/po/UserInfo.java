package com.easypan.entity.po;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.easypan.entity.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description: 
 * @Author: false
 * @Date: 2024/06/08 21:49:50
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_info")
public class UserInfo implements Serializable {
	/**
 	 * 
 	 */
	@TableId
	private String userId;

	/**
 	 * 
 	 */
	private String nickName;


	/**
 	 * 
 	 */
	private String password;

	/**
 	 * 
 	 */
	private String email;

	/**
 	 * 
 	 */
	private Long useSpace;

	/**
 	 * 
 	 */
	private Long totalSpace;

	/**
 	 * 
 	 */
	private String avatar;

	/**
 	 * 
 	 */
	private String qqOpenId;

	/**
 	 * 
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date joinTime;

	/**
 	 * 
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
 	 * 
 	 */
	@JsonIgnore
	private Integer status;


	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}




	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setUseSpace(Long useSpace) {
		this.useSpace = useSpace;
	}

	public Long getUseSpace() {
		return useSpace;
	}

	public void setTotalSpace(Long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public Long getTotalSpace() {
		return totalSpace;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setQqOpenId(String qqOpenId) {
		this.qqOpenId = qqOpenId;
	}

	public String getQqOpenId() {
		return qqOpenId;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public Date getJoinTime() {
		return joinTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}
	@Override
	public String toString() {
		return ":" + (userId == null ? "空" : userId) + "," + 
				":" + (nickName == null ? "空" : nickName) + "," +
				":" + (password == null ? "空" : password) + "," + 
				":" + (email == null ? "空" : email) + "," + 
				":" + (useSpace == null ? "空" : useSpace) + "," + 
				":" + (totalSpace == null ? "空" : totalSpace) + "," +
				":" + (avatar == null ? "空" : avatar) + "," + 
				":" + (qqOpenId == null ? "空" : qqOpenId) + "," + 
				":" + (joinTime == null ? "空" : DateUtils.format(joinTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				":" + (lastLoginTime == null ? "空" : DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				":" + (status == null ? "空" : status);
		}
}