package org.springframework.feign.codec;

import org.springframework.http.HttpStatus;

/**
 * @author shanhuiming
 */
public enum ResponseCode {

    /**
     * 200
     */
    OK(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase()),

    /**
     * 202
     */
    ACCEPTED(HttpStatus.ACCEPTED.value(), HttpStatus.ACCEPTED.getReasonPhrase()),

    /**
     * 400
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()),

    /**
     * 401
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()),

    /**
     * 403
     */
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase()),

    /**
     * 429
     */
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()),

    /**
     * 498
     */
    TOKEN_INVALID_OR_EXPIRED(498, "Token changed or expired"),

    /**
     * 500
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),

    /**
     * 597
     */
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
