package com.cowave.zoo.http.client.invoke.proxy;

import com.cowave.zoo.http.client.asserts.HttpHintException;
import com.cowave.zoo.http.client.response.HttpResponse;
import lombok.Getter;

import static com.cowave.zoo.http.client.constants.HttpCode.SERVICE_ERROR;

/**
 * @author shanhuiming
 */
@Getter
class HttpResponseException extends HttpHintException {

    private final HttpResponse<?> response;

    public HttpResponseException(HttpResponse<?> response) {
        super(response.getStatus(), SERVICE_ERROR.getCode(), "Remote failed");
        this.response = response;
    }

    public HttpResponseException(Throwable cause, HttpResponse<?> response) {
        super(cause, "Remote failed");
        this.response = response;
    }
}
