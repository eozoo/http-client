package com.cowave.zoo.http.client.post;

import com.cowave.zoo.http.client.response.HttpResponse;
import com.cowave.zoo.http.client.response.Response;
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
public class PostMockController {

    private final PostMockService postMockService;

    private final PostMockDecodeService postMockDecodeService;

    @GetMapping("/post1")
    public HttpResponse<Object> urlPost1() {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("name", "xxx");
        return postMockService.create(bodyMap);
    }

    @GetMapping("/post2")
    public PostBody urlPost2() {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("name", "xxx");
        return postMockDecodeService.create(bodyMap);
    }

    @GetMapping("/post3")
    public Response<PostBody> urlPost3() {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("name", "xxx");
        return postMockService.create2(bodyMap);
    }
}
