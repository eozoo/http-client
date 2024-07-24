package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import org.springframework.feign.invoke.RemoteAssertsException;

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
    public Object decode(Response response, Type type, String name, String url, long cost, int httpCode, org.slf4j.Logger logger) throws Exception {
        if (response.body() == null) {
            return null;
        }

        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader, 1);
        }

        reader.mark(1); // Read the first byte to see if we have any data
        if (reader.read() == -1) {
            // Eagerly returning null avoids "No content to map due to end-of-input"
            return null;
        }
        reader.reset();

        org.springframework.feign.codec.Response<?> resp =
                mapper.readValue(reader, org.springframework.feign.codec.Response.class);
        if(200 != resp.getCode()){
            RemoteChain.appendChain(false, name, url, cost, httpCode, String.valueOf(resp.getCode()), resp.getChains());
            logger.error(">< {} {}ms {} {code={}, msg={}}", httpCode, cost, url, resp.getCode(), resp.getMsg());
            throw new RemoteAssertsException(url, httpCode, resp.getCode(), resp.getMsg());
        }

        if (void.class == type) {
            RemoteChain.appendChain(true, name, url, cost, httpCode, String.valueOf(resp.getCode()), resp.getChains());
            logger.info(">< {} {}ms {} {code={}, msg={}}", httpCode, cost, url, resp.getCode(), resp.getMsg());
            return null;
        }

        logger.info(">< {} {}ms {} {code={}, msg={}}", httpCode, cost, url, resp.getCode(), resp.getMsg());
        RemoteChain.appendChain(true, name, url, cost, httpCode, String.valueOf(resp.getCode()), resp.getChains());
        String data = mapper.writeValueAsString(resp.getData());
        return mapper.readValue(data, mapper.constructType(type));
    }
}
