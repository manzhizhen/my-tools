package com.sfpay.openapi.config;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

/**
 * 服务调用缓存类
 * @author 625288
 *
 * 2014年10月22日
 */
@Service
public class CallServiceCache {
	// 服务名到方法对象的映射关系
	private ConcurrentMap<String, Method> methodMap = new ConcurrentHashMap<String, Method>();
	// 服务名到代理对象的映射
	private ConcurrentMap<String, Object> proxyObjMap = new ConcurrentHashMap<String, Object>();
	
	public void updateCache() {
		
	}
	
	
	/**
	 * 获取一个方法对象
	 * @param serviceName
	 * @return
	 */
	public Method getMethod(String serviceName) {
		if(serviceName == null) {
			return null;
		}
		
		return methodMap.get(serviceName);
	}
	
	/**
	 * 获取一个代理对象
	 * @param serviceName
	 * @return
	 */
	public Object getProxyObj(String serviceName) {
		if(serviceName == null) {
			return null;
		}
		
		return proxyObjMap.get(serviceName);
	}
	
	/**
	 * 清除缓存数据
	 */
	public void clearAll() {
		methodMap.clear();
		proxyObjMap.clear();
	}

	public ConcurrentMap<String, Method> getMethodMap() {
		return methodMap;
	}

	public ConcurrentMap<String, Object> getProxyObjMap() {
		return proxyObjMap;
	}
}
