package com.cowave.zoo.http.client.download;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.annotation.HttpLine;
import com.cowave.zoo.http.client.response.HttpResponse;

import java.io.InputStream;

/**
 *
 * @author shanhuiming
 *
 */
@HttpClient(url = "http://127.0.0.1:8080")
public interface DownloadMockService {

    @HttpLine("GET /api/v1/download")
    HttpResponse<InputStream> download();
}
