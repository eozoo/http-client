/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package org.springframework.feign.codec;

import org.springframework.http.HttpStatus;

/**
 * @author shanhuiming
 */
public enum ResponseCode implements HttpCode {

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

    private final Integer code;

    private final String msg;

    ResponseCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
