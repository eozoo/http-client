package com.cowave.zoo.http.client.put;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface PutMockService {

    @HttpLine("PUT /api/v1/put")
    HttpResponse<Object> put1(Map<String, Object> bodyMap);
}
