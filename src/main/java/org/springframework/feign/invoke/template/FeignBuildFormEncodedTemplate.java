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
    protected FeignRequestTemplate resolve(Object[] argv, RequestTemplate template, Map<String, Object> variables) {
        // 填充表单参数
        Map<String, Object> formVariables = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (metadata.formParams().contains(entry.getKey())) {
                formVariables.put(entry.getKey(), entry.getValue());
            }
        }
        // 编码template.body
        try {
            encoder.encode(formVariables, Encoder.MAP_STRING_WILDCARD, template);
        } catch (EncodeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new EncodeException(e.getMessage(), e);
        }
        return super.resolve(argv, template, variables);
    }
}
