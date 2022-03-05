package org.springframework.feign;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.feign.annotation.FeignScan;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class FeignBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private static final Logger LOG = LoggerFactory.getLogger(FeignBeanDefinitionRegistrar.class);
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata meta, BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes.fromMap(meta.getAnnotationAttributes(FeignScan.class.getName()));
		if(attrs == null){
			return;
		}

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
		Environment environment = beanFactory.getBean(Environment.class);
		
		String[] basePackages = (String[])attrs.get("basePackages");
		for(String pack : basePackages){
			Reflections reflections = new Reflections(pack);
			for(Class<?> clazz : reflections.getTypesAnnotatedWith(FeignClient.class)){
				FeignClient feign = AnnotationUtils.getAnnotation(clazz, FeignClient.class);
				try{
					environment.resolveRequiredPlaceholders(feign.url());
				}catch(IllegalThreadStateException e){
					LOG.info("Skipped Bean of FeignClient[" + clazz.getName() + "] due to url " + feign.url() + " not exist");
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
