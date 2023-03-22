package org.springframework.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * @author shanhuiming
 */
public class FeignInterceptor implements RequestInterceptor {

    public static final ThreadLocal<Map<String, String>> headers = new ThreadLocal<>();

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String requestId = null;
        String authorization = null;

        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestId = request.getHeader("requestId");
            authorization = request.getHeader("Authorization");
        }

        if(authorization == null){
            Map<String, String> header = headers.get();
            if (header != null) {
                requestId = header.get("requestId");
                authorization = header.get("Authorization");
            }
        }

        if(authorization == null){
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
            }
        }

        if(requestId != null){
            requestTemplate.header("requestId", requestId);
        }
        if(authorization != null){
            requestTemplate.header("Authorization", authorization);
        }
    }
}
