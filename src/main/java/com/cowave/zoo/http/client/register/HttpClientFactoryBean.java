package com.cowave.zoo.http.client.register;

import com.cowave.zoo.http.client.annotation.HttpClient;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringValueResolver;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author shanhuiming
 *
 */
@Setter
public class HttpClientFactoryBean<T> implements FactoryBean<T>, EmbeddedValueResolverAware, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private StringValueResolver valueResolver;

    private Class<T> targetClass;

    public HttpClientFactoryBean() {

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    @Override
    public T getObject() throws UnsupportedEncodingException {
        HttpClient httpClient = AnnotationUtils.getAnnotation(targetClass, HttpClient.class);
        return HttpProxyFactory.newProxy(targetClass, httpClient, applicationContext, valueResolver);
    }

    @Override
    public Class<?> getObjectType() {
        return targetClass;
    }
}
