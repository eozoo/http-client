package org.springframework.feign.codec;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@Setter
public class HttpResponse<T> extends ResponseEntity<T> {

    /**
     * 非200响应时，body序列化T类型可能失败，所以这里加个字段存放描述信息
     */
    private String message;

    public HttpResponse(){
        super(HttpStatus.OK);
    }

    public HttpResponse(int httpStatus, @Nullable MultiValueMap<String, String> headers, @Nullable T body) {
        super(body, headers, httpStatus);
    }

    public HttpResponse(int httpStatus, @Nullable MultiValueMap<String, String> headers, @Nullable T body, String message) {
        super(body, headers, httpStatus);
        this.message = message;
    }

    public HttpResponse(HttpCode httpCode, @Nullable MultiValueMap<String, String> headers, @Nullable T body) {
        super(body, headers, httpCode.getStatus());
    }

    public HttpResponse(HttpCode httpCode, @Nullable MultiValueMap<String, String> headers, @Nullable T body, String message) {
        super(body, headers, httpCode.getStatus());
        this.message = message;
    }

    /**
     * 获取Http Header
     */
    public String getHeader(String headerName){
        HttpHeaders headers = this.getHeaders();
        List<String> list = headers.get(headerName);
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取Http Header
     */
    public List<String> getHeaders(String headerName){
        HttpHeaders headers = this.getHeaders();
        return headers.get(headerName);
    }

    /**
     * 设置Http Header
     */
    public HttpResponse<T> setHeader(String key, String... values){
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(this.getHeaders());
        headers.put(key, List.of(values));
        return new HttpResponse<>(this.getStatusCode().value(), headers, this.getBody());
    }

    /**
     * status == 200
     */
    public boolean isSuccess(){
        return getStatusCodeValue() == 200;
    }

    /**
     * status != 200
     */
    public boolean isFailed(){
        return getStatusCodeValue() != 200;
    }

    /**
     * status=#{HttpCode.status}, body=#{HttpCode.msg}
     */
    public static HttpResponse<Object> code(HttpCode httpCode) {
        return new HttpResponse<>(httpCode, null, null);
    }

    /**
     * status=#{HttpCode.status}, body=#{data}
     */
    public static <V> HttpResponse<V> body(HttpCode httpCode, V data) {
        return new HttpResponse<>(httpCode, null, data);
    }

    /**
     * status=200, body=null
     */
    public static <V> HttpResponse<V> success(){
        return new HttpResponse<>(ResponseCode.SUCCESS, null, null);
    }

    /**
     * status=200, body=#{data}
     */
    public static <V> HttpResponse<V> success(V data) {
        return new HttpResponse<>(ResponseCode.SUCCESS, null, data);
    }
}
