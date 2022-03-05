package org.springframework.feign;

import java.lang.reflect.Constructor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.lang.NonNull;
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
	public void setEmbeddedValueResolver(@NonNull StringValueResolver valueResolver) {
		this.valueResolver = valueResolver;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		FeignClient feign = AnnotationUtils.getAnnotation(feignClass, FeignClient.class);

		String url = valueResolver.resolveStringValue(feign.url());
		int connectTimeoutMillis = getInt(feign.connectTimeoutMillis(), feign.connectTimeoutMillisStr());
		int readTimeoutMillis = getInt(feign.readTimeoutMillis(), feign.readTimeoutMillisStr());
		long period = getLong(feign.period(), feign.periodStr());
		long maxPeriod = getLong(feign.maxPeriod(), feign.maxPeriodStr());
		int maxAttempts = getInt(feign.maxAttempts(), feign.maxAttemptsStr());

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

	private int getInt(int defaultValue, String regex) {
		if(StringUtils.isEmpty(regex)){
			return defaultValue;
		}
		return Integer.parseInt(valueResolver.resolveStringValue(regex));
	}

	private long getLong(long defaultValue, String regex) {
		if(StringUtils.isEmpty(regex)){
			return defaultValue;
		}
		return Long.parseLong(valueResolver.resolveStringValue(regex));
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
