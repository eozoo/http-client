package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.HttpFallback;
import com.cowave.zoo.http.client.request.meta.HttpMethodMetaParser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class ProxyFactory {

    private final HttpMethodInvokerFactory httpMethodInvokerFactory;

    private final Object fallback;

    private final HttpFallback httpFallback;

    public ProxyFactory(HttpMethodInvokerFactory httpMethodInvokerFactory, Object fallback, HttpFallback httpFallback) {
        this.httpMethodInvokerFactory = httpMethodInvokerFactory;
        this.fallback = fallback;
        this.httpFallback = httpFallback;
    }

    @SuppressWarnings("unchecked")
    public <T> T newProxy(ProxyTarget<T> proxyTarget) throws UnsupportedEncodingException {
        // 默认方法
        List<DefaultMethodInvoker> defaultProxyInvokerList = new LinkedList<>();

        // Http调用方法
        Map<String, MethodInvoker> httpProxyInvokerMap = httpMethodInvokerFactory.create(proxyTarget);

        Map<Method, MethodInvoker> methodInvokeHandlerMap = new LinkedHashMap<>();
        for (Method method : proxyTarget.type().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                // object方法不进行代理
            } else if (HttpMethodMetaParser.isDefault(method)) {
                DefaultMethodInvoker defaultInvoker = new DefaultMethodInvoker(method);
                defaultProxyInvokerList.add(defaultInvoker);
                methodInvokeHandlerMap.put(method, defaultInvoker);
            } else {
                MethodInvoker httpInvoker = httpProxyInvokerMap.get(HttpMethodMetaParser.methodKey(proxyTarget.type(), method));
                // 如果需要fallback，就对httpInvoker进行一下包装
                methodInvokeHandlerMap.put(method,
                        fallback == null && httpFallback == null ? httpInvoker
                                : new FallbackMethodInvoker(httpInvoker, method, fallback, httpFallback, proxyTarget.type()));
            }
        }

        // 创建代理
        T proxy = (T) Proxy.newProxyInstance(
                proxyTarget.type().getClassLoader(),
                new Class<?>[]{proxyTarget.type()},
                new ProxyInvoker(proxyTarget, methodInvokeHandlerMap));
        for (DefaultMethodInvoker methodHandler : defaultProxyInvokerList) {
            methodHandler.bindTo(proxy);
        }
        return proxy;
    }
}
