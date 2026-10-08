package com.cowave.zoo.http.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author shanhuiming
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "http-client")
public class HttpClientProperties {

    /**
     * 共享连接池最多保留的空闲连接数，不限制请求并发数
     */
    private int poolConnections = 10;

    /**
     * 空闲连接保留时间
     */
    private Duration keepAliveDuration = Duration.ofMinutes(5);
}
