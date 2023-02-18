package org.springframework.feign.invoke;

import feign.*;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.feign.invoke.template.FeignTemplateFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static feign.Util.checkNotNull;
import static feign.Util.ensureClosed;
import static java.lang.String.format;

/**
 * @author shanhuiming
 */
public class FeignSynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {
    private static final long MAX_RESPONSE_BUFFER_SIZE = 8192L;
    private final org.slf4j.Logger logger;
    private final MethodMetadata metadata;
    private final Target<?> target;
    private final Client client;
    private final org.springframework.feign.retryer.Retryer retryer;
    private final List<RequestInterceptor> requestInterceptors;
    private final FeignTemplateFactory buildTemplateFromArgs;
    private final Request.Options options;
    private final Decoder decoder;

    public FeignSynchronousMethodHandler(Target<?> target, Client client,
                                         org.springframework.feign.retryer.Retryer retryer,
                                         List<RequestInterceptor> requestInterceptors,
                                         MethodMetadata metadata,
                                         FeignTemplateFactory buildTemplateFromArgs,
                                         Request.Options options,
                                         Decoder decoder, org.slf4j.Logger logger) {
        this.target = checkNotNull(target, "target");
        this.client = checkNotNull(client, "client for %s", target);
        this.retryer = checkNotNull(retryer, "retryer for %s", target);
        this.requestInterceptors = checkNotNull(requestInterceptors, "requestInterceptors for %s", target);
        this.metadata = checkNotNull(metadata, "metadata for %s", target);
        this.buildTemplateFromArgs = checkNotNull(buildTemplateFromArgs, "metadata for %s", target);
        this.options = checkNotNull(options, "options for %s", target);
        this.decoder = checkNotNull(decoder, "decoder for %s", target);
        this.logger = checkNotNull(logger, "decoder for %s", target);
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        RequestTemplate template = buildTemplateFromArgs.create(argv);
        org.springframework.feign.retryer.Retryer retryer = this.retryer.clone();
        while (true) {
            try {
                return executeAndDecode(template);
            } catch (RetryableException e) {
                retryer.continueOrPropagate(e);
            }
        }
    }

    Object executeAndDecode(RequestTemplate template) throws Throwable {
        Request request = targetRequest(template);
        String url = request.url();

        Response response;
        long start = System.nanoTime();
        try {
            response = client.execute(request, options);
        } catch (IOException e) {
            long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            throw new RemoteException(format("remote failed %sms %s ", cost, url), e);
        }

        long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        int status = response.status();
        boolean shouldClose = true;
        try {
            if (Response.class == metadata.returnType()) {
                if (response.body() == null) {
                    return response;
                }

                if (response.body().length() == null || response.body().length() > MAX_RESPONSE_BUFFER_SIZE) {
                    shouldClose = false;
                    return response;
                }

                // Ensure the response body is disconnected
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                return Response.create(response.status(), response.reason(), response.headers(), bodyData);
            }

            if (response.status() >= 200 && response.status() < 300) {
                logger.info(">< remote   {} {}ms {}", status, cost, url);
                if (void.class == metadata.returnType()) {
                    return null;
                } else {
                    return decode(response);
                }
            }
            throw new RemoteException(format("remote[%s] %sms %s", status, cost, url));
        } catch (IOException e) {
            throw new RemoteException(format("remote failed %sms %s ", cost, url), e);
        } finally {
            if (shouldClose) {
                ensureClosed(response.body());
            }
        }
    }

    Request targetRequest(RequestTemplate template) {
        for (RequestInterceptor interceptor : requestInterceptors) {
            interceptor.apply(template);
        }
        return target.apply(new RequestTemplate(template));
    }

    Object decode(Response response) throws Throwable {
        try {
            return decoder.decode(response, metadata.returnType());
        } catch (FeignException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DecodeException(e.getMessage(), e);
        }
    }
}
