package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.HttpFallback;
import com.cowave.zoo.http.client.HttpFallbackFactory;
import com.cowave.zoo.http.client.asserts.HttpException;
import com.cowave.zoo.http.client.asserts.HttpHintException;
import com.cowave.zoo.http.client.response.HttpResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.cowave.zoo.http.client.constants.HttpCode.SERVICE_ERROR;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class FallbackMethodInvoker implements MethodInvoker {

    // 原HTTP调用，内部完成重试、响应解析及失败结果包装
    private final MethodInvoker httpInvoker;

    // 降级调用使用同一接口方法及原始参数
    private final Method method;

    // 接口fallback实现
    private final Object fallback;

    // 全局默认fallback处理
    private final HttpFallback httpFallback;

    // 原HTTP接口，校验工厂返回的降级实现
    private final Class<?> clientType;

    @Override
    public Object invoke(Object[] args) throws Throwable {
        Object result;
        try {
            // HttpMethodInvoker.invoke
            result = httpInvoker.invoke(args);
        } catch (HttpException exception) {
            // 中断不能被降级为正常返回；参数及编码等其他异常保持原行为
            if (Thread.currentThread().isInterrupted()) {
                throw exception;
            }

            HttpResponse<?> response = exception instanceof HttpResponseException
                    ? ((HttpResponseException) exception).getResponse() : null;
            if (fallback == null) {
                notifyFailure(args, response, exception);
                throw exception;
            }
            // 异常触发降级调用
            return invokeFallback(args, response, exception);
        }

        // HttpResponse及ignoreError可能返回失败结果而不抛异常
        if (result instanceof HttpResponse<?> && ((HttpResponse<?>) result).isFailed()) {
            HttpResponse<?> response = (HttpResponse<?>) result;
            // 将失败包装成一个failure异常
            Throwable failure = response.getCause();
            if (failure == null) {
                failure = new HttpHintException(response.getStatus(), SERVICE_ERROR.getCode(), "Remote failed");
            }

            // 中断直接返回
            if (Thread.currentThread().isInterrupted()) {
                return result;
            }

            // ignoreError包装的网络失败没有远端响应，解码失败则恢复异常携带的响应信息
            HttpResponse<?> failedResponse = failure instanceof HttpResponseException
                    ? ((HttpResponseException) failure).getResponse()
                    : response.getCause() == null ? response : null;

            // 全局处理只记录失败，原响应及响应流仍交给调用方
            if (fallback == null) {
                notifyFailure(args, failedResponse, failure);
                return result;
            }

            // 降级可以读取失败响应，执行结束后关闭不再交给调用方的响应流
            try {
                return invokeFallback(args, failedResponse, failure);
            } finally {
                if (response.getBody() instanceof InputStream) {
                    try {
                        ((InputStream) response.getBody()).close();
                    } catch (IOException exception) {
                        failure.addSuppressed(exception);
                    }
                }
            }
        }
        return result;
    }

    // 全局处理失败时保留其异常信息，不覆盖原失败或阻止响应返回
    private void notifyFailure(Object[] args, HttpResponse<?> response, Throwable failure) {
        try {
            httpFallback.fallback(method, args, response, failure);
        } catch (Exception exception) {
            if (exception != failure) {
                failure.addSuppressed(exception);
            }
        }
    }

    // 尝试降级调用，失败保留原HTTP失败信息
    private Object invokeFallback(Object[] args, HttpResponse<?> response, Throwable failure) throws Throwable {
        try {
            // 原接口直接实现
            Object target = fallback;
            // 通过工厂接口创建
            if (fallback instanceof HttpFallbackFactory<?>) {
                target = ((HttpFallbackFactory<?>) fallback).create(response, failure);
                if (!clientType.isInstance(target)) {
                    throw new IllegalArgumentException("Fallback factory must return an implementation of " + clientType.getName());
                }
            }
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause != failure) {
                cause.addSuppressed(failure);
            }
            throw cause;
        } catch (Exception exception) {
            if (exception != failure) {
                exception.addSuppressed(failure);
            }
            throw exception;
        }
    }
}
