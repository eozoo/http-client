package org.springframework.feign.invoke.template;

import feign.MethodMetadata;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.springframework.feign.invoke.method.FeignMethodMetadata;

import java.util.Map;

import static feign.Util.checkArgument;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBuildEncodedTemplate extends FeignTemplateFactory{

    private final Encoder encoder;

    public FeignBuildEncodedTemplate(FeignMethodMetadata metadata, Encoder encoder) {
        super(metadata);
        this.encoder = encoder;
    }

    @Override
    protected FeignRequestTemplate resolve(Object[] argv, RequestTemplate template, Map<String, Object> variables) {
        Object body = argv[metadata.bodyIndex()];
        checkArgument(body != null, "Body parameter %s was null", metadata.bodyIndex());
        // 编码template.body
        try {
            encoder.encode(body, metadata.bodyType(), template);
        } catch (EncodeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EncodeException(e.getMessage(), e);
        }
        return super.resolve(argv, template, variables);
    }
}
