package com.cowave.zoo.http.client.invoke.codec.decoder;

import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;
import com.cowave.zoo.http.client.response.Response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import com.cowave.zoo.http.client.request.HttpRequestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.TimeZone;

import static com.cowave.zoo.http.client.constants.HttpCode.SUCCESS;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class JacksonDecoder implements HttpDecoder {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.findAndRegisterModules();
        MAPPER.setTimeZone(TimeZone.getDefault());
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final ObjectMapper mapper;

    public JacksonDecoder() {
        this(MAPPER);
    }

    public JacksonDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object decode(HttpResponseTemplate response, Type type, String url, long cost, int status) throws Exception {
        String logUrl = HttpRequestTemplate.logUrl(url);
        if (void.class == type || Void.class == type || response.getInputStream() == null) {
            if (log.isInfoEnabled()) {
                log.info(">< {} {}ms {}", status, cost, logUrl);
            }
            return null;
        }
        Object obj;
        // Jackson识别JSON编码和BOM；空响应及纯空白响应不进行对象映射
        try (JsonParser parser = mapper.getFactory().createParser(response.getInputStream())) {
            // 网络响应由调用层统一关闭，解析器只释放自身资源
            parser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            if (parser.nextToken() == null) {
                if (log.isInfoEnabled()) {
                    log.info(">< {} {}ms {}", status, cost, logUrl);
                }
                return null;
            }
            obj = mapper.readValue(parser, mapper.constructType(type));
        }
        if(obj != null){
            if(Response.class.isAssignableFrom(obj.getClass())){
                Response<?> resp = (Response<?>)obj;
                if(!Objects.equals(SUCCESS.getCode(), resp.getCode())){
                    log.error(">< {} {}ms {} {code={}, msg={}}", status, cost, logUrl, resp.getCode(), resp.getMsg());
                }else if(log.isInfoEnabled()){
                    log.info(">< {} {}ms {} {code={}, msg={}}", status, cost, logUrl, resp.getCode(), resp.getMsg());
                }
            }else if(log.isInfoEnabled()){
                log.info(">< {} {}ms {}", status, cost, logUrl);
            }
        }else if(log.isInfoEnabled()){
            log.info(">< {} {}ms {}", status, cost, logUrl);
        }
        return obj;
    }
}
