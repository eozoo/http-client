package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import org.springframework.feign.invoke.RemoteAssertsException;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;

/**
 *
 * @author shanhuiming
 *
 */
public class ResponseDecoder implements FeignDecoder {

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
    public Object decode(Response response, Type type, String url, long cost, int status, org.slf4j.Logger logger) throws Exception {
        if (response.body() == null) {
            logger.info(">< {} {}ms {}", status, cost, url);
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
            logger.info(">< {} {}ms {}", status, cost, url);
            return null;
        }
        reader.reset();

        org.springframework.feign.codec.Response<?> resp =
                mapper.readValue(reader, org.springframework.feign.codec.Response.class);
        if(HttpStatus.OK.value() != resp.getCode()){
            logger.error(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
            throw new RemoteAssertsException(url, status, resp.getCode(), resp.getMsg());
        }

        if (void.class == type) {
            logger.info(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
            return null;
        }

        logger.info(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
        String data = mapper.writeValueAsString(resp.getData());
        return mapper.readValue(data, mapper.constructType(type));
    }
}
