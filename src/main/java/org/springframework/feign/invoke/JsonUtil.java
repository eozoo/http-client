package org.springframework.feign.invoke;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.TimeZone;

/**
 *
 * @author shanhuiming
 *
 */
class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static{
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .registerModules(Collections.emptyList());
        MAPPER.setTimeZone(TimeZone.getDefault());
    }

    public static Object read(String json, Type type) throws IOException {
        return MAPPER.readValue(json, MAPPER.constructType(type));
    }
}
