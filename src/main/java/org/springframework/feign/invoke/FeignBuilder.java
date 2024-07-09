package org.springframework.feign.invoke;

import feign.*;
import feign.codec.Encoder;
import org.springframework.context.ApplicationContext;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.codec.FeignDecoder;
import org.springframework.feign.retryer.DefaultRetryer;
import org.springframework.util.StringValueResolver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBuilder {
    private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private final Contract contract = new Contract.Default();
    private Client client = new Client.Default(null, null);
    private org.springframework.feign.retryer.Retryer retryer = new DefaultRetryer();
    private Encoder encoder = new Encoder.Default();
    private FeignDecoder decoder = new FeignDecoder.StringDecoder();
    private Request.Options options = new Request.Options();
    private InvocationHandlerFactory invocationHandlerFactory = new InvocationHandlerFactory.Default();

    private FeignExceptionHandler exceptionHandler;

    public void client(Client client) {
        this.client = client;
    }

    public FeignBuilder retryer(org.springframework.feign.retryer.Retryer retryer) {
        this.retryer = retryer;
        return this;
    }

    public FeignBuilder encoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public FeignBuilder decoder(FeignDecoder decoder) {
        this.decoder = decoder;
        return this;
    }

    public FeignBuilder options(Request.Options options) {
        this.options = options;
        return this;
    }

    /**
     * Adds a single request interceptor to the builder.
     */
    public FeignBuilder requestInterceptor(RequestInterceptor requestInterceptor) {
        this.requestInterceptors.add(requestInterceptor);
        return this;
    }

    public FeignBuilder exceptionHandler(FeignExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Allows you to override how reflective dispatch works inside of Feign.
     */
    public FeignBuilder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
        this.invocationHandlerFactory = invocationHandlerFactory;
        return this;
    }

    public <T> T target(Class<T> apiType, String url, String name,
                        ApplicationContext applicationContext, StringValueResolver valueResolver, org.slf4j.Logger logger) {
        return target(new FeignTarget<>(apiType, name, url, applicationContext, valueResolver), logger);
    }

    public <T> T target(Target<T> target, org.slf4j.Logger logger) {
        return build(logger).newInstance(target);
    }

    public Feign build(org.slf4j.Logger logger) {
        FeignMethodHandlerFactory methodHandlerFactory =
                new FeignMethodHandlerFactory(client, retryer, requestInterceptors, exceptionHandler);
        FeignParseHandlersByName parseHandlersByName =
                new FeignParseHandlersByName(contract, options, encoder, decoder, methodHandlerFactory, logger);
        return new FeignImplement(parseHandlersByName, invocationHandlerFactory);
    }
}
