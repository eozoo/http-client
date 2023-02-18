package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import feign.jackson.JacksonDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;

/**
 *
 * @author shanhuiming
 *
 */
public class ResponseDecoder extends JacksonDecoder {

    private final ObjectMapper mapper;

    public ResponseDecoder() {
        this(Collections.<Module>emptyList());
    }

    public ResponseDecoder(Iterable<Module> modules) {
        this(new ObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModules(modules));
    }

    public ResponseDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() == null) {
            return null;
        }

        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader, 1);
        }

        try {
            reader.mark(1); // Read the first byte to see if we have any data
            if (reader.read() == -1) {
                return null; // Eagerly returning null avoids "No content to map due to end-of-input"
            }
            reader.reset();

            org.springframework.feign.codec.Response<?> resp =
                    mapper.readValue(reader, org.springframework.feign.codec.Response.class);
            if(ResponseCode.OK.getCode() != resp.getCode()){
                throw new RuntimeException(resp.getCode() + ", " + resp.getMsg());
            }

            String data = mapper.writeValueAsString(resp.getData());
            return mapper.readValue(data, mapper.constructType(type));
        } catch (RuntimeJsonMappingException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw IOException.class.cast(e.getCause());
            }
            throw e;
        }
    }
}
