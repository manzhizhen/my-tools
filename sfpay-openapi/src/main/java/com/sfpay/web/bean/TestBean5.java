package com.sfpay.web.bean;

import java.io.Serializable;
import java.util.Date;

public class TestBean5 implements Serializable{
	private Date date;
	private String future;
	private static final long serialVersionUID = 2220130547367361819L;

	public TestBean5() {
	}
	
	public TestBean5(Date date, String future) {
		this.date = date;
		this.future = future;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFuture() {
		return future;
	}

	public void setFuture(String future) {
		this.future = future;
	}
	
	@Override
	public String toString() {
		return date + future;
	}

}
