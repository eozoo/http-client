package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.asserts.HttpException;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpExceptionHandler {

    void handle(HttpException e);
}
