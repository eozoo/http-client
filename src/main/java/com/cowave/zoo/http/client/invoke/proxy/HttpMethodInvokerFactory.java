package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.request.MultipartRequestFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import com.cowave.zoo.http.client.HttpExceptionHandler;
import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import com.cowave.zoo.http.client.invoke.codec.HttpEncoder;
import com.cowave.zoo.http.client.invoke.exec.HttpExecutor;
import com.cowave.zoo.http.client.request.Options;
import com.cowave.zoo.http.client.request.meta.HttpMethodMetaParser;
import com.cowave.zoo.http.client.request.meta.HttpMethodMeta;
import com.cowave.zoo.http.client.request.HttpRequestFactory;
import com.cowave.zoo.http.client.request.BodyRequestFactory;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class HttpMethodInvokerFactory {
    private final HttpExecutor httpExecutor;
    private final HttpMethodMetaParser metaParser;
    private final HttpEncoder encoder;
    private final HttpDecoder decoder;
    private final Options options;
    private final List<HttpClientInterceptor> httpClientInterceptors;
    private final HttpExceptionHandler exceptionHandler;

    private final boolean ignoreError;
    private final Level level;

    public Map<String, MethodInvoker> create(ProxyTarget<?> proxyTarget) throws UnsupportedEncodingException {

        List<HttpMethodMeta> metaList = metaParser.parse(proxyTarget.type());

        Map<String, MethodInvoker> result = new LinkedHashMap<>();
        for (HttpMethodMeta meta : metaList) {
            HttpRequestFactory httpRequestFactory;
            if (meta.getMultipartFileIndex() != null
                    || meta.getMultipartFormIndex() != null || !meta.getMultipartParams().isEmpty()) {
                // multipart/form-data
                httpRequestFactory = new MultipartRequestFactory(meta);
            } else if (meta.getBodyIndex() != null) {
                // 存在body
                httpRequestFactory = new BodyRequestFactory(meta, encoder);
            } else {
                httpRequestFactory = new HttpRequestFactory(meta);
            }

            // <methodKey, MethodHandler>
            result.put(meta.getMethodKey(),
                    new HttpMethodInvoker(proxyTarget, meta, httpRequestFactory,
                            httpExecutor, httpClientInterceptors, options, decoder, level, ignoreError, exceptionHandler));
        }
        return result;
    }
}
