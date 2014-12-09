package com.sfpay.web.service;

import com.sfpay.web.bean.TestBean1;
import com.sfpay.web.bean.TestBean2;
import com.sfpay.web.bean.TestBean3;

public interface TestService {
	public TestBean3 serviceTest(TestBean1 testBean1, TestBean2 testBean2, int index, String label);


}
