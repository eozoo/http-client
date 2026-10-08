package com.cowave.zoo.http.client.register;

import com.cowave.zoo.http.client.annotation.HttpClient;
import com.cowave.zoo.http.client.invoke.exec.OkHttpExecutorFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.NonNull;

import java.util.*;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpClientBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata meta, @NonNull BeanDefinitionRegistry registry) {
        // 在spring中注册一个OkHttpExecutorFactory
        String clientFactoryName = OkHttpExecutorFactory.class.getName();
        if (!registry.containsBeanDefinition(clientFactoryName)) {
            registry.registerBeanDefinition(clientFactoryName,
                    BeanDefinitionBuilder.genericBeanDefinition(OkHttpExecutorFactory.class)
                            .setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR).getBeanDefinition());
        }
        // 寻找注册@HttpClient
        TreeSet<String> packageSet = new TreeSet<>();
        String[] beanNames = registry.getBeanDefinitionNames();
        // ImportBeanDefinitionRegistrar 的导入类通常就是启动类，直接纳入其所在包，
        // 避免注册阶段尚未完成时从 BeanDefinitionRegistry 中取不到启动类。
        try {
            Class<?> importingClass = Class.forName(meta.getClassName());
            packageSet.add(importingClass.getPackage().getName());
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException("", e);
        }

        // 省掉package指定扫描，就检查下启动类下的路径，以及@ComponentScan指定的路径
        try {
            for (String beanName : beanNames) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                String className = beanDefinition.getBeanClassName();
                if (className != null) {
                    Class<?> clazz = Class.forName(className);

                    // 启动类路径
                    SpringBootApplication springBoot = clazz.getAnnotation(SpringBootApplication.class);
                    if (springBoot != null) {
                        packageSet.add(clazz.getPackage().getName());
                    }

                    // 扫描类路径
                    ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
                    if (componentScan != null) {
                        String[] values = componentScan.value();
                        String[] basePackages = componentScan.basePackages();
                        packageSet.addAll(Arrays.asList(values));
                        packageSet.addAll(Arrays.asList(basePackages));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException("", e);
        }

        List<String> packageList = new ArrayList<>(packageSet);

        // 合并package
        List<String> packages = new ArrayList<>();
        for(int right = packageList.size() - 1; right >= 0; right--){
            boolean hasPrefix = false;
            for(int i = right - 1; i >= 0; i--){
                if(packageList.get(right).startsWith(packageList.get(i) + ".")){
                    hasPrefix = true;
                    break;
                }
            }
            if(hasPrefix){
                continue;
            }
            packages.add(packageList.get(right));
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(MetadataReader metadataReader) {
                        return true;
                    }

                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return true;
                    }
                };
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        scanner.setResourceLoader(new PathMatchingResourcePatternResolver(
                new DefaultResourceLoader(classLoader)));
        scanner.addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) ->
                metadataReader.getAnnotationMetadata().hasAnnotation(HttpClient.class.getName()));

        for (String pack : packages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(pack)) {
                String className = candidate.getBeanClassName();
                if (className == null) {
                    continue;
                }

                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    HttpClient feign = AnnotationUtils.getAnnotation(clazz, HttpClient.class);
                    if (feign == null) {
                        continue;
                    }

                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                    GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
                    beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                    beanDefinition.getPropertyValues().add("targetClass", clazz);
                    beanDefinition.setBeanClass(HttpClientFactoryBean.class);
                    // 降级实现也实现同一接口，按接口注入时优先使用HTTP代理
                    beanDefinition.setPrimary(true);
                    registry.registerBeanDefinition(clazz.getSimpleName(), beanDefinition);
                } catch (ClassNotFoundException e) {
                    throw new ApplicationContextException("", e);
                }
            }
        }
    }
}
