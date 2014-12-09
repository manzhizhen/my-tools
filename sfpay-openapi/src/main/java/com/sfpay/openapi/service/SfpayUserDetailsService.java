package com.sfpay.openapi.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("sfpayUserDetailsService")
public class SfpayUserDetailsService implements UserDetailsService{
	private List<GrantedAuthority> grantedAuthoritys = new ArrayList<GrantedAuthority>();
	
	@Override
	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException {
		if(StringUtils.isBlank(userName)) {
			throw new UsernameNotFoundException("用户名不能为空！");
		}
		
		// 把用户名作为密码传入
		return new User(userName, userName, grantedAuthoritys);
	}
}
