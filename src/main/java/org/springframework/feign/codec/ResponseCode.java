package org.springframework.feign.codec;

import javax.servlet.http.HttpServletResponse;

/**
 * @author shanhuiming
 */
public enum ResponseCode {

    /**
     * 处理成功
     */
    OK(HttpServletResponse.SC_OK, "Success"),

    /**
     * 需要请求发起方确认是否继续处理的请求
     */
    ACCEPTED(HttpServletResponse.SC_ACCEPTED, "Accepted"),

    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST, "Bad Request"),

    UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"),

    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "Forbidden"),

    TOKEN_INVALID_OR_EXPIRED(498, "Token changed or expired"),

    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System Error"),

    SYS_ERROR(597, "Sys Error");

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
