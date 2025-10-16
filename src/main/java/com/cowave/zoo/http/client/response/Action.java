package com.cowave.zoo.http.client.response;

/**
 *
 * @author shanhuiming
 *
 */
@FunctionalInterface
public interface Action {

    void exec() throws Exception;
}
