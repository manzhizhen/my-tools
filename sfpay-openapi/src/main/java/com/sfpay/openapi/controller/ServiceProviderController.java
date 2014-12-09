package com.sfpay.openapi.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sfpay.openapi.config.ControllerHelper;
import com.sfpay.openapi.exception.ServiceException;

/**
 * OpenApi的控制器
 * 
 * @author 625288
 * 
 *         2014年10月5日
 */

@Controller
@RequestMapping("/oms")
public class ServiceProviderController {
	@Autowired
	private ControllerHelper controllerHelper;
	
	@RequestMapping("/orgtypecodequery/{servicename}")
	public void orgTypeCodeService(HttpServletRequest request, HttpServletResponse response) throws ServiceException, IOException {
		@SuppressWarnings("unchecked")
		Map<String, String[]> paramsMap = request.getParameterMap();
		Map<String, String> realParamsMap = new HashMap<String, String>(paramsMap.size());
		for(Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
			realParamsMap.put(entry.getKey(), entry.getValue()[0]);
		}
		String serviceName = "orgtypecodequery";
		Object returnObj = controllerHelper.callService(serviceName, realParamsMap);
		
//		Object returnObj = "{123:123}";
		
		if(returnObj == null) {
			response.sendError(-1);
		}
		
		OutputStream output = null;
		try {
			output = response.getOutputStream();
			if(returnObj instanceof Object[] || returnObj instanceof Collection) {
				JSONArray array = JSONArray.fromObject(returnObj);
				output.write(array.toString().getBytes("UTF-8"));
			} else {
				JSONObject object = JSONObject.fromObject(returnObj);  
				output.write(object.toString().getBytes("UTF-8"));
			}
		} finally {
			if(output != null) {
				output.close();
			}
		}
	}
	
	
	
	public void test() {
		Map<String, String> paramsMap = new TreeMap<String, String>();
		paramsMap.put("tp1", "39");
		paramsMap.put("tp2", "manzhizhen");
		paramsMap.put("tp3", "20141006");
		paramsMap.put("tp4", "39");
		paramsMap.put("tp5", "forever");
		
		try {
			System.out.println(controllerHelper.callService("com.sypay.openapi.test", paramsMap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test1() {
		Map<String, String> paramsMap = new TreeMap<String, String>();
		paramsMap.put("tp6", "20141006");
		paramsMap.put("tp7", "forever");
		try {
			System.out.println(controllerHelper.callService("com.sypay.openapi.test1", paramsMap));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


}
