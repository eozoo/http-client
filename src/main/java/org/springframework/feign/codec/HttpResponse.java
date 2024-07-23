package org.springframework.feign.codec;

import feign.Response;
import feign.Util;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@NoArgsConstructor
@Data
public class HttpResponse {

    private String requestId;

    private int status;

    private String reason;

    private Map<String, Collection<String>> headers;

    private Response.Body body;

    private String data;

    private Logger logger;

    private long cost;

    private String url;

    public HttpResponse(feign.Response response, Logger logger, long cost, String url) throws IOException {
        this.status = response.status();
        this.reason = response.reason();
        this.headers = response.headers();
        this.body = response.body();
        if (response.body() != null) {
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            this.data = new String(bodyData, StandardCharsets.UTF_8);
        }
        logger.info(">< {} {}ms {}", status, cost, url);
    }
}
