package org.springframework.feign;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Maps;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author jiyusheng
 * @date 2021-01-05 13:25:28
 */
public class RequestHeaderContext {

    /**
     * 需要存储的request header
     */
    public static final List<String> INCLUDE_HEADER_NAME = Arrays.asList("requestId", "Authorization");

    /**
     * 用TransmittableThreadLocal，否则Spring Cloud中的Hystrix线程隔离导致ThreadLocal数据丢失
     *
     * @see https://mp.weixin.qq.com/s?__biz=MzIwMDY0Nzk2Mw==&mid=2650319274&idx=1&sn=0a5bf8ef9148ba8562e0e1c58038da98&chksm=8ef5fddeb98274c8a88522c56050b5e5c7f6d510c5345cec795a130d3abd2b53a16513f578ce&scene=21#wechat_redirect
     */
    private static ThreadLocal<Map<String, String>> transmittableThreadLocal = new TransmittableThreadLocal<>();

    public static Map<String, String> get() {
        return transmittableThreadLocal.get();
    }

    public static void set(HttpServletRequest request) {
        Map<String, String> map = Maps.newHashMap();
        INCLUDE_HEADER_NAME.forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                map.put(headerName, headerValue);
            }
        });
        transmittableThreadLocal.set(map);
    }

    public static void set(Map<String, String> headers) {
        transmittableThreadLocal.set(headers);
    }

    public static void remove() {
        transmittableThreadLocal.remove();
    }
}
