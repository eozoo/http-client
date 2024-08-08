package org.springframework.feign.codec;

import org.springframework.http.HttpStatus;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpCode {

    /**
     * Http状态
     */
    default Integer getStatus(){
        return HttpStatus.OK.value();
    }

    /**
     * 响应码
     */
    String getCode();

    /**
     * 响应描述
     */
    String getMsg();
}
