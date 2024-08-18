package com.easypan.entity.po;

import java.io.Serializable;

import com.easypan.entity.enums.DateTimePatternEnum;
import com.easypan.utils.DateUtils;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @Description: 
 * @Author: false
 * @Date: 2024/06/11 22:25:02
 */
public class EmailCode implements Serializable {
	/**
 	 * 
 	 */
	private String email;

	/**
 	 * 
 	 */
	private String code;

	/**
 	 * 
 	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
 	 * 
 	 */
	@JsonIgnore
	private Integer status;


	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}
	@Override
	public String toString() {
		return ":" + (email == null ? "空" : email) + "," + 
				":" + (code == null ? "空" : code) + "," + 
				":" + (createTime == null ? "空" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "," + 
				":" + (status == null ? "空" : status);
		}
}