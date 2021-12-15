package org.springframework.feign.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.feign.NULL;

import feign.codec.Decoder;
import feign.codec.Encoder;

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

	Class<?> encoder() default Encoder.Default.class;
	
	Class<?> decoder() default Decoder.Default.class;
	
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
