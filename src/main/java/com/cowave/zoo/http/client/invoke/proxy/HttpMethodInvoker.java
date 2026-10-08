package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.invoke.exec.HttpExecutor;
import com.cowave.zoo.http.client.response.HttpResponse;
import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import com.cowave.zoo.http.client.request.Options;
import com.cowave.zoo.http.client.request.HttpRequestTemplate;
import com.cowave.zoo.http.client.request.HttpRequest;
import com.cowave.zoo.http.client.request.HttpRequestFactory;
import com.cowave.zoo.http.client.request.meta.HttpMethodMeta;
import com.cowave.zoo.http.client.asserts.HttpException;
import com.cowave.zoo.http.client.asserts.HttpHintException;
import lombok.extern.slf4j.Slf4j;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cowave.zoo.http.client.constants.HttpCode.SERVICE_ERROR;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class HttpMethodInvoker implements MethodInvoker {
    private final boolean ignoreError;
    private final HttpMethodMeta metadata;
    private final ProxyTarget<?> proxyTarget;
    private final HttpExecutor httpExecutor;
    private final List<HttpClientInterceptor> httpClientInterceptors;
    private final HttpRequestFactory httpRequestFactory;
    private final Options options;
    private final Set<Integer> retryForStatus;
    private final List<Class<? extends Exception>> retryForExceptions;
    private final HttpDecoder decoder;

    public HttpMethodInvoker(ProxyTarget<?> proxyTarget,
                             HttpMethodMeta metadata,
                             HttpRequestFactory httpRequestFactory,
                             HttpExecutor httpExecutor,
                             List<HttpClientInterceptor> httpClientInterceptors,
                             Options options,
                             Set<Integer> retryForStatus,
                             List<Class<? extends Exception>> retryForExceptions,
                             HttpDecoder decoder,
                             boolean ignoreError) {
        this.proxyTarget = proxyTarget;
        this.httpExecutor = httpExecutor;
        this.httpClientInterceptors = httpClientInterceptors;
        this.metadata = metadata;
        this.httpRequestFactory = httpRequestFactory;
        this.options = options;
        this.retryForStatus = retryForStatus;
        this.retryForExceptions = retryForExceptions;
        this.decoder = decoder;
        this.ignoreError = ignoreError;
    }

    @Override
    public Object invoke(Object[] args) throws Throwable {
        HttpRequest httpRequest = httpRequestFactory.create(args);
        // 确保上传文件流释放
        try (InputStream file = httpRequest.getMultiFile()) {
            return executeAndDecode(httpRequest);
        }
    }

    Object executeAndDecode(HttpRequest httpRequest) throws Throwable {
        Type returnType = metadata.getReturnType();
        Type httpType = getParamTypeOf(returnType, HttpResponse.class);

        // Http请求
        HttpRequestTemplate httpRequestTemplate = applyTemplate(httpRequest);
        String url = httpRequestTemplate.getUrl();
        String method = httpRequestTemplate.getMethod();
        String logUrl = HttpRequestTemplate.logUrl(url);

        long start = System.nanoTime();
        HttpResponseTemplate httpResponseTemplate;
        try {
            httpResponseTemplate = executeRequest(httpRequestTemplate);
        } catch (IOException e) {
            log.error(">< {} {} {}", e.getMessage(), method, logUrl, e);
            return throwOrReturn(httpType, new HttpHintException(e, "Remote failed"));
        }

        long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        int status = httpResponseTemplate.getStatus();
        try {
            // 响应类型: HttpResponse
            if (httpType != null) {
                return parseHttpResponse(httpType, httpResponseTemplate, status, method, url, cost);
            }

            // decoder解码
            if (status >= 200 && status < 300) {
                if (status == 204) {
                    if (log.isInfoEnabled()) {
                        log.info(">< {} {}ms {} {}", status, cost, method, logUrl);
                    }
                    return null;
                }
                return decoder.decode(httpResponseTemplate, metadata.getReturnType(), url, cost, status);
            }

            String reason = httpResponseTemplate.getReason();
            if (httpResponseTemplate.getInputStream() != null) {
                reason = StreamUtils.copyToString(httpResponseTemplate.getInputStream(), StandardCharsets.UTF_8);
            }

            if(StringUtils.hasText(reason)){
                log.error(">< {} {}ms {} {} {}", status, cost, method, logUrl, reason);
            }else {
                log.error(">< {} {}ms {} {}", status, cost, method, logUrl);
            }

            HttpResponse<String> failedResponse = new HttpResponse<>(httpResponseTemplate.getRemoteHeaders(), status, reason);
            failedResponse.setMessage(reason);
            throw new HttpResponseException(failedResponse);
        } catch (HttpException e) {
            return throwOrReturn(httpType, e);
        } catch (Exception e) {
            log.error(">< {}ms {} {} {}", cost, e.getMessage(), method, logUrl, e);
            HttpResponse<Void> failedResponse = new HttpResponse<>(httpResponseTemplate.getRemoteHeaders(), status, null);
            failedResponse.setCause(e);
            return throwOrReturn(httpType, new HttpResponseException(e, failedResponse));
        } finally {
            if(httpResponseTemplate.isShouldClose()){
                httpResponseTemplate.close();
            }
        }
    }

    private HttpResponseTemplate executeRequest(HttpRequestTemplate request) throws Exception {
        for (int attempt = 0; ; attempt++) {
            // 整体预算耗尽后直接结束，不因配置了超时异常重试而继续调用
            request.remainingCallTimeout();

            HttpResponseTemplate response;
            try {
                response = httpExecutor.execute(request);
            } catch (Exception exception) {
                // 没有超出重试次数，且异常类型匹配则进行重试，否则直接抛出异常
                if (!canRetry(request, attempt) || !retryForException(exception)) {
                    throw exception;
                }
                // 等待重试间隔
                awaitRetry(request);
                continue;
            }

            // 没有超出重试次数，且状态码匹配则进行重试，否则直接返回response
            if (!canRetry(request, attempt) || !retryForStatus.contains(response.getStatus())) {
                return response;
            }
            // 状态码重试之前先释放上一次响应
            response.close();
            // 等待重试间隔
            awaitRetry(request);
        }
    }

    // 重试次数限制，上传流及已中断的调用不重试
    private boolean canRetry(HttpRequestTemplate request, int attempt) {
        return attempt < request.getRetryTimes() && request.getMultiFile() == null
                && !Thread.currentThread().isInterrupted();
    }

    // 按异常类型匹配，配置的父类包含其子类，不匹配Error
    private boolean retryForException(Exception exception) {
        for (Class<? extends Exception> type : retryForExceptions) {
            if (type.isInstance(exception)) {
                return true;
            }
        }
        return false;
    }

    // 固定间隔等待重试，共享整体调用超时，中断时立即结束
    private void awaitRetry(HttpRequestTemplate request) throws IOException {
        long remaining = request.remainingCallTimeout();
        if (remaining != 0 && remaining <= request.getRetryInterval()) {
            throw new SocketTimeoutException("HTTP call timeout before retry");
        }

        try {
            Thread.sleep(request.getRetryInterval());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            InterruptedIOException exception = new InterruptedIOException("HTTP retry interrupted");
            exception.initCause(interrupted);
            throw exception;
        }
    }

    private Object throwOrReturn(Type httpType, Exception exception) throws Throwable {
        if (ignoreError && httpType != null) {
            HttpResponse<?> httpResponse = new HttpResponse<>(SERVICE_ERROR);
            httpResponse.setCause(exception);
            httpResponse.setMessage(exception.getMessage());
            return httpResponse;
        }
        throw exception;
    }

    private HttpResponse<?> parseHttpResponse(Type paramType, HttpResponseTemplate response,
                                              int status, String method, String url, long cost) throws Exception {
        String logUrl = HttpRequestTemplate.logUrl(url);
        // 无内容响应保留状态及响应头，不进行解码
        if (status == 204) {
            if (log.isInfoEnabled()) {
                log.info(">< {} {}ms {} {}", status, cost, method, logUrl);
            }
            return new HttpResponse<>(response.getRemoteHeaders(), status, null);
        }
        // InputStream交给调用者处理
        if (paramType.equals(InputStream.class)) {
            if (status >= 200 && status < 300) {
                if (log.isInfoEnabled()) {
                    log.info(">< {} {}ms {} {}", status, cost, method, logUrl);
                }
            } else {
                log.warn(">< {} {}ms {}, {}", status, cost, method, logUrl);
            }
            if (response.getInputStream() != null) {
                response.setShouldClose(false);
            }
            return new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), response.getInputStream());
        }

        // 为decoder提供可读取的响应流
        byte[] bodyBytes = response.getInputStream() == null ? null
                : StreamUtils.copyToByteArray(response.getInputStream());
        String body = bodyBytes == null ? null : new String(bodyBytes, StandardCharsets.UTF_8);

        if (status >= 200 && status < 300) {
            // 普通对象由decoder记录解码日志，避免成功响应重复打印
            if (log.isInfoEnabled() && (paramType.equals(Void.class)
                    || !StringUtils.hasText(body) || paramType.equals(String.class))) {
                log.info(">< {} {}ms {} {}", status, cost, method, logUrl);
            }
            if (paramType.equals(Void.class) || (!StringUtils.hasText(body) && !paramType.equals(String.class))) {
                return new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), null);
            } else if (paramType.equals(String.class)) {
                return new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), body);
            } else {
                return new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(),
                        decodeBody(bodyBytes, paramType, response, url, cost));
            }
        } else {
            String reason = response.getReason();
            if(body != null){
                reason = body;
            }

            if(StringUtils.hasText(reason)){
                log.error(">< {} {}ms {} {} {}", status, cost, method, logUrl, reason);
            }else {
                log.error(">< {} {}ms {} {}", status, cost, method, logUrl);
            }

            HttpResponse<?> httpResponse;
            if (paramType.equals(Void.class) || (!StringUtils.hasText(body) && !paramType.equals(String.class))) {
                httpResponse = new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), null);
            } else if (paramType.equals(String.class)) {
                httpResponse = new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), body);
            } else {
                Object decoded = null;
                try {
                    decoded = decodeBody(bodyBytes, paramType, response, url, cost);
                } catch (Exception ignored) {
                    // 错误响应无法解码时仍返回原状态和错误文本
                }
                httpResponse = new HttpResponse<>(response.getRemoteHeaders(), response.getStatus(), decoded);
            }
            httpResponse.setMessage(reason);
            return httpResponse;
        }
    }

    private Object decodeBody(byte[] body, Type paramType, HttpResponseTemplate response,
                              String url, long cost) throws Exception {
        HttpResponseTemplate buffered = new HttpResponseTemplate(response.getHttpResponse(), response.getStatus(),
                response.getRemoteHeaders(), response.getReason(), new ByteArrayInputStream(body), (long) body.length);
        return decoder.decode(buffered, paramType, url, cost, response.getStatus());
    }

    private Type getParamTypeOf(Type type, Class<?> clazz) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> && clazz.equals(rawType)) {
                Type[] paramTypes = parameterizedType.getActualTypeArguments();
                if (paramTypes == null || paramTypes.length == 0) {
                    return Object.class;
                } else {
                    return paramTypes[0];
                }
            }
        }
        return null;
    }

    HttpRequestTemplate applyTemplate(HttpRequest httpRequest) throws UnsupportedEncodingException {
        for (HttpClientInterceptor interceptor : httpClientInterceptors) {
            interceptor.apply(httpRequest);
        }

        // 方法Options注解指定的超时优先级更高
        int connectTimeout = options.getConnectTimeout();
        int readTimeout = options.getReadTimeout();
        int writeTimeout = options.getWriteTimeout();
        int callTimeout = options.getCallTimeout();
        int retryTimes = options.getRetryTimes();
        int retryInterval = options.getRetryInterval();
        if (metadata.getConnectTimeout() != -1) {
            connectTimeout = metadata.getConnectTimeout();
        }
        if (metadata.getReadTimeout() != -1) {
            readTimeout = metadata.getReadTimeout();
        }
        if (metadata.getWriteTimeout() != -1) {
            writeTimeout = metadata.getWriteTimeout();
        }
        if (metadata.getCallTimeout() != -1) {
            callTimeout = metadata.getCallTimeout();
        }
        if (metadata.getRetryTimes() != -1) {
            retryTimes = metadata.getRetryTimes();
        }
        if (metadata.getRetryInterval() != -1) {
            retryInterval = metadata.getRetryInterval();
        }
        if (retryTimes < 0 || retryInterval < 0) {
            throw new IllegalArgumentException("Effective retryTimes and retryInterval must not be negative");
        }
        // 设置url及一些参数
        return proxyTarget.apply(httpRequest,
                httpRequest.getHostUrl(), retryTimes, retryInterval, connectTimeout, readTimeout, writeTimeout, callTimeout);
    }
}
