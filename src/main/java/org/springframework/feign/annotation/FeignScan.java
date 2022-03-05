package org.springframework.feign.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.feign.FeignBeanDefinitionRegistrar;

import java.lang.annotation.*;

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
