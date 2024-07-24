package org.springframework.feign.codec;

import org.springframework.http.HttpStatus;

/**
 *
 * @author shanhuiming
 *
 */
public interface ResponseCode {

    /**
     * Http状态
     */
    default int status(){
        return HttpStatus.OK.value();
    }

    /**
     * 响应码
     */
    int code();

    /**
     * 响应描述
     */
    String msg();
}
