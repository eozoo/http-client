package com.cowave.commons.client.http.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
public class HttpResponseTemplate {
    private final int status;
    private final Map<String, List<String>> remoteHeaders;
    private final String reason;
    private final InputStream inputStream;
    private final Integer length;
}
