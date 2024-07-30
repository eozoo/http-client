package org.springframework.feign.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.feign.FeignBeanDefinitionRegistrar;

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
@Import(FeignBeanDefinitionRegistrar.class)
public @interface FeignScan {

	String[] basePackages() default{};
}
