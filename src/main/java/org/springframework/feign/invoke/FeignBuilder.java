package org.springframework.feign.invoke;

import feign.*;
import feign.codec.Encoder;
import org.springframework.context.ApplicationContext;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.codec.FeignDecoder;
import org.springframework.feign.invoke.method.DefaultFeignContract;
import org.springframework.feign.invoke.method.FeignContract;
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
    private Client client = new Client.Default(null, null);
    private final FeignContract contract = new DefaultFeignContract();
    private org.springframework.feign.retryer.Retryer retryer = new DefaultRetryer();
    private Encoder encoder = new Encoder.Default();
    private FeignDecoder decoder = new FeignDecoder.StringDecoder();
    private Request.Options options = new Request.Options();
    private InvocationHandlerFactory invocationHandlerFactory = new InvocationHandlerFactory.Default();
    private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
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
                        ApplicationContext applicationContext, StringValueResolver valueResolver, boolean logInfo) {
        return target(new FeignTarget<>(apiType, name, url, applicationContext, valueResolver), logInfo);
    }

    public <T> T target(Target<T> target, boolean logInfo) {
        return build(logInfo).newInstance(target);
    }

    public Feign build(boolean logInfo) {
        FeignMethodHandlerFactory methodHandlerFactory =
                new FeignMethodHandlerFactory(client, retryer, requestInterceptors, exceptionHandler);
        FeignParseHandlersByName parseHandlersByName =
                new FeignParseHandlersByName(contract, options, encoder, decoder, methodHandlerFactory, logInfo);
        return new FeignImplement(parseHandlersByName, invocationHandlerFactory);
    }
}
