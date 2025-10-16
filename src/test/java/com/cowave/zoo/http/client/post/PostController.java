package com.cowave.zoo.http.client.post;

import com.cowave.zoo.http.client.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class PostController {

    @PostMapping("/create")
    public Response<PostBody> create(@RequestBody PostBody postBody){
        return Response.success(postBody);
    }
}
