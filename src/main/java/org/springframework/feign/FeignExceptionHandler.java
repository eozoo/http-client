package org.springframework.feign;

import com.cowave.commons.response.exception.HttpException;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignExceptionHandler {

    void handle(HttpException e);
}
