package org.springframework.feign.codec;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author shanhuiming
 *
 */
public enum ResponseCode {
	
	OK(HttpServletResponse.SC_OK, "Success"),
	
	BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),
	
	UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"),
	
	FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),
	
	TOKEN_INVALID_OR_EXPIRED(498, "Token changed or expired"),

	INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System Error");

	private final int code;
	
	private final String desc;
	
	ResponseCode(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
