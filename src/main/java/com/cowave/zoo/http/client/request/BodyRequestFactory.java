package com.cowave.zoo.http.client.request;

import com.cowave.zoo.http.client.request.meta.HttpMethodMeta;
import com.cowave.zoo.http.client.invoke.codec.HttpEncoder;

import java.util.Collection;
import java.util.Map;

import static com.cowave.zoo.http.client.constants.HttpHeader.Content_Type;

/**
 *
 * @author shanhuiming
 *
 */
public class BodyRequestFactory extends HttpRequestFactory {

    private final HttpEncoder encoder;

    public BodyRequestFactory(HttpMethodMeta metadata, HttpEncoder encoder) {
        super(metadata);
        this.encoder = encoder;
    }

    @Override
    protected HttpRequest resolve(Object[] argv, HttpRequest httpRequest,
                                  Map<String, Object> variables, Map<String, Object> multiParams) throws Exception {
        Object body = argv[metadata.getBodyIndex()];

        encoder.encode(httpRequest, body);

        // body请求，默认设置下application/json
        Map<String, Collection<String>> headers = httpRequest.headers();
        if(!headers.containsKey(Content_Type)){
            httpRequest.header(Content_Type, "application/json");
        }
        return super.resolve(argv, httpRequest, variables, multiParams);
    }
}
