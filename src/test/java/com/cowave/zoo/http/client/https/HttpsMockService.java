package com.cowave.zoo.http.client.https;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpHeaders;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.util.Map;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "https://127.0.0.1:8443")
public interface HttpsMockService {

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("POST /api/v1/create")
    HttpResponse<Object> create(Map<String, Object> bodyMap);

}
