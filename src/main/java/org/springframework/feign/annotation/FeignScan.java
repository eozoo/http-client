package org.springframework.feign.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.feign.FeignBeanDefinitionRegistrar;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FeignBeanDefinitionRegistrar.class)
public @interface FeignScan {

	String[] basePackages() default{};
}
