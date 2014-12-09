package com.sypay.openapi.test;

import java.util.ArrayList;
import java.util.Arrays;

import com.sfpay.openapi.model.MethodBean;

public class Test {
	public void math(String name, int age, String text) {
		System.out.println(name);
	}	
	public void math(Class[] aa) {
		System.out.println(aa);
	}
	
	public static void main(String[] args) {
//		System.out.println(int.class.getName().equals("int"));
//		try {
//			System.out.println(Test.class.getClassLoader().getParent().loadClass("int"));
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		Object[] obs = new Object[0];
//		String[] strs = new String[0];
//		Object obj = new ArrayList<String>();
//		if(obj instanceof Object[]) {
//			System.out.println(true);
//		}
		
		System.out.println(String.format("%s-%s", new String[]{"234", "dfs"}));
	}
}
