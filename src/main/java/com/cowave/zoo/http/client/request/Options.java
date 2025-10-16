package com.cowave.zoo.http.client.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@RequiredArgsConstructor
public class Options {
    private final int connectTimeout;
    private final int readTimeout;
    private final int retryTimes;
    private final int retryInterval;
}
