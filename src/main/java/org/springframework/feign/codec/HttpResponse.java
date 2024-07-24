package org.springframework.feign.codec;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@Setter
public class HttpResponse<T> extends ResponseEntity<T> {

    private String requestId;

    public HttpResponse(){
        super(HttpStatus.OK);
    }

    public HttpResponse(int httpStatus, @Nullable MultiValueMap<String, String> headers, @Nullable T body) {
        super(body, headers, httpStatus);
    }

    public HttpResponse(HttpCode httpCode, @Nullable MultiValueMap<String, String> headers, @Nullable T body) {
        super(body, headers, httpCode.getStatus());
    }

    /**
     * 设置Http Header
     */
    public HttpResponse<T> setHeader(String key, String... values){
        HttpHeaders headers = new HttpHeaders(this.getHeaders());
        headers.put(key, Arrays.stream(values).toList());
        return new HttpResponse<>(this.getStatusCode().value(), headers, this.getBody());
    }

    /**
     * status=#{HttpCode.status}, body=#{HttpCode.msg}
     */
    public static HttpResponse<String> code(HttpCode httpCode) {
        return new HttpResponse<>(httpCode, null, httpCode.getMsg());
    }

    /**
     * status=#{HttpCode.status}, body=#{data}
     */
    public static <V> HttpResponse<V> message(HttpCode httpCode, V data) {
        return new HttpResponse<>(httpCode, null, data);
    }

    /**
     * status=200, body=null
     */
    public static <V> HttpResponse<V> success(){
        return new HttpResponse<>(ResponseCode.OK, null, null);
    }

    /**
     * status=200, body=#{data}
     */
    public static <V> HttpResponse<V> success(V data) {
        return new HttpResponse<>(ResponseCode.OK, null, data);
    }
}
