package com.cowave.zoo.http.client.delete;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.annotation.HttpParam;
import com.cowave.zoo.http.client.annotation.HttpParamMap;
import com.cowave.zoo.http.client.invoke.codec.decoder.ResponseDecoder;
import com.cowave.zoo.http.client.response.Response;

import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080", decoder = ResponseDecoder.class)
public interface DeleteMockService {

    @HttpLine("DELETE /api/v1/del/{list}")
    Response.Page<Integer> batchDelete(@HttpParam("list") List<Integer> list);

    @HttpLine("DELETE /api/v1/del")
    Response.Page<Integer> batchDelete2(@HttpParamMap Map<String, Object> paramMap);
}
