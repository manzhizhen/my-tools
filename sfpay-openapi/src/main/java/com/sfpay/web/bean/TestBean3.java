package com.sfpay.web.bean;

import java.io.Serializable;
import java.util.Date;

public class TestBean3 implements Serializable{
	private String name;
	private int age;
	private Date date;
	
	private static final long serialVersionUID = -5867718678279936824L;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	@Override
	public String toString() {
		return name + " " + age + " " + date;
	}

}
