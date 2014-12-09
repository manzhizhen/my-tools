package com.sfpay.openapi.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 函数的参数Bean
 * 其中包含暴露给外部的fields，
 * 暴露给外部的field和调用函数的参数并不一定是一一对应的。
 * @author 625288
 *
 * 2014年10月6日
 */
@SuppressWarnings("rawtypes")
public class MethodParamBean {
	private String call; 	// 暴露给外部的field名称，只有当type为基本类型时，才需要填写call，否则只需要填充callParamMap
//	private String type;	// 参数的类型
	private Class type;
	private String defaultValue; 	// 参数默认值
	// 方法参数属性赋值策略Map,key-属性XPath名称，value-对应方法参数的XPath
	private Map<String, AttributeBean> paramAttributeMap = new HashMap<String, AttributeBean>(); 
	
	public MethodParamBean() {
	}
	
	public MethodParamBean(Class type) {
		this.type = type;
	}
	
	public MethodParamBean(Class type, Map<String, AttributeBean> paramAttributeMap) {
		this.type = type;
		this.paramAttributeMap = paramAttributeMap;
	}
	
	public MethodParamBean(Class type, String call) {
		this.type = type;
		this.call = call;
	} 

	/**
	 * 该方法参数是否由调用来传值
	 * @return
	 */
	public boolean isCalled() {
		return StringUtils.isNotBlank(call);
	}
	
	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Map<String, AttributeBean> getParamAttributeMap() {
		return paramAttributeMap;
	}

	public void setParamAttributeMap(Map<String, AttributeBean> paramAttributeMap) {
		this.paramAttributeMap = paramAttributeMap;
	}

	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
