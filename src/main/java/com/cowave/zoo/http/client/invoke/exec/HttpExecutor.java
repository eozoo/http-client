package com.cowave.zoo.http.client.invoke.exec;

import com.cowave.zoo.http.client.request.HttpRequestTemplate;
import com.cowave.zoo.http.client.response.HttpResponseTemplate;

import java.io.IOException;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpExecutor {

    HttpResponseTemplate execute(HttpRequestTemplate httpRequestTemplate) throws IOException;
}
