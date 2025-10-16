package com.cowave.zoo.http.client.register;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.invoke.exec.HttpClientExecutor;
import com.cowave.zoo.http.client.invoke.codec.HttpEncoder;
import com.cowave.zoo.http.client.invoke.proxy.ProxyFactory;
import com.cowave.zoo.http.client.invoke.proxy.ProxyFactoryBuilder;
import com.cowave.zoo.http.client.invoke.proxy.ProxyTarget;
import com.cowave.zoo.http.client.request.Options;
import org.springframework.context.ApplicationContext;
import com.cowave.zoo.http.client.HttpExceptionHandler;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpProxyFactory {

    public static <T> T newProxy(Class<T> clazz, HttpClient httpClient,
                                 ApplicationContext applicationContext, StringValueResolver valueResolver) throws UnsupportedEncodingException {
        int retryTimes = getInt(httpClient.retryTimes(), httpClient.retryTimesStr(), valueResolver);
        int retryInterval = getInt(httpClient.retryInterval(), httpClient.retryIntervalStr(), valueResolver);
        int connectTimeout = getInt(httpClient.connectTimeout(), httpClient.connectTimeoutStr(), valueResolver);
        int readTimeout = getInt(httpClient.readTimeout(), httpClient.readTimeoutStr(), valueResolver);
        ProxyFactoryBuilder proxyFactoryBuilder = new ProxyFactoryBuilder(new Options(connectTimeout, readTimeout, retryTimes, retryInterval));

        String[] interceptors = applicationContext.getBeanNamesForType(HttpClientInterceptor.class);
        for (String interceptorName : interceptors) {
            HttpClientInterceptor httpClientInterceptor = applicationContext.getBean(interceptorName, HttpClientInterceptor.class);
            proxyFactoryBuilder.addHttpInterceptor(httpClientInterceptor);
        }

        String[] handlers = applicationContext.getBeanNamesForType(HttpExceptionHandler.class);
        if (handlers.length > 0) {
            String handler = handlers[0];
            HttpExceptionHandler exceptionHandler = applicationContext.getBean(handler, HttpExceptionHandler.class);
            proxyFactoryBuilder.setExceptionHandler(exceptionHandler);
        }

        try {
            // encode / decoder
            proxyFactoryBuilder.setEncoder((HttpEncoder) httpClient.encoder().newInstance());
            proxyFactoryBuilder.setDecoder((HttpDecoder) httpClient.decoder().newInstance());
            // Https设置
            Class<? extends SSLSocketFactory> sslSocketFactoryClass = httpClient.sslSocketFactory();
            Class<? extends HostnameVerifier> hostnameVerifierClass = httpClient.hostnameVerifier();
            SSLSocketFactory SSLSocketFactory = sslSocketFactoryClass.newInstance();
            HostnameVerifier hostnameVerifier = hostnameVerifierClass.newInstance();
            proxyFactoryBuilder.setHttpExecutor(new HttpClientExecutor(SSLSocketFactory, hostnameVerifier));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ProxyFactory proxyFactory = proxyFactoryBuilder.newProxyFactory(httpClient.level(), httpClient.ignoreError());
        ProxyTarget<T> proxyTarget = new ProxyTarget<>(
                applicationContext, valueResolver, clazz, httpClient.name(), httpClient.url());
        return proxyFactory.newProxy(proxyTarget);
    }

    private static int getInt(int defaultValue, String regex, StringValueResolver valueResolver) {
        if (!StringUtils.hasText(regex)) {
            return defaultValue;
        }
        return Integer.parseInt(Objects.requireNonNull(valueResolver.resolveStringValue(regex)));
    }
}
