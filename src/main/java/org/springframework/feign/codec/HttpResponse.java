package org.springframework.feign.codec;

import feign.Response;
import feign.Util;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private int status;

    private String reason;

    private Map<String, Collection<String>> headers;

    private Response.Body body;

    private String data;

    public HttpResponse(feign.Response response) throws IOException {
        this.status = response.status();
        this.reason = response.reason();
        this.headers = response.headers();
        this.body = response.body();
        if (response.body() != null) {
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            this.data = new String(bodyData, StandardCharsets.UTF_8);
        }
    }
}
