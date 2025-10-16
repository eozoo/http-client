package com.cowave.zoo.http.client.get;

import com.cowave.zoo.http.client.annotation.*;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.util.Map;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(name = "xxxService")
public interface GetMockNameService {

    @HttpHeaders({X_Request_ID + ": 12345"})
    @HttpLine("GET /api/v1/get/{id}")
    HttpResponse<Object> getWithHttpParam(@HttpParam("id") Integer id);

    @HttpLine("GET /api/v1/getByMap")
    HttpResponse<GetParam> getWithHttpParamMap(@HttpHeaderMap Map<String, Object> headMap, @HttpParamMap Map<String, Object> paramMap);
}
