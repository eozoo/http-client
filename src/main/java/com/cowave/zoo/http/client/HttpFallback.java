package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.response.HttpResponse;

import java.lang.reflect.Method;

/**
 * @author shanhuiming
 */
public interface HttpFallback {

    /**
     * 未配置类级降级时的全局失败处理，不改变原调用结果
     * 不应修改原始参数或响应，也不应读取、关闭响应流
     *
     * @param method 接口方法
     * @param args 调用参数
     * @param response 失败响应，抛出异常时为null；响应流仍由调用方关闭
     * @param cause 失败原因
     */
    void fallback(Method method, Object[] args, HttpResponse<?> response, Throwable cause);
}
