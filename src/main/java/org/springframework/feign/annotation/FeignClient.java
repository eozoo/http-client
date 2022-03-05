package org.springframework.feign.annotation;

import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.core.annotation.AliasFor;
import org.springframework.feign.NULL;

import java.lang.annotation.*;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignClient {
	
	@AliasFor(attribute = "url")
	String value() default "";
	
	@AliasFor(attribute = "value")
	String url() default "";

	Class<?> encoder() default JacksonEncoder.class;
	
	Class<?> decoder() default JacksonDecoder.class;
	
	Class<?> sslSocketFactory() default NULL.class;
	
	Class<?> hostnameVerifier() default NULL.class;
	
	String sslPasswd() default "";
	
	String sslCertPath() default "";
	
	int connectTimeoutMillis() default 1000;
	
	String connectTimeoutMillisStr() default "";
	
	int readTimeoutMillis() default 1000;
	
	String readTimeoutMillisStr() default "";
	
	int maxAttempts() default 1;
	
	String maxAttemptsStr() default "";
	
	long period() default 1000;
	
	String periodStr() default "";
	
	long maxPeriod() default 1000;
	
	String maxPeriodStr() default "";
}
