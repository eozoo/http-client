package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.request.HttpRequest;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpClientInterceptor {

    void apply(HttpRequest httpRequest);
}
