package org.springframework.feign.invoke.template;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.springframework.feign.invoke.method.FeignMethodMetadata;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static feign.Util.checkArgument;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBodyRequestFactory extends FeignRequestFactory {

    private final Encoder encoder;

    public FeignBodyRequestFactory(FeignMethodMetadata metadata, Encoder encoder) {
        super(metadata);
        this.encoder = encoder;
    }

    @Override
    protected FeignRequestTemplate resolve(Object[] argv, RequestTemplate template, Map<String, Object> variables) throws IOException {
        Object body = argv[metadata.bodyIndex()];
        checkArgument(body != null, "Body parameter %s was null", metadata.bodyIndex());
        try {
            encoder.encode(body, metadata.bodyType(), template);
        } catch (EncodeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EncodeException(e.getMessage(), e);
        }

        Map<String, Collection<String>> headers = template.headers();
        if(!headers.containsKey("Content-Type")){
            template.header("Content-Type", "application/json");
        }
        return super.resolve(argv, template, variables);
    }
}
