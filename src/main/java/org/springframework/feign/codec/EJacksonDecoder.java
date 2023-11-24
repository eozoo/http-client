package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import feign.Response;

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
public class EJacksonDecoder implements FeignDecoder {
    private final ObjectMapper mapper;

    public EJacksonDecoder() {
        this(Collections.emptyList());
    }

    public EJacksonDecoder(Iterable<Module> modules) {
        this(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModules(modules));
    }

    public EJacksonDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object decode(Response response, Type type, String name, String url, long cost, int httpCode, org.slf4j.Logger logger) throws IOException {
        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader, 1);
        }
        try {
            reader.mark(1);
            if (reader.read() == -1) {
                // Eagerly returning null avoids "No content to map due to end-of-input"
                RemoteChain.appendChain(true, name, url, cost, httpCode, "?", null);
                return null;
            }
            reader.reset();

            if (void.class == type) {
                RemoteChain.appendChain(true, name, url, cost, httpCode, "?", null);
                logger.info(">< remote   {}|? {}ms {}", httpCode, cost, url);
                return null;
            }

            Object obj = mapper.readValue(reader, mapper.constructType(type));
            if(org.springframework.feign.codec.Response.class.isAssignableFrom(obj.getClass())){
                org.springframework.feign.codec.Response resp = (org.springframework.feign.codec.Response)obj;
                boolean success = ResponseCode.OK.getCode() != resp.getCode();
                RemoteChain.appendChain(success, name, url, cost, httpCode, String.valueOf(resp.getCode()), resp.getChains());
                logger.info(">< remote   {}|{} {}ms {}", httpCode, resp.getCode(), cost, url);
;            }else{
                RemoteChain.appendChain(true, name, url, cost, httpCode, "?", null);
                logger.info(">< remote   {}|? {}ms {}", httpCode, cost, url);
            }
            return obj;
        } catch (RuntimeJsonMappingException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw IOException.class.cast(e.getCause());
            }
            RemoteChain.appendChain(false, name, url, cost, httpCode, "E4", null);
            throw e;
        }
    }
}
