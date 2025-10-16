package com.cowave.zoo.http.client.asserts;

import com.cowave.zoo.http.client.response.ResponseCode;
import lombok.Getter;

/**
 * 不打印异常日志
 *
 * @author shanhuiming
 */
@Getter
public class HttpHintException extends HttpException {

    public HttpHintException(String message, Object... args) {
        super(message, args);
    }

    public HttpHintException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public HttpHintException(ResponseCode responseCode) {
        super(responseCode);
    }

    public HttpHintException(ResponseCode responseCode, String message, Object... args) {
       super(responseCode, message, args);
    }

    public HttpHintException(Throwable cause, ResponseCode responseCode, String message, Object... args) {
        super(cause, responseCode, message, args);
    }

    public HttpHintException(int status, String code) {
        super(status, code);
    }

    public HttpHintException(int status, String code, String message, Object... args) {
        super(status, code, message, args);
    }

    public HttpHintException(Throwable cause, int status, String code, String message, Object... args) {
        super(cause, status, code, message, args);
    }
}
