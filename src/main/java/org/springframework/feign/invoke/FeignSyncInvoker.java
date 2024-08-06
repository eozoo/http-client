package org.springframework.feign.invoke;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.*;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.codec.FeignDecoder;
import org.springframework.feign.codec.HttpResponse;
import org.springframework.feign.invoke.method.FeignMethodMetadata;
import org.springframework.feign.invoke.template.FeignRequestTemplate;
import org.springframework.feign.invoke.template.FeignTemplateFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static feign.Util.checkNotNull;
import static java.lang.String.format;

/**
 * @author shanhuiming
 */
public class FeignSyncInvoker implements InvocationHandlerFactory.MethodHandler {
    private static final long MAX_RESPONSE_BUFFER_SIZE = 8192L;

    private static final ObjectMapper RESPONSE_MAPEER = new ObjectMapper();

    static{
        RESPONSE_MAPEER.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .registerModules(Collections.emptyList());
        RESPONSE_MAPEER.setTimeZone(TimeZone.getDefault());
    }

    private final org.slf4j.Logger logger;
    private final FeignMethodMetadata metadata;
    private final Target<?> target;
    private final Client client;
    private final org.springframework.feign.retryer.Retryer retryer;
    private final List<RequestInterceptor> requestInterceptors;
    private final FeignTemplateFactory buildTemplateFromArgs;
    private Request.Options options;
    private final FeignDecoder decoder;

    private final FeignExceptionHandler exceptionHandler;

    public FeignSyncInvoker(Target<?> target, Client client,
                            org.springframework.feign.retryer.Retryer retryer,
                            List<RequestInterceptor> requestInterceptors,
                            FeignMethodMetadata metadata,
                            FeignTemplateFactory buildTemplateFromArgs,
                            Request.Options options,
                            FeignDecoder decoder,
                            org.slf4j.Logger logger,
                            FeignExceptionHandler exceptionHandler) {
        this.target = checkNotNull(target, "target");
        this.client = checkNotNull(client, "client for %s", target);
        this.retryer = checkNotNull(retryer, "retryer for %s", target);
        this.requestInterceptors = checkNotNull(requestInterceptors, "requestInterceptors for %s", target);
        this.metadata = checkNotNull(metadata, "metadata for %s", target);
        this.buildTemplateFromArgs = checkNotNull(buildTemplateFromArgs, "metadata for %s", target);
        this.options = checkNotNull(options, "options for %s", target);
        this.decoder = checkNotNull(decoder, "decoder for %s", target);
        this.logger = checkNotNull(logger, "decoder for %s", target);
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        FeignRequestTemplate template = buildTemplateFromArgs.create(argv);
        org.springframework.feign.retryer.Retryer retryer = this.retryer.clone();
        while (true) {
            try {
                return executeAndDecode(template);
            } catch (RetryableException e) {
                retryer.continueOrPropagate(e);
            }
        }
    }

    // TODO 下载
    Object executeAndDecode(FeignRequestTemplate template) throws Throwable {
        Type returnType = metadata.returnType();
        Type httpType = getParamTypeOf(returnType, HttpResponse.class);

        Request request = targetRequest(template);
        String url = request.url();
        Response response;
        long start = System.nanoTime();
        try {
            // http调用
            response = client.execute(request, options);
        } catch (IOException e){
            long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.error(">< {}ms {} {}", cost, e.getMessage(), url);
            if(httpType != null){
                // 如果响应类型是HttpResponse，没有调成功也构造一个HttpResponse返回，防止上层需要识别处理
                return new HttpResponse<>(500, new HttpHeaders(), e.getMessage());
            }else{
                throw new RemoteException(url, format("%sms %s %s", cost, e.getMessage(), url));
            }
        }

        long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        int status = response.status();
        try {

            // 1.响应类型: feign.Response
            if (Response.class.equals(returnType)) {
                return parseFeignResponse(response, status, url, cost);
            }

            Type entityType = getParamTypeOf(returnType, ResponseEntity.class);
            // 2.响应类型: org.springframework.http.ResponseEntity
            if(entityType != null){
                return parseResponseEntity(entityType, response, status, url, cost);
            }

            // 3.响应类型: org.springframework.feign.codec.HttpResponse
            if(httpType != null){
                return parseHttpResponse(httpType, response, status, url, cost);
            }

            // 4.decoder解码
            if (status == 200) {
                return decoder.decode(response, metadata.returnType(), url, cost, status, logger);
            }

            String body = null;
            if (response.body() != null) {
                body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
                logger.error(">< {} {}ms {} {}", status, cost, url, body);
            }else{
                logger.error(">< {} {}ms {}", status, cost, url);
            }
            throw new RemoteException(url, status, null, body);
        } catch(RemoteException e) {
            if(exceptionHandler != null){
                exceptionHandler.handle(e);
            }
            throw e;
        } catch (IOException e) {
            logger.error(">< {}ms {} {}", cost, e.getMessage(), url);
            throw new RemoteException(url, format("%sms %s %s", cost, e.getMessage(), url));
        }
    }

    private HttpResponse<?> parseHttpResponse(Type paramType, Response response, int status, String url, long cost) throws IOException {
        String requestId = "";
        // Header信息
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            if(entry.getKey().equals("requestId")){
                Collection<String> collection = entry.getValue();
                if(!CollectionUtils.isEmpty(collection)){
                    requestId = collection.stream().findFirst().get();
                }
            }
            headers.put(entry.getKey(), entry.getValue().stream().toList());
        }

        // 获取响应主体
        String body = null;
        if (response.body() != null) {
            body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
        }

        if(status == 200){
            logger.info(">< {} {}ms {} {}", status, cost, requestId, url);
            if(body == null || paramType.equals(String.class)){
                return new HttpResponse<>(response.status(), headers, body);
            }else{
                return new HttpResponse<>(response.status(), headers, readType(body, paramType));
            }
        }else if(status > 200 && status < 300){
            logger.warn(">< {} {}ms {} {} {}", status, cost, requestId, url, body);
            HttpResponse<?> httpResponse = new HttpResponse<>(response.status(), headers, null);
            httpResponse.setMessage(body);
            return httpResponse;
        }else{
            logger.error(">< {} {}ms {} {} {}", status, cost, requestId, url, body);
            HttpResponse<?> httpResponse = new HttpResponse<>(response.status(), headers, null);
            httpResponse.setMessage(body);
            return httpResponse;
        }
    }

    private ResponseEntity<?> parseResponseEntity(Type paramType, Response response, int status, String url, long cost) throws IOException {
        // Header信息
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            headers.put(entry.getKey(), entry.getValue().stream().toList());
        }

        // 获取响应主体
        String body = null;
        if (response.body() != null) {
            body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
        }

        if(status >= 200 && status < 300){
            logger.info(">< {} {}ms {}", status, cost, url);
            if(body == null || paramType.equals(String.class)){
                return new ResponseEntity<>(body, headers, response.status());
            }else{
                return new ResponseEntity<>(readType(body, paramType), headers, response.status());
            }
        }else if(body == null){
            logger.error(">< {} {}ms {}", status, cost, url);
        }else{
            logger.error(">< {} {}ms {} {}", status, cost, url, body);
        }
        return new ResponseEntity<>(body, headers, response.status());
    }

    private Response parseFeignResponse(Response response, int status, String url, long cost) throws IOException {
        if (response.body() == null) {
            logging(status, cost, url, null);
            return response;
        }

        if (response.body().length() == null || response.body().length() > MAX_RESPONSE_BUFFER_SIZE) {
            logging(status, cost, url, null);
            return response; // 未关闭流
        }

        String body = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
        logging(status, cost, url, body);
        return Response.create(status, response.reason(), response.headers(), body, StandardCharsets.UTF_8);
    }

    private Type getParamTypeOf(Type type, Class<?> clazz) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> && clazz.equals(rawType)) {
                Type[] paramTypes = parameterizedType.getActualTypeArguments();
                if (paramTypes == null || paramTypes.length == 0) {
                    return Object.class;
                } else {
                    return paramTypes[0];
                }
            }
        }
        return null;
    }

    private void logging(int status, long cost, String url, String body){
        if(status >= 200 && status < 300){
            logger.info(">< {} {}ms {}", status, cost, url);
        }else if(body == null){
            logger.error(">< {} {}ms {}", status, cost, url);
        }else{
            logger.error(">< {} {}ms {} {}", status, cost, url, body);
        }
    }

    Request targetRequest(FeignRequestTemplate feignTemplate) {
        for (RequestInterceptor interceptor : requestInterceptors) {
            interceptor.apply(feignTemplate.getTemplate());
        }
        if(target instanceof FeignTarget feignTarget){
            // 方法Options注解指定的超时优先级更高
            int connectTimeoutMillis = options.connectTimeoutMillis();
            int readTimeoutMillis = options.readTimeoutMillis();
            if(metadata.connectTimeoutMillis() != -1){
                connectTimeoutMillis = metadata.connectTimeoutMillis();
            }
            if(metadata.readTimeoutMillis() != -1){
                readTimeoutMillis = metadata.readTimeoutMillis();
            }
            this.options = new Request.Options(connectTimeoutMillis, readTimeoutMillis);
            // 设置url
            return feignTarget.apply(new RequestTemplate(feignTemplate.getTemplate()), feignTemplate.getHostUrl());
        }else{
            return target.apply(new RequestTemplate(feignTemplate.getTemplate()));
        }
    }

    private static Object readType(String json, Type type) throws IOException {
        return RESPONSE_MAPEER.readValue(json, RESPONSE_MAPEER.constructType(type));
    }
}
