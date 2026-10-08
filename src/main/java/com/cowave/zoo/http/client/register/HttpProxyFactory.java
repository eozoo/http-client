package com.cowave.zoo.http.client.register;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.HttpFallback;
import com.cowave.zoo.http.client.HttpFallbackFactory;
import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.invoke.exec.OkHttpExecutor;
import com.cowave.zoo.http.client.invoke.exec.OkHttpExecutorFactory;
import com.cowave.zoo.http.client.invoke.codec.HttpEncoder;
import com.cowave.zoo.http.client.invoke.proxy.ProxyFactory;
import com.cowave.zoo.http.client.invoke.proxy.ProxyFactoryBuilder;
import com.cowave.zoo.http.client.invoke.proxy.ProxyTarget;
import com.cowave.zoo.http.client.request.Options;
import org.springframework.context.ApplicationContext;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpProxyFactory {

    public static <T> T newProxy(Class<T> clazz, HttpClient annotation,
                                 ApplicationContext applicationContext, StringValueResolver valueResolver) throws UnsupportedEncodingException {
        int retryTimes = getInt(annotation.retryTimes(), annotation.retryTimesStr(), valueResolver);
        int retryInterval = getInt(annotation.retryInterval(), annotation.retryIntervalStr(), valueResolver);
        if (retryTimes < 0 || retryInterval < 0) {
            throw new IllegalArgumentException("Effective retryTimes and retryInterval must not be negative");
        }
        Set<Integer> retryForStatus = retryForStatus(annotation);
        List<Class<? extends Exception>> retryForExceptions =
                Collections.unmodifiableList(new ArrayList<>(Arrays.asList(annotation.retryForExceptions())));
        int connectTimeout = getInt(annotation.connectTimeout(), annotation.connectTimeoutStr(), valueResolver);
        int readTimeout = getInt(annotation.readTimeout(), annotation.readTimeoutStr(), valueResolver);
        int writeTimeout = getInt(annotation.writeTimeout(), annotation.writeTimeoutStr(), valueResolver);
        int callTimeout = getInt(annotation.callTimeout(), annotation.callTimeoutStr(), valueResolver);
        ProxyFactoryBuilder proxyFactoryBuilder = new ProxyFactoryBuilder(
                new Options(connectTimeout, readTimeout, writeTimeout, callTimeout,
                        retryTimes, retryInterval));
        proxyFactoryBuilder.setRetryForStatus(retryForStatus);
        proxyFactoryBuilder.setRetryForExceptions(retryForExceptions);
        proxyFactoryBuilder.setFallback(fallback(clazz, annotation, applicationContext));
        proxyFactoryBuilder.setHttpFallback(applicationContext.getBeanProvider(HttpFallback.class).getIfAvailable());
        // 按照Spring优先级执行全局拦截器
        applicationContext.getBeanProvider(HttpClientInterceptor.class).orderedStream()
                .forEach(proxyFactoryBuilder::addHttpInterceptor);

        try {
            // 接口类指定的拦截器，手动实例化放到最后执行
            if (annotation.interceptor() != HttpClientInterceptor.class) {
                HttpClientInterceptor interceptor = annotation.interceptor().getDeclaredConstructor().newInstance();
                proxyFactoryBuilder.addHttpInterceptor(interceptor);
            }
            // encode / decoder
            proxyFactoryBuilder.setEncoder((HttpEncoder) annotation.encoder().newInstance());
            proxyFactoryBuilder.setDecoder((HttpDecoder) annotation.decoder().newInstance());
            // 客户端工厂、连接池及调度资源
            OkHttpExecutorFactory clientFactory = applicationContext.getBean(OkHttpExecutorFactory.class);
            proxyFactoryBuilder.setHttpExecutor(new OkHttpExecutor(clientFactory.getClient(annotation)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ProxyFactory proxyFactory = proxyFactoryBuilder.newProxyFactory(annotation.ignoreError());
        ProxyTarget<T> proxyTarget = new ProxyTarget<>(
                applicationContext, valueResolver, clazz, annotation.name(), annotation.url());
        return proxyFactory.newProxy(proxyTarget);
    }

    // 获取类级降级实现，创建代理时校验接口类型并获取Spring实现
    private static Object fallback(Class<?> clientType, HttpClient annotation, ApplicationContext context) {
        Class<?> fallbackType = annotation.fallback();
        // 默认不进行fallback调用
        if (fallbackType == void.class) {
            return null;
        }
        // 降级实现需实现接口，工厂在失败时创建接口实现
        if (fallbackType.isInterface() || (!clientType.isAssignableFrom(fallbackType)
                && !HttpFallbackFactory.class.isAssignableFrom(fallbackType))) {
            throw new IllegalArgumentException("Fallback must implement " + clientType.getName() + " or HttpFallbackFactory");
        }
        return context.getBean(fallbackType);
    }

    private static Set<Integer> retryForStatus(HttpClient annotation) {
        int[] statuses = annotation.retryForStatus();
        Set<Integer> result = new LinkedHashSet<>();
        for (int status : statuses) {
            result.add(status);
        }
        for (int status : result) {
            if (status < 100 || status > 599) {
                throw new IllegalArgumentException("retryForStatus must contain HTTP status codes from 100 to 599");
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static int getInt(int defaultValue, String regex, StringValueResolver valueResolver) {
        if (!StringUtils.hasText(regex)) {
            return defaultValue;
        }
        return Integer.parseInt(Objects.requireNonNull(valueResolver.resolveStringValue(regex)));
    }
}
