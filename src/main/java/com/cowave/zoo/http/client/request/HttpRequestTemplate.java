package com.cowave.zoo.http.client.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@RequiredArgsConstructor
public class HttpRequestTemplate {
    private static final int LOG_URL_MAX_LENGTH = 128;
    private final String method;
    private final String url;
    private final byte[] body;
    private final Charset charset;
    private final Map<String, Collection<String>> headers;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private final int callTimeout;
    private final int retryTimes;
    private final int retryInterval;
    private final InputStream multiFile;
    private final String multiFileName;
    private final Map<String, Object> multiForm;

    // 调用从请求模板创建时开始计时
    private final long startNanos = System.nanoTime();

    // 整体剩余调用时间
    public long remainingCallTimeout() throws SocketTimeoutException {
        if (callTimeout == 0) {
            return 0;
        }

        long remainingNanos = TimeUnit.MILLISECONDS.toNanos(callTimeout) - (System.nanoTime() - startNanos);
        if (remainingNanos <= 0) {
            throw new SocketTimeoutException("HTTP call timeout");
        }
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(remainingNanos));
    }

    public static String logUrl(String url) {
        return url.length() > LOG_URL_MAX_LENGTH
                ? url.substring(0, LOG_URL_MAX_LENGTH) + "..." : url;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method).append(' ').append(url).append(" HTTP/1.1\n");
        for (String field : headers.keySet()) {
            for (String value : valuesOrEmpty(headers, field)) {
                builder.append(field).append(": ").append(value).append('\n');
            }
        }
        if (body != null) {
            builder.append('\n').append(charset != null ? new String(body, charset) : "Binary data");
        }
        return builder.toString();
    }

    public static <T> Collection<T> valuesOrEmpty(Map<String, Collection<T>> map, String key) {
        return map.containsKey(key) && map.get(key) != null ? map.get(key) : Collections.emptyList();
    }
}
