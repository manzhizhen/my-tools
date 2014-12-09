package com.sfpay.openapi.config;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.sfpay.openapi.model.MethodBean;
import com.sfpay.openapi.model.ServiceBean;

/**
 * 服务管理类
 * 
 * @author 625288
 * 2014年10月4日
 */
@Service
public class ServiceManager {
	// 服务Bean列表
	private CopyOnWriteArrayList<ServiceBean> serviceBeanList = 
			new CopyOnWriteArrayList<ServiceBean>();
	// 服务名称与方法实体Bean的映射关系（key-【服务名-版本号】 value-【方法实体Bean】）
	private ConcurrentMap<String, MethodBean> serviceRegisterMap = 
			new ConcurrentHashMap<String, MethodBean>(50);

	static {
		
	}
	
	public Map<String, MethodBean> getServiceRegisterMap() {
		return serviceRegisterMap;
	}

	public void setServiceRegisterMap(
			ConcurrentMap<String, MethodBean> serviceRegisterMap) {
		this.serviceRegisterMap = serviceRegisterMap;
	}

	public CopyOnWriteArrayList<ServiceBean> getServiceBeanList() {
		return serviceBeanList;
	}

	public void setServiceBeanList(
			CopyOnWriteArrayList<ServiceBean> serviceBeanList) {
		this.serviceBeanList = serviceBeanList;
	}
	
	/**
	 * 服务有效性检查
	 */
	public void serviceEffectiveCheck() {
		if(serviceRegisterMap == null) {
			// TODO 打印日志
			return ;
		}
		
		Date todate = new Date();
		Date startDate = null;
		Date endDate = null;
		MethodBean methodBean = null;
		
		for(Map.Entry<String, MethodBean> entry : serviceRegisterMap.entrySet()) {
			methodBean = entry.getValue();
			startDate = methodBean.getStartDate();
			endDate = methodBean.getEndDate();
			
			if(todate.compareTo(startDate) < 0 || (endDate != null && todate.compareTo(endDate) > 0)) {
				methodBean.setEnable(false);
			} else {
				methodBean.setEnable(true);
			}
		}
		
		
	}
}
