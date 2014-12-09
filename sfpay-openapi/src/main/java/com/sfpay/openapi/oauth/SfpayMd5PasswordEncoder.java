package com.sfpay.openapi.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sfpay.um.service.IUserService;

@Service("sfpayMd5PasswordEncoder")
public class SfpayMd5PasswordEncoder extends Md5PasswordEncoder{
	@Autowired
	@Qualifier("userService")
	private IUserService userService;
	
	private final static String SALT = "sfpay";
	
	@Override
	public boolean isPasswordValid(String userName, String rawPass, Object salt) {
		com.sfpay.um.domain.User user = userService.getByUserName(userName);
		if(user == null) {
			return false;
		}
		
		rawPass = encodePassword(rawPass, SALT);
		
		return userService.login(userName, rawPass.toUpperCase());
	}
	
	/**
	 * 不管strict值为什么，都不允许在salt中添加{}
	 */
	protected String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = "";
        }

        if (salt != null) {
            if ((salt.toString().lastIndexOf("{") != -1) || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + salt.toString();
        }
    }
}
