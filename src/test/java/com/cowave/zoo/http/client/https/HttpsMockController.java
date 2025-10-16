package com.cowave.zoo.http.client.https;

import com.cowave.zoo.http.client.response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mock")
public class HttpsMockController {

    private final HttpsMockService httpsMockService;

    @GetMapping("/https1")
    public HttpResponse<Object> https1() {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("name", "xxx");
        return httpsMockService.create(bodyMap);
    }
}
