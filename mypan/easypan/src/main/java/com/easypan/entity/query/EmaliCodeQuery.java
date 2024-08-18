package com.easypan.entity.query;

import com.easypan.entity.query.BaseQuery;

import java.util.Date;


/**
 * @Description: 
 * @Author: false
 * @Date: 2024/06/11 22:25:02
 */
public class EmaliCodeQuery extends BaseQuery {
	/**
 	 *  查询对象
 	 */
	private String email;

	private String emailFuzzy;

	/**
 	 *  查询对象
 	 */
	private String code;

	private String codeFuzzy;

	/**
 	 *  查询对象
 	 */
	private Date createTime;

	private String createTimeStart;
	private String createTimeEnd;
	/**
 	 *  查询对象
 	 */
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

	public void setEmailFuzzy(String emailFuzzy) {
		this.emailFuzzy = emailFuzzy;
	}

	public String getEmailFuzzy() {
		return emailFuzzy;
	}

	public void setCodeFuzzy(String codeFuzzy) {
		this.codeFuzzy = codeFuzzy;
	}

	public String getCodeFuzzy() {
		return codeFuzzy;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart() {
		return createTimeStart;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd() {
		return createTimeEnd;
	}
}