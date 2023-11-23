package org.springframework.feign.codec;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.StringDecoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 *
 * @author shanhuiming
 *
 */
public interface FeignDecoder {

    Object decode(Response response, Type type, String name, String url, long cost, int httpCode) throws IOException, DecodeException, FeignException;

    class StringDecoder implements FeignDecoder {

        @Override
        public Object decode(Response response, Type type, String name, String url, long cost, int httpCode) throws IOException {
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
