package org.springframework.feign;

import java.lang.reflect.Constructor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import feign.Client;
import feign.Feign;
import feign.Feign.Builder;
import feign.Request.Options;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class FeignFactory<T> implements FactoryBean<T>, EmbeddedValueResolverAware {

	private Class<?> feignClass;

	private StringValueResolver valueResolver;

	@Override
	public void setEmbeddedValueResolver(StringValueResolver valueResolver) {
		this.valueResolver = valueResolver;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		FeignClient feign = AnnotationUtils.getAnnotation(feignClass, FeignClient.class);

		String url = valueResolver.resolveStringValue(feign.url());

		int connectTimeoutMillis = feign.connectTimeoutMillis();
		if(!StringUtils.isEmpty(feign.connectTimeoutMillisStr())){
			connectTimeoutMillis = Integer.parseInt(feign.connectTimeoutMillisStr());
		}

		int readTimeoutMillis = feign.readTimeoutMillis();
		if(!StringUtils.isEmpty(feign.readTimeoutMillisStr())){
			readTimeoutMillis = Integer.parseInt(feign.readTimeoutMillisStr());
		}

		long period = feign.period();
		if(!StringUtils.isEmpty(feign.periodStr())){
			period = Integer.parseInt(feign.periodStr());
		}

		long maxPeriod = feign.maxPeriod();
		if(!StringUtils.isEmpty(feign.maxPeriodStr())){
			maxPeriod = Integer.parseInt(feign.maxPeriodStr());
		}

		int maxAttempts = feign.maxAttempts();
		if(!StringUtils.isEmpty(feign.maxAttemptsStr())){
			maxAttempts = Integer.parseInt(feign.maxAttemptsStr());
		}

		Builder builder = Feign.builder()
				.options(new Options(connectTimeoutMillis, readTimeoutMillis)) 
				.retryer(new Retryer.Default(period, maxPeriod, maxAttempts)) 
				.encoder((Encoder)feign.encoder().newInstance()).decoder((Decoder)feign.decoder().newInstance());
		
		Class<?> sslSocketFactoryClass = feign.sslSocketFactory();
		Class<?> hostnameVerifierClass = feign.hostnameVerifier();
		if(SSLSocketFactory.class.isAssignableFrom(sslSocketFactoryClass)){
			SSLSocketFactory sslSocketFactory = null;
			if(!StringUtils.isEmpty(feign.sslCertPath()) && !StringUtils.isEmpty(feign.sslPasswd())){
				Constructor<?> constructor = sslSocketFactoryClass.getConstructor(String.class, String.class);
				sslSocketFactory = (SSLSocketFactory)constructor.newInstance(feign.sslCertPath(), feign.sslPasswd());
			}else{
				sslSocketFactory = (SSLSocketFactory)sslSocketFactoryClass.newInstance();
			}
			
			if(!HostnameVerifier.class.isAssignableFrom(hostnameVerifierClass)){
				builder.client(new Client.Default(sslSocketFactory, null));
			}else{
				builder.client(new Client.Default(sslSocketFactory, (HostnameVerifier)hostnameVerifierClass.newInstance()));
			}
		}
		return	(T)builder.target(feignClass, url);
	}

	@Override
	public Class<?> getObjectType() {
		return feignClass;
	}

	public Class<?> getFeignClass() {
		return feignClass;
	}

	public void setFeignClass(Class<?> feignClass) {
		this.feignClass = feignClass;
	}
}
