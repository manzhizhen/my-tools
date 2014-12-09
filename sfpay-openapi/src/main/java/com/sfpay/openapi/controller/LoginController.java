package com.sfpay.openapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class LoginController {
	
	@RequestMapping(value="/login", method= RequestMethod.GET)
	public ModelAndView loginPage() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login/login");
		return modelAndView;
	}
	
	@RequestMapping("/login/result")
	public ModelAndView loginCheck(@RequestParam(value = "error", required = false) boolean error) {
		ModelAndView modelAndView = new ModelAndView();
		if(error) {
			modelAndView.setViewName("login/login_fail");
		} else {
			modelAndView.setViewName("login/login_success");
		}
		return modelAndView;
	}
	
	@RequestMapping("/loginout")
	public ModelAndView loginOut() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login/login_out");
		return modelAndView;
	}
	
	@RequestMapping("/deied")
	public String deniedPage() {
		return "login/deied";
	}
	
	
	
}
