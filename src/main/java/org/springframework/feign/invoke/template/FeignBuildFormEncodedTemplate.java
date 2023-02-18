package org.springframework.feign.invoke.template;

import feign.MethodMetadata;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignBuildFormEncodedTemplate extends FeignTemplateFactory{

    private final Encoder encoder;

    public FeignBuildFormEncodedTemplate(MethodMetadata metadata, Encoder encoder) {
        super(metadata);
        this.encoder = encoder;
    }

    @Override
    protected RequestTemplate resolve(Object[] argv, RequestTemplate mutable,
                                      Map<String, Object> variables) {
        Map<String, Object> formVariables = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (metadata.formParams().contains(entry.getKey())) {
                formVariables.put(entry.getKey(), entry.getValue());
            }
        }
        try {
            encoder.encode(formVariables, Encoder.MAP_STRING_WILDCARD, mutable);
        } catch (EncodeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EncodeException(e.getMessage(), e);
        }
        return super.resolve(argv, mutable, variables);
    }
}
