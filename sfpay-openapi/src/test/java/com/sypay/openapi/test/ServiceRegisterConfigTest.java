package com.sypay.openapi.test;


public class ServiceRegisterConfigTest {

	public static void main(String[] args) {
		Class clazz;
		try {
			clazz = Class.forName("com.sypay.openapi.test.Test");
			System.out.println(clazz.getDeclaredMethod("math", new Class[]{String.class, int.class, String.class}));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

}
