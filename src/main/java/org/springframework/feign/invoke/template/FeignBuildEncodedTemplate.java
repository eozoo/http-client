package org.springframework.feign.invoke.template;

import feign.MethodMetadata;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.util.Map;

import static feign.Util.checkArgument;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBuildEncodedTemplate extends FeignTemplateFactory{

    private final Encoder encoder;

    public FeignBuildEncodedTemplate(MethodMetadata metadata, Encoder encoder) {
        super(metadata);
        this.encoder = encoder;
    }

    @Override
    protected RequestTemplate resolve(Object[] argv, RequestTemplate mutable,
                                      Map<String, Object> variables) {
        Object body = argv[metadata.bodyIndex()];
        checkArgument(body != null, "Body parameter %s was null", metadata.bodyIndex());
        try {
            encoder.encode(body, metadata.bodyType(), mutable);
        } catch (EncodeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EncodeException(e.getMessage(), e);
        }
        return super.resolve(argv, mutable, variables);
    }
}
