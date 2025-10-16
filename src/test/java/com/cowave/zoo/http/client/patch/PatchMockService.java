package com.cowave.zoo.http.client.patch;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpHeaders;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.annotation.HttpParam;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.util.Map;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface PatchMockService {

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("PATCH /api/v1/modify/{id}")
    HttpResponse<Object> modifyWithHttpParam(@HttpParam("id") Integer id, Map<String, Object> bodyMap);
}
