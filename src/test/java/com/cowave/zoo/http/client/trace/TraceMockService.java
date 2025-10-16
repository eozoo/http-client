package com.cowave.zoo.http.client.trace;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpHeaders;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.response.HttpResponse;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface TraceMockService {

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("TRACE /api/v1/trace")
    HttpResponse<String> trace();
}
