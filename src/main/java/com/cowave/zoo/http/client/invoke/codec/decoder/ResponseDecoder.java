package com.cowave.zoo.http.client.invoke.codec.decoder;

import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import com.cowave.zoo.http.client.response.Response;
import com.cowave.zoo.http.client.asserts.HttpHintException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.slf4j.Slf4j;
import com.cowave.zoo.http.client.request.HttpRequestTemplate;
import com.cowave.zoo.http.client.invoke.codec.HttpDecoder;

import java.lang.reflect.Type;
import java.util.Objects;

import static com.cowave.zoo.http.client.constants.HttpCode.SUCCESS;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class ResponseDecoder implements HttpDecoder {

    private final ObjectMapper mapper;

    public ResponseDecoder() {
        this.mapper = JacksonDecoder.MAPPER;
    }

    public ResponseDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object decode(HttpResponseTemplate response, Type type, String url, long cost, int status) throws Exception {
        String logUrl = HttpRequestTemplate.logUrl(url);
        if (response.getInputStream() == null) {
            if(log.isInfoEnabled()){
                log.info(">< {} {}ms {}", status, cost, logUrl);
            }
            return null;
        }

        Response<?> resp;
        // 与JacksonDecoder保持一致，空响应及纯空白响应不进行对象映射
        try (JsonParser parser = mapper.getFactory().createParser(response.getInputStream())) {
            parser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            resp = parser.nextToken() == null ? null : mapper.readValue(parser, Response.class);
        }
        if (resp == null) {
            if(log.isInfoEnabled()){
                log.info(">< {} {}ms {}", status, cost, logUrl);
            }
            return null;
        }
        if(!Objects.equals(SUCCESS.getCode(), resp.getCode())){
            log.error(">< {} {}ms {} {code={}, msg={}}", status, cost, logUrl, resp.getCode(), resp.getMsg());
            throw new HttpHintException(status, resp.getCode(), resp.getMsg());
        }

        if (void.class == type || Void.class == type) {
            if(log.isInfoEnabled()){
                log.info(">< {} {}ms {} {code={}, msg={}}", status, cost, logUrl, resp.getCode(), resp.getMsg());
            }
            return null;
        }

        if(log.isInfoEnabled()){
            log.info(">< {} {}ms {} {code={}, msg={}}", status, cost, logUrl, resp.getCode(), resp.getMsg());
        }
        String data = mapper.writeValueAsString(resp.getData());
        return mapper.readValue(data, mapper.constructType(type));
    }
}
