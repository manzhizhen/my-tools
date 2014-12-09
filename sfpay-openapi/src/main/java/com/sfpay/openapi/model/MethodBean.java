package com.sfpay.openapi.model;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 方法实体Bean
 * 
 * @author 625288
 * 
 *         2014年10月5日
 */
@SuppressWarnings("rawtypes")
public class MethodBean {
	private String name; 		// 方法名称
	private String call;		// 调用名称
	private String desc;		// 调用描述
	private String version;		// 版本号
	
	// 注意：方法Bean的生效时间和结束时间不填写的话，默认和其服务节点保持一致
	private Date startDate;		// 生效日期
	private Date endDate;		// 结束日期
	
	private CopyOnWriteArrayList<MethodParamBean> paramList = 
				new CopyOnWriteArrayList<MethodParamBean>();	 // 方法参数的List
	private Class returnType; // 返回值类型
	
	private ServiceBean serviceBean; // 所属的服务实体Bean
	
	private boolean enable;	// 是否启用

	public MethodBean() {
	}

	public MethodBean(String methodName, String callName, String version, String desc, CopyOnWriteArrayList<MethodParamBean> paramList, 
			Class returnType, Date startDate, Date endDate, ServiceBean serviceBean) {
		this.name = methodName;
		this.call = callName;
		this.version = version;
		this.desc = desc;
		this.paramList = paramList;
		this.returnType = returnType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.serviceBean = serviceBean;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String methodName) {
		this.name = methodName;
	}

	public ServiceBean getServiceBean() {
		return serviceBean;
	}

	public void setServiceBean(ServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public String getCall() {
		return call;
	}

	public void setCall(String callName) {
		this.call = callName;
	}

	public CopyOnWriteArrayList<MethodParamBean> getParamList() {
		return paramList;
	}
	
	public void setParamList(CopyOnWriteArrayList<MethodParamBean> paramList) {
		this.paramList = paramList;
	}

	public Class getReturnType() {
		return returnType;
	}

	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
