package com.sfpay.openapi.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caucho.hessian.client.HessianProxyFactory;
import com.sfpay.openapi.exception.ServiceException;
import com.sfpay.openapi.model.AttributeBean;
import com.sfpay.openapi.model.MethodBean;
import com.sfpay.openapi.model.MethodParamBean;
import com.sfpay.openapi.model.ServiceBean;

/**
 * 控制器辅助类
 * @author 625288
 *
 * 2014-10-28
 */
@Service
public class ControllerHelper {
	@Autowired
	private ServiceManager serviceRegisterFactory;
	
	@Autowired
	private CallServiceCache callServiceCache;
	
	private HessianProxyFactory factory = new HessianProxyFactory();
	
	/**
	 * 调用服务
	 * @param serviceName	服务名称
	 * @param callParamsMap 调用者传入的参数Map
	 * @return
	 * @throws ServiceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object callService(String serviceName, Map<String, String> callParamsMap) throws ServiceException {
		try {
			MethodBean methodBean = serviceRegisterFactory.getServiceRegisterMap().get(String.format("%s-%s", 
					new Object[]{serviceName, callParamsMap.get("version")}));
			if(methodBean == null) {
				throw new ServiceException(String.format("服务调用失败,不存在的服务名和版本：%s", serviceName));
			} else if(!methodBean.isEnable()) {
				throw new ServiceException(String.format("服务调用失败,服务尚未开启：%s", serviceName));
			}
			
			ServiceBean serviceBean = methodBean.getServiceBean();
			String url = serviceBean.getUrl();
			Class clazz = serviceBean.getClazz();
					
			Object proxyObj = callServiceCache.getProxyObj(serviceName);
			if(proxyObj == null) {
				proxyObj = factory.create(clazz, url);
			}
			
			if(proxyObj != null) {
				callServiceCache.getProxyObjMap().putIfAbsent(serviceName, proxyObj);
			} else {
				throw new ServiceException(String.format("%s服务调用创建代理类失败！", serviceName));
			}
			
			List<MethodParamBean> methodParamBeanList = methodBean.getParamList();
			
			// 检查传入参数是否正确
			Set<String> callParamSet = new HashSet<String>(callParamsMap.size());
			for(MethodParamBean methodParamBean : methodParamBeanList) {
				for(Map.Entry<String, AttributeBean> entry : methodParamBean.getParamAttributeMap().entrySet()) {
					if(StringUtils.isNotBlank(entry.getValue().getCall())) {
						callParamSet.add(entry.getValue().getCall());
					}
				}
			}
			if(!callParamsMap.keySet().containsAll(callParamSet)) {
				throw new ServiceException(String.format("调用%s服务失败,传入参数不正确!", serviceName));
			}
			
			int paramNum = methodParamBeanList.size();
			Class[] classArray = new Class[paramNum];
			List<Object> paramList = new ArrayList<Object>();
			int index = 0;
			Class paramClass = null;
			for(MethodParamBean methodParamBean : methodParamBeanList) {
//				paramClass = sfpayOpenApiClassLoader.loadClass(methodParamBean.getType());
				paramClass = methodParamBean.getType();
				classArray[index++] = paramClass;
				
				// 如果是基本类型,则可以直接处理,因为其下没有子节点
				if(ObjectSwitchTool.isBaseType(paramClass)) {
					paramList.add(ObjectSwitchTool.switchToBase(paramClass, methodParamBean.isCalled() ? 
							callParamsMap.get(methodParamBean.getCall()) : methodParamBean.getDefaultValue()));
					
				// 如果不是基本类型，则其下有可能有子节点
				} else {
					Map<String, AttributeBean> paramAttriMap = methodParamBean.getParamAttributeMap();
					Map<String, String> paramFieldValueMap = new HashMap<String, String>();
					for(Map.Entry<String, AttributeBean> entry : paramAttriMap.entrySet()) {
						// key为方法参数对象的XPath路径，值为对应的取值
						paramFieldValueMap.put(entry.getKey(), entry.getValue().isCalled() ? 
								callParamsMap.get(entry.getValue().getCall()) : entry.getValue().getDefaultValue());
					}
					// 组装参数对象
					paramList.add(ObjectSwitchTool.createParamObject(paramClass, paramFieldValueMap));
				}
			}
			
			// 获取调用服务的方法对象
			Method method = callServiceCache.getMethod(serviceName);
			if(method == null) {
				method = clazz.getDeclaredMethod(methodBean.getName(), classArray);
			}
			
			return method == null ? null : method.invoke(proxyObj, paramList.toArray());
		} catch (ServiceException e) {
			// TODO 打印错误日志
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			// TODO 打印错误日志
			e.printStackTrace();
			throw new ServiceException(String.format("调用%服务失败！", serviceName), e);
		} 
	}

}
