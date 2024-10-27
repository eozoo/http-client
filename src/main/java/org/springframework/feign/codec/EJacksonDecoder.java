package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;

import static org.slf4j.event.Level.WARN;

/**
 *
 * @author shanhuiming
 *
 */
public class EJacksonDecoder implements FeignDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(EJacksonDecoder.class);
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
    public Object decode(Response response, Type type, String url, long cost, int status, Level level) throws Exception {
        Reader reader = response.body().asReader();
        if (!reader.markSupported()) {
            reader = new BufferedReader(reader, 1);
        }

        reader.mark(1);
        if (reader.read() == -1) {
            return null;
        }
        reader.reset();

        if (void.class == type) {
            if(LOGGER.isDebugEnabled() || level.toInt() < WARN.toInt()){
                LOGGER.info(">< {} {}ms {}", status, cost, url);
            }
            return null;
        }

        Object obj = mapper.readValue(reader, mapper.constructType(type));
        if(obj != null){
            if(org.springframework.feign.codec.Response.class.isAssignableFrom(obj.getClass())){
                org.springframework.feign.codec.Response resp = (org.springframework.feign.codec.Response)obj;
                if(!Objects.equals(ResponseCode.SUCCESS.getCode(), resp.getCode())){
                    LOGGER.error(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
                }else if(LOGGER.isDebugEnabled() || level.toInt() < WARN.toInt()){
                    LOGGER.info(">< {} {}ms {} {code={}, msg={}}", status, cost, url, resp.getCode(), resp.getMsg());
                }
            }else if(LOGGER.isDebugEnabled() || level.toInt() < WARN.toInt()){
                LOGGER.info(">< {} {}ms {}", status, cost, url);
            }
        }else if(LOGGER.isDebugEnabled() || level.toInt() < WARN.toInt()){
            LOGGER.info(">< {} {}ms {}", status, cost, url);
        }
        return obj;
    }
}
