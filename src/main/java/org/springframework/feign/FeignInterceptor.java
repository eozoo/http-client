package org.springframework.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignInterceptor implements RequestInterceptor {

    public static final ThreadLocal<Map<String, String>> headers = new ThreadLocal<>();

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> header = headers.get();
        String requestId = null;
        String authorization = null;

        if (header != null && !header.isEmpty()) {
            requestId = header.get("requestId");
            authorization = header.get("Authorization");
            // 如果有其他携带的header在这获取，并调用requestTemplate.header()
        }

        // ttl存储的requestHeader
        Map<String, String> requestHeaders = RequestHeaderContext.get();
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            // 添加需要携带的消息头信息
            Set<Map.Entry<String, String>> entrySet = requestHeaders.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();
                if (headerName.equals("requestId")) {
                    if (requestId == null) {
                        requestId = headerValue;
                    }
                    continue;
                }
                if (headerName.equals("Authorization")) {
                    if (authorization == null) {
                        authorization = headerValue;
                    }
                    continue;
                }
                requestTemplate.header(headerName, headerValue);
            }
        } else {
            // 理论上，不会走这个方法
            ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (requestId == null) {
                    requestId = request.getHeader("requestId");
                }
                if (authorization == null) {
                    authorization = request.getHeader("Authorization");
                }
            }
        }

        // 添加requestId和Authorization
        requestTemplate.header("requestId", requestId);
        requestTemplate.header("Authorization", authorization);
    }
}
