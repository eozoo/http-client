package org.springframework.feign.invoke;

import static feign.Util.checkNotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.feign.codec.FeignDecoder;
import org.springframework.feign.invoke.method.FeignContract;
import org.springframework.feign.invoke.method.FeignMethodMetadata;
import org.springframework.feign.invoke.template.FeignBuildEncodedTemplate;
import org.springframework.feign.invoke.template.FeignBuildFormEncodedTemplate;
import org.springframework.feign.invoke.template.FeignTemplateFactory;

import feign.Contract;
import feign.InvocationHandlerFactory;
import feign.MethodMetadata;
import feign.Request;
import feign.Target;
import feign.codec.Encoder;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignParseHandlersByName {
    private final FeignContract contract;
    private final Request.Options options;
    private final Encoder encoder;
    private final FeignDecoder decoder;
    private final FeignMethodHandlerFactory factory;
    private final org.slf4j.Logger logger;

    FeignParseHandlersByName(FeignContract contract, Request.Options options, Encoder encoder, FeignDecoder decoder,
                        FeignMethodHandlerFactory factory, org.slf4j.Logger logger) {
        this.contract = contract;
        this.options = options;
        this.factory = factory;
        this.encoder = checkNotNull(encoder, "encoder");
        this.decoder = checkNotNull(decoder, "decoder");
        this.logger = checkNotNull(logger, "logger");
    }

    public Map<String, InvocationHandlerFactory.MethodHandler> apply(Target<?> key) {
        List<FeignMethodMetadata> metadata = contract.parseAndValidatateMetadata(key.type());

        Map<String, InvocationHandlerFactory.MethodHandler> result = new LinkedHashMap<>();
        for (FeignMethodMetadata md : metadata) {
            FeignTemplateFactory buildTemplate;
            if (!md.formParams().isEmpty() && md.template().bodyTemplate() == null) {
                buildTemplate = new FeignBuildFormEncodedTemplate(md, encoder);
            } else if (md.bodyIndex() != null) {
                buildTemplate = new FeignBuildEncodedTemplate(md, encoder);
            } else {
                buildTemplate = new FeignTemplateFactory(md);
            }
            result.put(md.configKey(), factory.create(key, md, buildTemplate, options, decoder, logger));
        }
        return result;
    }
}
