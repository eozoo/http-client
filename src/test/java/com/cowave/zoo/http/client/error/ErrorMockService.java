package com.cowave.zoo.http.client.error;

import com.cowave.zoo.http.client.annotation.*;
import com.cowave.zoo.http.client.response.HttpResponse;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(ignoreError = true)
public interface ErrorMockService {

    @HttpOptions(connectTimeout = 500)
    @HttpLine("GET /api/v1/get/{id}")
    HttpResponse<Void> error1(@HttpHost String httpUrl, @HttpParam("id") Integer id);
}
