package com.cowave.zoo.http.client.invoke.codec;

import com.cowave.zoo.http.client.response.HttpResponseTemplate;
import org.slf4j.event.Level;

import java.lang.reflect.Type;

/**
 *
 * @author shanhuiming
 *
 */
public interface HttpDecoder {

    Object decode(HttpResponseTemplate response, Type type, String url, long cost, int httpCode, Level level) throws Exception;
}
