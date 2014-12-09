package com.sfpay.openapi.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * 方法参数的属性Bean
 * 
 * @author 625288
 * 
 *         2014年10月22日
 */

public class AttributeBean implements Serializable{
	private String call;
	private String match;
	private String defaultValue;
	
	private static final long serialVersionUID = 1L;

	public AttributeBean() {
	}
	
	public AttributeBean(String match, String call, String defaultValue) {
		this.match = match;
		this.call = call;
		this.defaultValue = defaultValue;
	}

	/**
	 * 该方法参数属性是否由调用来传值
	 * @return
	 */
	public boolean isCalled() {
		return StringUtils.isNotBlank(call);
	}
	
	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
