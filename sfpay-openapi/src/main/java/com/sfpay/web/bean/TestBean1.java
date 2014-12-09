package com.sfpay.web.bean;

import java.io.Serializable;


public class TestBean1 implements Serializable{
	private int age;
	private TestBean4 testBean4;
	
	private static final long serialVersionUID = -268302902384796825L;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public TestBean4 getTestBean4() {
		return testBean4;
	}

	public void setTestBean4(TestBean4 testBean4) {
		this.testBean4 = testBean4;
	}
}
