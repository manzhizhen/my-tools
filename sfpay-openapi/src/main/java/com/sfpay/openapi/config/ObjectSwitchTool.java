package com.sfpay.openapi.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sfpay.web.bean.TestBean7;

@SuppressWarnings("rawtypes")
public class ObjectSwitchTool {
	private static Map<String, Class> baseClassMap;
	
	static {
		baseClassMap = new HashMap<String, Class>();
		baseClassMap.put(String.class.getName(), String.class);
		baseClassMap.put(byte.class.getName(), byte.class);
		baseClassMap.put(char.class.getName(), char.class);
		baseClassMap.put(short.class.getName(), short.class);
		baseClassMap.put(int.class.getName(), int.class);
		baseClassMap.put(short.class.getName(), short.class);
		baseClassMap.put(long.class.getName(), long.class);
		baseClassMap.put(float.class.getName(), float.class);
		baseClassMap.put(double.class.getName(), double.class);
		baseClassMap.put(boolean.class.getName(), boolean.class);
		baseClassMap.put(String.class.getName(), String.class);
		baseClassMap.put(Date.class.getName(), Date.class);
	}
	
	/**
	 * 创建一个方法参数对象
	 * 递归处理
	 * @param paramClass
	 * @param paramFieldValueMap
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static Object createParamObjectForRecursive(Object paramClass,
			Map<String, String> paramFieldValueMap)
				throws InstantiationException, IllegalAccessException, NoSuchFieldException, 
				SecurityException, NoSuchMethodException, IllegalArgumentException, 
				InvocationTargetException, ParseException {
		Object object = null;
		if(paramClass instanceof Class) {
			if(isBaseType((Class)paramClass)) {
				return switchToBase((Class)paramClass, null);
			}
			
			object = ((Class)paramClass).newInstance();
		} else {
			object = paramClass;
		}
		Class clazz = object.getClass();
			
		Object fieldObj = null;
		for (Map.Entry<String, String> entry : paramFieldValueMap.entrySet()) {
			String fieldPath = entry.getKey();
			String value = entry.getValue();
			
			// 只需要处理第一个path，其下的递归处理
			String[] fieldXPathArray = fieldPath.split("\\.");
		
			Field field = clazz.getDeclaredField(fieldXPathArray[0]);
			Class fieldClass = field.getType();
			
			if(isBaseType(fieldClass)) {
				Method method = clazz.getDeclaredMethod(getSetMethodName(field.getName()), 
						field.getType());
				method.invoke(object, switchToBase(field.getType(), (String)entry.getValue()));
			} else {
				Method getMethod = clazz.getDeclaredMethod(getGetMethodName(field.getName()));
				fieldObj = getMethod.invoke(object);
				
				Method setMethod = clazz.getDeclaredMethod(getSetMethodName(field.getName()), fieldClass);
				Map<String, String> fieldValueMap = new HashMap<String, String>();
				fieldValueMap.put(fieldPath.replaceFirst(fieldXPathArray[0] + "\\.", ""), value);
				if(fieldObj == null) {
					fieldObj = createParamObjectForRecursive(fieldClass,
							fieldValueMap);
					setMethod.invoke(object, fieldObj);
				} else {
					createParamObjectForRecursive(fieldObj,
							fieldValueMap);
				}
			}
			
		}
		
		return object;
	}
	
	/**
	 * 创建一个方法参数对象
	 * 非递归处理,比递归处理（createParamObjectForRecursive）效率更高
	 * @param paramClass
	 * @param paramFieldValueMap
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 */
	public static Object createParamObject(Class paramClass,
			Map<String, String> paramFieldValueMap)
				throws InstantiationException, IllegalAccessException, NoSuchFieldException, 
				SecurityException, NoSuchMethodException, IllegalArgumentException, 
				InvocationTargetException, ParseException {
		if(isBaseType(paramClass)) {
			return switchToBase(paramClass, null);
		}
		
		Object object = paramClass.newInstance();
		// 用一个Map来存储这次转换所用到的中间对象，key为xpath全路径，value为对应的对象
		Map<String, Object> xpathMap = new HashMap<String, Object>();
		xpathMap.put("", object);// object为根对象，它的key为空字符串
		for (Map.Entry<String, String> entry : paramFieldValueMap.entrySet()) {
			String fieldPath = entry.getKey();
			String value = entry.getValue();
			
			String[] fieldXPathArray = fieldPath.split("\\.");
			StringBuilder preXPath = new StringBuilder();
			Object preObject = null, thisObject = null;
			Method setMethod = null;
			Field field = null;
			Class fieldClass = null;
			for(String xpath : fieldXPathArray) {
				// 由于是从浅到深来处理，所以前对象是肯定已经被创建了
				preObject = xpathMap.get(preXPath.toString());
				preXPath.append(preXPath.length() ==0 ? xpath : ("." + xpath));
				thisObject = xpathMap.get(preXPath.toString());	// 当前xpath对应的对象，有可能为空
				
				if(thisObject == null) {
					field = preObject.getClass().getDeclaredField(xpath);
					fieldClass = field.getType();
					setMethod = preObject.getClass().getDeclaredMethod(getSetMethodName(xpath), 
							fieldClass);
					
					// 如果是基本类型，则直接构造该对象
					if(isBaseType(fieldClass)) {
						thisObject = switchToBase(field.getType(), StringUtils.equals(fieldPath, preXPath.toString()) ? value : null);
					// 如果不是基本类型，则直接newInstanceof来创建对象
					} else {
						thisObject = fieldClass.newInstance();
					}
					
					setMethod.invoke(preObject, thisObject); // 将该对象设置到父对象中
					xpathMap.put(preXPath.toString(), thisObject); // 将该对象放到Map中，以防重新创建。
				}
			}
		}
		
		return object;
	}
	
	/**
	 * 通过类名来获取Class对象
	 * @param clazzName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class getClassForName(String clazzName) throws ClassNotFoundException {
		Class clazz = baseClassMap.get(clazzName);
		if(clazz != null) {
			return clazz;
		}
		
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
		} catch (ClassNotFoundException e) {
			clazz = ObjectSwitchTool.class.getClassLoader().loadClass(clazzName);
		}
		
		return clazz;
	}
	
	public static boolean isBaseType(Class clazz) {
		return baseClassMap.values().contains(clazz);
	}
	
	/**
	 * 根据value和Class创建一个基本类型对象
	 * @param clazz
	 * @param value
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 */
	public static Object switchToBase(Class clazz, String value) throws InstantiationException, 
		IllegalAccessException, ParseException {
		if(clazz == null) {
			return null;
		}
		
		if(value == null && clazz.isSynthetic()) {
			return clazz.newInstance();
		}
		
		if(clazz == String.class) {
			return value;
		} else if(clazz == int.class) {
			return value == null ? 0 : Integer.valueOf(value);
		} else if(clazz == boolean.class) {
			return value == null ? false : Boolean.valueOf(value);
		} else if(clazz == long.class) {
			return value == null ? 0 : Long.valueOf(value);
		} else if(clazz == double.class) {
			return value == null ? 0 : Double.valueOf(value);
		} else if(clazz == char.class) {
			return value == null ? 0 : value.charAt(0);
		} else if(clazz == float.class) {
			return value == null ? 0 : Float.valueOf(value);
		} else if(clazz == short.class) {
			return value == null ? 0 : Short.valueOf(value);
		} else if(clazz == Date.class) {
			return value == null ? new Date() : new SimpleDateFormat("yyyyMMdd").parse(value);
		} else if(clazz == byte.class) {
			return value == null ? 0 : Byte.valueOf(value);
		}
		
		return null;
	}
	
	public static String getSetMethodName(String fieldName) {
		return "set" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
	}
	
	public static String getGetMethodName(String fieldName, Class ... clazz) {
		if(clazz.length == 0 || clazz[0] != boolean.class) {
			return "get" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
		} else {
			return "is" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
		}
	}
	
	public static void main(String[] args) {
		Map<String, String> paramFieldValueMap = new HashMap<String, String>();
		paramFieldValueMap.put("testBean3.name", "manzhizhen");
		paramFieldValueMap.put("testBean3.age", "13");
		paramFieldValueMap.put("testBean3.date", "20140909");
		paramFieldValueMap.put("testBean1.age", "36");
		paramFieldValueMap.put("testBean1.testBean4.love", "manzhizhen123123123123");
		paramFieldValueMap.put("testBean2.date", "20141049");
		paramFieldValueMap.put("testBean2.girl", "huizi");
		paramFieldValueMap.put("testBean4.love", "huizi123");	
		paramFieldValueMap.put("testBean5.date", "20141010");
		paramFieldValueMap.put("testBean5.future", "money");	
		
		Object object = null;
		System.out.println("开始咯！");
		long start = System.currentTimeMillis();
		for(int i = 0; i < 500000; i++) {
			try {
				object = createParamObject(TestBean7.class, paramFieldValueMap);
//				object = createParamObjectForRecursive(TestBean7.class, paramFieldValueMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("总共时间：" + (System.currentTimeMillis() - start));
		System.out.println(object);
	}

}
