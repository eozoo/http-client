package org.springframework.feign.codec;

import feign.Response;
import feign.Util;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.feign.invoke.FeignSyncInvoker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import static org.slf4j.event.Level.WARN;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignDecoder {

    Object decode(Response response, Type type, String url, long cost, int httpCode, Level level) throws Exception;

    class StringDecoder implements FeignDecoder {

        private static final Logger LOGGER = LoggerFactory.getLogger(StringDecoder.class);

        @Override
        public Object decode(Response response, Type type, String url, long cost, int status, Level level) throws IOException {
            if(LOGGER.isDebugEnabled() || level.toInt() < WARN.toInt()){
                LOGGER.info(">< {} {}ms {}", status, cost, url);
            }

            if (byte[].class.equals(type)) {
                return Util.toByteArray(response.body().asInputStream());
            }

            Response.Body body = response.body();
            if (String.class.equals(type)) {
                return Util.toString(body.asReader());
            }

            throw new UnsupportedEncodingException();
        }
    }
}
