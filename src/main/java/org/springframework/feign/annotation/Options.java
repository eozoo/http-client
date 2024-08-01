package org.springframework.feign.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author shanhuiming
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Options {

    int connectTimeoutMillis() default -1;

    int readTimeoutMillis() default -1;
}
