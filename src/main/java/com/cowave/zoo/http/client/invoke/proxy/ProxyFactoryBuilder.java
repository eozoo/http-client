package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.request.meta.HttpMethodMetaParser;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.event.Level;
import com.cowave.zoo.http.client.HttpExceptionHandler;
import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import com.cowave.zoo.http.client.invoke.codec.HttpEncoder;
import com.cowave.zoo.http.client.invoke.codec.decoder.JacksonDecoder;
import com.cowave.zoo.http.client.invoke.codec.encoder.JacksonEncoder;
import com.cowave.zoo.http.client.invoke.exec.HttpExecutor;
import com.cowave.zoo.http.client.request.Options;
import com.cowave.zoo.http.client.request.meta.HttpMethodMetaParserImpl;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Setter
@RequiredArgsConstructor
public class ProxyFactoryBuilder {
    private final HttpMethodMetaParser metaParser = new HttpMethodMetaParserImpl();
    private final List<HttpClientInterceptor> httpClientInterceptors = new ArrayList<>();
    private final Options options;
    private HttpEncoder encoder = new JacksonEncoder();
    private HttpDecoder decoder = new JacksonDecoder();
    private HttpExecutor httpExecutor;
    private HttpExceptionHandler exceptionHandler;

    public void addHttpInterceptor(HttpClientInterceptor httpClientInterceptor) {
        this.httpClientInterceptors.add(httpClientInterceptor);
    }

    public ProxyFactory newProxyFactory(Level level, boolean ignoreError) {
        HttpMethodInvokerFactory methodHandlerFactory = new HttpMethodInvokerFactory(
                httpExecutor, metaParser, encoder, decoder,
                options, httpClientInterceptors, exceptionHandler, ignoreError, level);
        return new ProxyFactory(methodHandlerFactory);
    }
}
