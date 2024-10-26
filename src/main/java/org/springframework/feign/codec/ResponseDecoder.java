package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.feign.invoke.RemoteAssertsException;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author shanhuiming
 *
 */
public class ResponseDecoder implements FeignDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseDecoder.class);
    private final ObjectMapper mapper;

    public ResponseDecoder() {
        this(Collections.emptyList());
    }

    public ResponseDecoder(Iterable<Module> modules) {
        this(new ObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModules(modules));
    }

    public ResponseDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object decode(Response response, Type type, String url, long cost, int status, boolean logInfo) throws Exception {
        if (response.body() == null) {
            if(logInfo){
                LOGGER.info(">< {} {}ms {}", status, cost, url);
            }
            return null;
        }

        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader, 1);
        }

        // Read the first byte to see if we have any data
        reader.mark(1);
        // Eagerly returning null avoids "No content to map due to end-of-input"
        if (reader.read() == -1) {
            if(logInfo){
                LOGGER.info(">< {} {}ms {}", status, cost, url);
            }
            return null;
        }
        reader.reset();

        org.springframework.feign.codec.Response<?> resp =
                mapper.readValue(reader, org.springframework.feign.codec.Response.class);
        if(!Objects.equals(ResponseCode.SUCCESS.getCode(), resp.getCode())){
            LOGGER.error(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
            throw new RemoteAssertsException(url, status, resp.getCode(), resp.getMsg());
        }

        if (void.class == type) {
            if(logInfo){
                LOGGER.info(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
            }
            return null;
        }

        if(logInfo){
            LOGGER.info(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
        }
        String data = mapper.writeValueAsString(resp.getData());
        return mapper.readValue(data, mapper.constructType(type));
    }
}
