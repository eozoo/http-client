package org.springframework.feign;

import org.reflections.Reflections;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.feign.annotation.FeignScan;
import org.springframework.lang.NonNull;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata meta, @NonNull BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes.fromMap(meta.getAnnotationAttributes(FeignScan.class.getName()));
		if(attrs == null){
			return;
		}

		String[] basePackages = (String[])attrs.get("basePackages");
		for(String pack : basePackages){
			Reflections reflections = new Reflections(pack);
			for(Class<?> clazz : reflections.getTypesAnnotatedWith(FeignClient.class)){
				FeignClient feign = AnnotationUtils.getAnnotation(clazz, FeignClient.class);
				if(feign == null){
					continue;
				}

				BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);

				GenericBeanDefinition beanDefinition = (GenericBeanDefinition)builder.getBeanDefinition();
				beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
				beanDefinition.getPropertyValues().add("feignClass", clazz);
				beanDefinition.setBeanClass(FeignFactory.class);

				registry.registerBeanDefinition(clazz.getSimpleName(), beanDefinition);
			}
		}
	}
}
