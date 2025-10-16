package com.cowave.zoo.http.client.asserts;

import com.cowave.zoo.http.client.response.ResponseCode;
import lombok.Getter;

/**
 * 异常日志只打印e.message
 *
 * @author shanhuiming
 */
@Getter
public class HttpWarnException extends HttpException {

    public HttpWarnException(String message, Object... args) {
        super(message, args);
    }

    public HttpWarnException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public HttpWarnException(ResponseCode responseCode) {
        super(responseCode);
    }

    public HttpWarnException(ResponseCode responseCode, String message, Object... args) {
       super(responseCode, message, args);
    }

    public HttpWarnException(Throwable cause, ResponseCode responseCode, String message, Object... args) {
        super(cause, responseCode, message, args);
    }

    public HttpWarnException(int status, String code) {
        super(status, code);
    }

    public HttpWarnException(int status, String code, String message, Object... args) {
        super(status, code, message, args);
    }

    public HttpWarnException(Throwable cause, int status, String code, String message, Object... args) {
        super(cause, status, code, message, args);
    }
}
