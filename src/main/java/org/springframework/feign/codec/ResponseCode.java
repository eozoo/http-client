package org.springframework.feign.codec;

import org.springframework.http.HttpStatus;

/**
 * @author shanhuiming
 */
public enum ResponseCode {

    /**
     * 200
     */
    OK(HttpStatus.OK.value(), "Success"),

    /**
     * 202
     */
    ACCEPTED(HttpStatus.ACCEPTED.value(), "Accepted"),

    /**
     * 400
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "Bad Request"),

    /**
     * 401
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"),

    /**
     * 403
     */
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "Forbidden"),

    /**
     * 429
     */
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests"),

    /**
     * 498
     */
    TOKEN_INVALID_OR_EXPIRED(498, "Token changed or expired"),

    /**
     * 500
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"),

    /**
     * 597
     */
    SYS_ERROR(597, "Business Error");

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
