package org.springframework.feign.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.feign.FeignBeanDefinitionRegistrar;

import java.lang.annotation.*;

/**
 *
 * @author shanhuiming
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FeignBeanDefinitionRegistrar.class)
public @interface FeignScan {

	String[] basePackages() default{};
}
