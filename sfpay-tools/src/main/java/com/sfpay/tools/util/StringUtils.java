/**
 * 
 */
package com.sfpay.tools.util;

/**
 * @author 625288
 *
 * 2014年11月12日
 */
public class StringUtils {
	/**
	 * 是否为空或是否为空白字符
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		if(str == null || str.trim().isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
	
	/**
	 * 比较两个字符串是否相等
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean isEqual(String str1, String str2) {
		if(str1 == null && str2 == null) {
			return true;
		}
		
		if(str1 != null) {
			return str1.equals(str2);
		} else {
			return str2.equals(str1);
		}
	}

	public static boolean isNotEqual(String str1, String str2) {
		return !isEqual(str1, str2);
	}
	
}
