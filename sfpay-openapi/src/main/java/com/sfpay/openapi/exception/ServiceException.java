package com.sfpay.openapi.exception;

/**
 * 服务类异常
 * @author 625288
 *
 * 2014年10月4日
 */
public class ServiceException extends Exception{
	private String message;
	private Exception cause;
	
	private static final long serialVersionUID = 7527308176324458430L;
	
	public ServiceException(String message) {
		this.message = message;
	}
	
	public ServiceException(String message, Exception cause) {
		this.message = message;
		this.cause = cause;
	}
	
	@Override
	public String toString() {
		return message + cause == null ? "" : System.getProperty("line.separator") + cause;
	}
}
