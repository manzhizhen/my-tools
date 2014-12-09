package com.sfpay.web.bean;

import java.io.Serializable;

public class TestBean6 implements Serializable{
	private String beanName;
	private static final long serialVersionUID = 7227855727904563080L;

	public TestBean6() {
	}

	public TestBean6(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	@Override
	public String toString() {
		return beanName;
	}
}
