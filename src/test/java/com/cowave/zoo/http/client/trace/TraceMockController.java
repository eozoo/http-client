package com.cowave.zoo.http.client.trace;

import com.cowave.zoo.http.client.response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mock")
public class TraceMockController {

    private final TraceMockService traceMockService;

    @GetMapping("/trace1")
    public String trace1() {
        HttpResponse<String> httpResponse = traceMockService.trace();
        return httpResponse.getBody();
    }
}
