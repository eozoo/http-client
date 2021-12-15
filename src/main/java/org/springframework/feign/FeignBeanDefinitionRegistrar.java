package org.springframework.feign;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.feign.annotation.FeignScan;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class FeignBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata meta, BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes.fromMap(meta.getAnnotationAttributes(FeignScan.class.getName()));
		if(attrs == null){
			return;
		}
		
		String[] basePackages = (String[])attrs.get("basePackages");
		for(String pack : basePackages){
			Reflections reflections = new Reflections(pack, new Scanner[0]);
			for(Class<?> clazz : reflections.getTypesAnnotatedWith(FeignClient.class)){
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
