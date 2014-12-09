
package com.sfpay.openapi.model;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务实体Bean
 * 
 * @author 625288
 * 
 *         2014年10月4日
 */
@SuppressWarnings("rawtypes")
public class ServiceBean {
//	private String className; 	// 类名(包名+类名)
	private Class clazz;
	private String url;			// 远程调用地址
//	private String version;		// 版本号
	
	private Date startDate;		// 生效日期
	private Date endDate;		// 结束日期
	
	private CopyOnWriteArrayList<MethodBean> methodBeans = new CopyOnWriteArrayList<MethodBean>();
	
	public ServiceBean() {
	}
	
	public ServiceBean(Class clazz, String url, Date startDate, Date endDate) {
		this.clazz = clazz;
		this.url = url;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public CopyOnWriteArrayList<MethodBean> getMethodBeans() {
		return methodBeans;
	}

	public void setMethodBeans(CopyOnWriteArrayList<MethodBean> methodBeans) {
		this.methodBeans = methodBeans;
	}
}
