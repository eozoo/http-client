package com.cowave.zoo.http.client.invoke.codec;

import com.cowave.zoo.http.client.request.HttpRequest;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpEncoder {

    void encode(HttpRequest request, Object object) throws Exception;
}
