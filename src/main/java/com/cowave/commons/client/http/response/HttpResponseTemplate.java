package com.cowave.commons.client.http.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
public class HttpResponseTemplate implements Closeable {
    private final CloseableHttpResponse httpResponse;
    private final int status;
    private final Map<String, List<String>> remoteHeaders;
    private final String reason;
    private final InputStream inputStream;
    private final Integer length;
    private boolean shouldClose = true;

    @Override
    public void close() throws IOException {
        httpResponse.close();
    }
}
