package org.springframework.feign.codec;

import java.lang.reflect.Type;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.jackson.JacksonEncoder;

/**
 *
 * @author shanhuiming
 *
 */
public class EJacksonEncoder extends JacksonEncoder {

    private final ObjectMapper mapper;

    public EJacksonEncoder() {
        this(Collections.emptyList());
    }

    public EJacksonEncoder(Iterable<Module> modules) {
        this(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(SerializationFeature.INDENT_OUTPUT, true)
            .registerModules(modules));
    }

    public EJacksonEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) {
        try {
            //JavaType javaType = mapper.getTypeFactory().constructType(bodyType);
            //template.body(mapper.writerFor(javaType).writeValueAsString(object));
            template.body(mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new EncodeException(e.getMessage(), e);
        }
    }
}
