package com.sfpay.web.bean;

import java.io.Serializable;
import java.util.Date;

public class TestBean2 implements Serializable {
	private Date date;
	private String girl;
	private static final long serialVersionUID = -660214622386921569L;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getGirl() {
		return girl;
	}

	public void setGirl(String girl) {
		this.girl = girl;
	}

}
