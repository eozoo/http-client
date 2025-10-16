package com.cowave.zoo.http.client.post;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpHeaders;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.response.HttpResponse;
import com.cowave.zoo.http.client.response.Response;

import java.util.Map;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface PostMockService {

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("POST /api/v1/create")
    HttpResponse<Object> create(Map<String, Object> bodyMap);

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("POST /api/v1/create")
    Response<PostBody> create2(Map<String, Object> bodyMap);
}
