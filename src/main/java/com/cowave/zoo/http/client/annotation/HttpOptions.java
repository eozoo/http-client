package com.cowave.zoo.http.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 超时参数，优先级高于@HttpClient
 *
 * @author shanhuiming
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface HttpOptions {

    /**
     * 连接超时
     */
    int connectTimeout() default -1;

    /**
     * socket超时
     */
    int readTimeout() default -1;

    /**
     * 写入超时
     */
    int writeTimeout() default -1;

    /**
     * 整体调用超时
     */
    int callTimeout() default -1;

    /**
     * 重试次数
     */
    int retryTimes() default -1;

    /**
     * 重试间隔
     */
    int retryInterval() default -1;
}
