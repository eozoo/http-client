package org.springframework.feign.codec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
    public Object decode(Response response, Type type, String url, long cost, int status, org.slf4j.Logger logger) throws Exception {
        String requestId = "";
        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            if(entry.getKey().equals("requestId")){
                Collection<String> collection = entry.getValue();
                if(!CollectionUtils.isEmpty(collection)){
                    requestId = collection.stream().findFirst().get();
                }
            }
        }

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
            logger.info(">< {} {}ms {} {}", status, cost, requestId, url);
            return null;
        }

        Object obj = mapper.readValue(reader, mapper.constructType(type));
        if(obj != null){
            if(org.springframework.feign.codec.Response.class.isAssignableFrom(obj.getClass())){
                org.springframework.feign.codec.Response resp = (org.springframework.feign.codec.Response)obj;
                if(HttpStatus.OK.value() != resp.getCode()){
                    logger.error(">< {} {}ms {} {code={}, msg={}} {}", status, cost, requestId, resp.getCode(), resp.getMsg(), url);
                }else{
                    logger.info(">< {} {}ms {} {code={}, msg={}} {}", status, cost, requestId, resp.getCode(), resp.getMsg(), url);
                }
            }else{
                logger.info(">< {} {}ms {} {}", status, cost, requestId, url);
            }
        }else{
            logger.info(">< {} {}ms {} {}", status, cost, requestId, url);
        }
        return obj;
    }
}
