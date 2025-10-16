package com.cowave.zoo.http.client.download;

import com.cowave.zoo.http.client.response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mock")
public class DownloadMockController {

    private final DownloadMockService downloadMockService;

    @GetMapping("/download1")
    public String download1() throws IOException {
        HttpResponse<InputStream> httpResponse = downloadMockService.download();
        return new String(IOUtils.toByteArray(httpResponse.getBody()));
    }
}
