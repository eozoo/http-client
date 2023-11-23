package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import org.springframework.feign.invoke.RemoteException;

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
    public Object decode(Response response, Type type, String name, String url, long cost, int httpCode, org.slf4j.Logger logger) throws IOException {
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
                // Eagerly returning null avoids "No content to map due to end-of-input"
                RemoteChain.appendChain(false, name, url, cost, httpCode, "?");
                return null;
            }
            reader.reset();

            org.springframework.feign.codec.Response<?> resp =
                    mapper.readValue(reader, org.springframework.feign.codec.Response.class);
            if(ResponseCode.OK.getCode() != resp.getCode()){
                RemoteChain.appendChain(true, name, url, cost, httpCode, String.valueOf(resp.getCode()));
                logger.info(">< remote   {}|{} {}ms {}", httpCode, resp.getCode(), cost, url);
                throw new RemoteException(resp.getCode() + ", " + resp.getMsg());
            }

            if (void.class == type) {
                RemoteChain.appendChain(true, name, url, cost, httpCode, String.valueOf(resp.getCode()));
                logger.info(">< remote   {}|{} {}ms {}", httpCode, resp.getCode(), cost, url);
                return null;
            }

            logger.info(">< remote   {}|{} {}ms {}", httpCode, resp.getCode(), cost, url);
            RemoteChain.appendChain(true, name, url, cost, httpCode, String.valueOf(resp.getCode()));
            String data = mapper.writeValueAsString(resp.getData());
            return mapper.readValue(data, mapper.constructType(type));
        } catch (RuntimeJsonMappingException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            RemoteChain.appendChain(false, name, url, cost, httpCode, "E4");
            throw e;
        }
    }
}
