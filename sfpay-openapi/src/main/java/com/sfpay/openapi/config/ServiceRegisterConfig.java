package com.sfpay.openapi.config;

import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caucho.hessian.client.HessianProxyFactory;
import com.sfpay.openapi.exception.ServiceException;
import com.sfpay.openapi.model.AttributeBean;
import com.sfpay.openapi.model.MethodBean;
import com.sfpay.openapi.model.MethodParamBean;
import com.sfpay.openapi.model.ServiceBean;

/**
 * 服务注册文件配置类
 * @author 625288
 *
 * 2014年10月4日
 */
@Service("serviceRegisterConfig")
public class ServiceRegisterConfig{
//	@Value(value="${PROPERTIES_FILE_PATH}")
//	private String propertiesFilePath;
	
	// 服务调用缓存
	@Autowired
	private CallServiceCache callServiceCache;
	@Autowired
	private ServiceManager serviceManager;
	private HessianProxyFactory factory = new HessianProxyFactory();
	
	/**
	 * 更新和加载服务注册文件
	 * @throws ServiceException
	 */
	@PostConstruct
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void updateServiceRegister() throws ServiceException{
//		File xmlFile = new File(propertiesFilePath, "service-register.xml");
		File xmlFile = new File(getClass().getClassLoader().getResource("").getPath(), "config" + File.separator + "service-register.xml");
		
		if(!xmlFile.isFile()) {
			throw new ServiceException(String.format("读取服务注册文件失败：%s文件不存在", xmlFile.getAbsolutePath()));
		}
		
		SAXReader saxReader = new SAXReader();  
		try {
			CopyOnWriteArrayList<ServiceBean> newServiceBeanList = 
					new CopyOnWriteArrayList<ServiceBean>();
			ConcurrentMap<String, MethodBean> newServiceRegisterMap = 
					new ConcurrentHashMap<String, MethodBean>(50);
			
			Map<String, Class> refersMap = new HashMap<String, Class>();
			
			Document doc = saxReader.read(xmlFile);
			Element rootE = doc.getRootElement();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");	// 格式化日期
			Pattern datePattern = Pattern.compile("[2-9]{1}[0-9]{7}");	// 校验日期格式
			
			// 更新前先把服务调用缓存清空
			callServiceCache.clearAll();
			
			// --------------------- 【解析refer节点】 ---------------------
			List<Element> referEList = rootE.elements("refer");
			String referName = null;
			String referClass = null;
			if(referEList != null && !referEList.isEmpty()) {
				for(Element referE : referEList) {
					referName = referE.attributeValue("name");
					referClass = referE.attributeValue("className");
					
					if(StringUtils.isBlank(referName)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException("updateServiceRegister失败：refer节点的name不能为空！");
					}
					if(StringUtils.isBlank(referClass)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException("updateServiceRegister失败：refer节点的class不能为空！");
					}
					if(refersMap.containsKey(referName)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException(String.format("updateServiceRegister失败：refer节点的name不能重复！", referName));
					}
					
					refersMap.put(referName, ObjectSwitchTool.getClassForName(referClass));
				}
			}
			
			// --------------------- 【解析service节点】 ---------------------
			List<Element> serviceEList = rootE.elements("service");
			if(serviceEList != null && !serviceEList.isEmpty()) {
				String className = null;
				String url = null;
				String startDate = null;
				String endDate = null;
				ServiceBean serviceBean = null;
				MethodBean methodBean = null;
//				List<MethodBean> methodBeanList = null;
				CopyOnWriteArrayList<MethodParamBean> paramList = null;
				String returnType = null;
				for(Element serviceE : serviceEList) {
					// 解析service节点
					className = serviceE.attributeValue("className");	// 服务接口类全名
					url = serviceE.attributeValue("url");				// 服务接口远程Hessian地址
					startDate = serviceE.attributeValue("startDate");	// 生效日期，必填
					endDate = serviceE.attributeValue("endDate");		// 失效日期，不填则永久有效
					
					if(StringUtils.isBlank(className)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException("updateServiceRegister失败：service节点的class不能为空！");
					}
					if(StringUtils.isBlank(url)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException(String.format("updateServiceRegister失败：%s service节点的url不能为空！", className));
					}
					if(StringUtils.isBlank(startDate)) {
						//TODO 这里打印错误日志。。
						throw new ServiceException(String.format("updateServiceRegister失败：%s service节点的startDate不能为空！", className));
					}
					
					
					if(!datePattern.matcher(startDate).matches()) {
						//TODO 这里打印错误日志。。
						throw new ServiceException(String.format("updateServiceRegister失败：%s service节点的startDate格式不正确！", className));
					}
					if(StringUtils.isNotBlank(endDate)) {
						if(!datePattern.matcher(endDate).matches()) {
							//TODO 这里打印错误日志。。
							throw new ServiceException(String.format("updateServiceRegister失败：%s service节点的endDate格式不正确！", className));
						}
					}
					
					// 校验日期是否正确
					if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
						if(endDate.compareTo(startDate) < 0) {
							//TODO 这里打印错误日志。。
							throw new ServiceException(String.format("updateServiceRegister失败：%s service节点的endDate不能小于startDate！", className));
						}
					}
					
					// 创建服务Bean对象
					serviceBean = new ServiceBean(refersMap.containsKey(className) ? refersMap.get(className) : ObjectSwitchTool.
							getClassForName(className), url, dateFormat.parse(startDate), StringUtils.isBlank(endDate) ? 
									null : dateFormat.parse(endDate));
					
					// --------------------- 【解析service下的method节点】 ---------------------
//					methodBeanList = new ArrayList<MethodBean>();
					List<Element> methodEList = serviceE.elements("method");
					// 解析method节点
					if(methodEList != null && !methodEList.isEmpty()) {
						String methodName = null;
						String callName = null;
						String desc = null;
						String version = null;
						String methodStartDate = null;
						String methodEndDate = null;
						for(Element methodE : methodEList) {
							methodName = methodE.attributeValue("name"); 	// 服务接口的方法名
							callName = methodE.attributeValue("call");		// 该方法对外公布的接口名
							version = methodE.attributeValue("version");	
							desc = methodE.attributeValue("desc");			// 该方法对外公布的描述名称
							methodStartDate = methodE.attributeValue("startDate");	// 非必填，不填则默认和服务节点的startDate一致
							methodEndDate = methodE.attributeValue("endDate");		// 非必填，不填则默认和服务节点的endDate一致
							
							if(StringUtils.isBlank(methodName)) {
								//TODO 这里打印错误日志。。
								throw new ServiceException("updateServiceRegister失败：method节点的name不能为空！");
							}
							if(StringUtils.isBlank(callName)) {
								//TODO 这里打印错误日志。。
								throw new ServiceException(String.format("updateServiceRegister失败：method节点 %s 的call不能为空！", methodName));
							}
							if(StringUtils.isBlank(desc)) {
								//TODO 这里打印错误日志。。
								throw new ServiceException(String.format("updateServiceRegister失败：method节点 %s 的desc不能为空！", methodName));
							}
							if(StringUtils.isBlank(version)) {
								//TODO 这里打印错误日志。。
								throw new ServiceException(String.format("updateServiceRegister失败：method节点 %s 的version不能为空！", methodName));
							}
							// 校验生效日期
							if(StringUtils.isNotBlank(methodStartDate)) {
								if(!datePattern.matcher(methodStartDate).matches()) {
									//TODO 这里打印错误日志。。
									throw new ServiceException(String.format("updateServiceRegister失败：method节点 %s 的startDate格式错误！", methodName));
								}
								if(startDate.compareTo(methodStartDate) > 0) {
									//TODO 这里打印错误日志。。
									throw new ServiceException("updateServiceRegister失败：method节点的startDate不能小于service节点的startDate！");
								}
							// 如果生效日期不填，默认和其service节点一致
							} else {
								methodStartDate = startDate;
							}
							// 校验失效日期
							if(StringUtils.isNotBlank(methodEndDate)) {
								if(!datePattern.matcher(methodEndDate).matches()) {
									//TODO 这里打印错误日志。。
									throw new ServiceException(String.format("updateServiceRegister失败：method节点 %s 的endDate格式错误！", methodName));
								}
								
								if(StringUtils.isNotBlank(endDate) && endDate.compareTo(methodEndDate) < 0) {
									//TODO 这里打印错误日志。。
									throw new ServiceException("updateServiceRegister失败：method节点的endDate不能大于service节点的endDate！");
								}
							// 如果失效日期不填，默认和其service节点一致
							} else {
								methodEndDate = endDate;
							}
							
							// --------------------- 【解析method节点下面的param节点】---------------------
							List<Element> paramEList = methodE.elements("param");
							String paramCallName = null, type = null, paramDefaultValue = null;
							paramList = new CopyOnWriteArrayList<MethodParamBean>();
							if(paramEList != null && !paramEList.isEmpty()) {
								for(Element paramE : paramEList) {
									paramCallName = paramE.attributeValue("call"); 	// 该属性不是必须的，只有需要外部提供，才需要该属性
									type = paramE.attributeValue("type");			// 类型，不填默认为java.lang.String
									paramDefaultValue = paramE.attributeValue("default");  // 默认值，没有default也没有call也没有attribute子节点的话，就会赋值基本类型的默认值
									// default和call互斥
									if(StringUtils.isNotBlank(paramCallName) && StringUtils.isNotBlank(paramDefaultValue)) {
										throw new ServiceException("param节点不能既有call属性又有default属性！");
									}
									
									// --------------------- 【解析param节点下面的attribute节点】---------------------
									// attribute节点的作用是给参数对象的属性赋值 默认值或外部传入值
									List<Element> paramAttriEList = paramE.elements("attribute");
									String callParamName = null;
									String callParamMatch = null;
									String defaultValue = null;
									Map<String, AttributeBean> paramAttriMap = new HashMap<String, AttributeBean>();
									Class methodParamClass = StringUtils.isBlank(type) ? String.class : 
										(refersMap.containsKey(type) ? refersMap.get(type) : ObjectSwitchTool.getClassForName(type));
									if(paramAttriEList != null && !paramAttriEList.isEmpty()) {
										// 如果param节点有call属性，其下就不能有attribute子节点
										if(StringUtils.isNotBlank(paramCallName) || StringUtils.isNotBlank(paramDefaultValue)) {
											throw new ServiceException("param节点有call或default属性，其下就不能有attribute子节点！");
										}
										
										// 解析callParam节点
										for(Element callParamE : paramAttriEList) {
											callParamMatch = callParamE.attributeValue("match"); // 该属性是必须的，为该属性对应param节点对象的XPath路径
											callParamName = callParamE.attributeValue("call");	 // 如果需要外部传入来赋值，则需要该属性
											defaultValue = callParamE.attributeValue("default"); // 如果需要设置默认值，则需要该属性
													
											if(StringUtils.isBlank(callParamMatch)) {
												//TODO 这里打印错误日志。。
												throw new ServiceException("updateServiceRegister失败：attribute的match不能为空！");
											}
											
											paramAttriMap.put(callParamMatch, new AttributeBean(callParamMatch, callParamName, defaultValue));
										}
										
										paramList.add(new MethodParamBean(methodParamClass, paramAttriMap));
									} else {
										MethodParamBean methodParamBeanTemp = new MethodParamBean(methodParamClass);
										if(StringUtils.isNotBlank(callParamName)) {
											methodParamBeanTemp.setCall(callParamName);
										} else {
											methodParamBeanTemp.setDefaultValue(defaultValue);
										}
										
										paramList.add(methodParamBeanTemp);
									}
								}
							}
							
							// 解析方法返回值
							Element returnE = methodE.element("return");
							if(returnE != null) {
								returnType = returnE.attributeValue("type");
								if(StringUtils.isBlank(returnType)) {
									//TODO 这里打印错误日志。。
									throw new ServiceException("updateServiceRegister失败：return节点的type不能为空！");
								}
								
							}
							
							methodBean = new MethodBean(methodName, callName, version, desc, paramList, StringUtils.isBlank(returnType) ? null : (refersMap.containsKey(returnType) ? 
									refersMap.get(returnType) : ObjectSwitchTool.getClassForName(returnType)), StringUtils.isBlank(methodStartDate) ? 
									null : dateFormat.parse(methodStartDate), StringUtils.isBlank(methodEndDate) ? null : dateFormat.parse(methodEndDate), serviceBean);
							
							serviceBean.getMethodBeans().add(methodBean);
//							methodBeanList.add(methodBean);
							
							// 每解析完一个method节点，都将其的Method对象放入缓存中
							Class[] paramClassArray = new Class[paramList.size()];
							for(int i = 0; i < paramList.size(); i++) {
								paramClassArray[i] = paramList.get(i).getType();
							}
							callServiceCache.getMethodMap().putIfAbsent(callName, serviceBean.getClazz().getMethod(methodName, paramClassArray));
							
						}
					}
					
					// 将解析的service bean添加到列表中
					newServiceBeanList.add(serviceBean);
					
					// 将解析完的服务bean的代理对象放入缓存
					Object proxyObj = factory.create(serviceBean.getClazz(), serviceBean.getUrl());
					for(MethodBean methodBeanTemp : serviceBean.getMethodBeans()) {
						callServiceCache.getProxyObjMap().putIfAbsent(methodBeanTemp.getCall(), proxyObj);
					}
					
					// 将解析的方法bean添加到newServiceRegisterMap中,【调用名称-版本号】为key
					for(MethodBean mBean : serviceBean.getMethodBeans()) {
						if(newServiceRegisterMap.containsKey(String.format("%s-%s", new Object[]{mBean.getCall(), mBean.getVersion()}))) {
							throw new ServiceException(String.format("updateServiceRegister失败:%s服务名%s版本已被注册！", Arrays.asList(mBean.
									getCall(), mBean.getVersion()))); 
						} else {
							newServiceRegisterMap.put(String.format("%s-%s", new Object[]{mBean.getCall(), mBean.getVersion()}), mBean);
						}
					}
				}
				
				// 将最新的数据更新到ServiceRegisterFactory
				serviceManager.setServiceBeanList(newServiceBeanList);
				serviceManager.setServiceRegisterMap(newServiceRegisterMap);
				// 服务有效性检查
				serviceManager.serviceEffectiveCheck();
			}
			
		} catch (DocumentException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:读取服务注册文件失败", e);
		} catch (ClassNotFoundException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:加载Class失败！", e);
		} catch (SecurityException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:加载类型失败！", e);
		} catch (NoSuchMethodException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:创建Method缓存失败！", e);
		} catch (MalformedURLException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:创建服务代理失败！", e);
		} catch (ParseException e) {
			//TODO 这里打印错误日志。。
			throw new ServiceException("updateServiceRegister失败:日期格式化错误，创建服务代理失败！", e);
		}
	}

//	public void setPropertiesFilePath(String propertiesFilePath) {
//		this.propertiesFilePath = propertiesFilePath;
//	}
}
