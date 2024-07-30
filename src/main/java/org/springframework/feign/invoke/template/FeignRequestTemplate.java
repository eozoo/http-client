package org.springframework.feign.invoke.template;

import feign.RequestTemplate;
import lombok.Data;

/**
 * 这里包一下RequestTemplate，约定一个根路径参数，不希望放在url中编码
 *
 * @author shanhuiming
 */
@Data
public class FeignRequestTemplate {

    private String hostUrl;

    private RequestTemplate template;

}
