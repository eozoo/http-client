package com.cowave.zoo.http.client.delete;

import com.cowave.zoo.http.client.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class DeleteController {

    @DeleteMapping("/del/{ids}")
    public Response<Response.Page<Integer>> batchDelete(@PathVariable List<Integer> ids){
        return Response.page(ids);
    }

    @DeleteMapping("/del")
    public Response<Response.Page<Integer>> batchDelete2(@RequestParam("ids") List<Integer> ids){
        return Response.page(ids);
    }
}
