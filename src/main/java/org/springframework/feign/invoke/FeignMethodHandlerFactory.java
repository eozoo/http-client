package org.springframework.feign.invoke;

import feign.*;
import org.springframework.feign.codec.FeignDecoder;
import org.springframework.feign.invoke.template.FeignTemplateFactory;

import java.util.List;

import static feign.Util.checkNotNull;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignMethodHandlerFactory {
    private final Client client;
    private final org.springframework.feign.retryer.Retryer retryer;
    private final List<RequestInterceptor> requestInterceptors;

    FeignMethodHandlerFactory(Client client, org.springframework.feign.retryer.Retryer retryer,
                              List<RequestInterceptor> requestInterceptors) {
        this.client = checkNotNull(client, "client");
        this.retryer = checkNotNull(retryer, "retryer");
        this.requestInterceptors = checkNotNull(requestInterceptors, "requestInterceptors");
    }

    public InvocationHandlerFactory.MethodHandler create(Target<?> target, MethodMetadata md,
                                                         FeignTemplateFactory buildTemplateFromArgs, Request.Options options, FeignDecoder decoder, org.slf4j.Logger logger) {
        return new FeignSynchronousMethodHandler(target,
                client, retryer, requestInterceptors, md, buildTemplateFromArgs, options, decoder, logger);
    }
}
