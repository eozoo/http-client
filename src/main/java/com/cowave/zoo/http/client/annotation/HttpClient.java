package com.cowave.zoo.http.client.annotation;

import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.request.ssl.NoopHostnameVerifier;
import com.cowave.zoo.http.client.request.ssl.NoopTrustManager;
import com.cowave.zoo.http.client.request.ssl.NoopKeyManager;
import org.springframework.core.annotation.AliasFor;
import com.cowave.zoo.http.client.invoke.codec.decoder.JacksonDecoder;
import com.cowave.zoo.http.client.invoke.codec.encoder.JacksonEncoder;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.net.SocketException;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author shanhuiming
 *
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface HttpClient {

    @AliasFor(attribute = "url")
    String value() default "";

    @AliasFor(attribute = "value")
    String url() default "";

    String name() default "";

    /**
     * 连接超时
     */
    int connectTimeout() default 10000;

    /**
     * 连接超时
     */
    String connectTimeoutStr() default "";

    /**
     * socket超时
     */
    int readTimeout() default 60000;

    /**
     * socket超时
     */
    String readTimeoutStr() default "";

    /**
     * 写入超时，0表示不限制
     */
    int writeTimeout() default 0;

    /**
     * 写入超时，0表示不限制
     */
    String writeTimeoutStr() default "";

    /**
     * 整体调用超时（包含重试等待及响应读取），0表示不限制
     */
    int callTimeout() default 0;

    /**
     * 整体调用超时（包含重试等待及响应读取），0表示不限制
     */
    String callTimeoutStr() default "";

    /**
     * 重试次数
     */
    int retryTimes() default 0;

    /**
     * 重试次数
     */
    String retryTimesStr() default "";

    /**
     * 重试间隔
     */
    int retryInterval() default 1000;

    /**
     * 重试间隔
     */
    String retryIntervalStr() default "";

    /**
     * 需要重试的HTTP状态码
     */
    int[] retryForStatus() default {};

    /**
     * 需要重试的异常类型，默认仅SocketException
     */
    Class<? extends Exception>[] retryForExceptions() default {SocketException.class};

    /**
     * 失败降级调用，需实现当前接口或HttpFallbackFactory，并注册为Spring Bean
     */
    Class<?> fallback() default void.class;

    /**
     * 请求拦截处理，在全部全局拦截器之后执行
     */
    Class<? extends HttpClientInterceptor> interceptor() default HttpClientInterceptor.class;

    /**
     * 客户端证书及私钥管理
     */
    Class<? extends X509KeyManager> keyManager() default NoopKeyManager.class;

    /**
     * 服务证书信任管理
     */
    Class<? extends X509TrustManager> trustManager() default NoopTrustManager.class;

    /**
     * 域名信任管理
     */
    Class<? extends HostnameVerifier> hostnameVerifier() default NoopHostnameVerifier.class;

    Class<?> encoder() default JacksonEncoder.class;

    Class<?> decoder() default JacksonDecoder.class;

    boolean ignoreError() default false;
}
