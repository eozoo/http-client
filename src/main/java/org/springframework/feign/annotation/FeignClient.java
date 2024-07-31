package org.springframework.feign.annotation;

import feign.Feign;
import org.springframework.core.annotation.AliasFor;
import org.springframework.feign.NULL;
import org.springframework.feign.codec.EJacksonDecoder;
import org.springframework.feign.codec.EJacksonEncoder;
import org.springframework.stereotype.Component;

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
public @interface FeignClient {

	@AliasFor(attribute = "url")
	String value() default "";

	@AliasFor(attribute = "value")
	String url() default "";

	String name() default "";

	Class<?> encoder() default EJacksonEncoder.class;

	Class<?> decoder() default EJacksonDecoder.class;

	Class<?> logger() default Feign.class;

	Class<?> sslSocketFactory() default NULL.class;

	Class<?> hostnameVerifier() default NULL.class;

	String sslPasswd() default "";

	String sslCertPath() default "";

	int connectTimeoutMillis() default 3000;

	String connectTimeoutMillisStr() default "";

	int readTimeoutMillis() default 30000;

	String readTimeoutMillisStr() default "";

	int maxAttempts() default 1;

	String maxAttemptsStr() default "";

	long period() default 1000;

	String periodStr() default "";

	long maxPeriod() default 1000;

	String maxPeriodStr() default "";
}
