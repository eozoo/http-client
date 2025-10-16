package com.cowave.zoo.http.client.multipart;

import com.cowave.zoo.http.client.annotation.*;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface MultiMockService {

    @HttpLine("POST /api/v1/submit")
    HttpResponse<Object> submit(@HttpMultiForm Map<String, Object> multiBody);

    @HttpLine("POST /api/v1/submit")
    HttpResponse<Object> submitParam(@HttpMultiParam("id") Integer id, @HttpMultiParam("name") String name);

    @HttpLine("POST /api/v1/submit2")
    HttpResponse<Object> submit2(@HttpMultiForm Map<String, Object> multiBody, @HttpMultiFile(fileName = "file") File file);

    @HttpOptions(connectTimeout = 3000, readTimeout = 5000, retryTimes = 2, retryInterval = 3)
    @HttpLine("POST /api/v1/submit2")
    HttpResponse<Object> submit3(@HttpMultiForm Map<String, Object> multiBody, @HttpMultiFile(fileName = "file")InputStream inputStream);

    @HttpLine("POST /api/v1/submit3")
    HttpResponse<Object> submit4(@HttpMultiForm Map<String, Object> multiBody, @HttpMultiFile(fileName = "testFile") byte[] bytes);
}
