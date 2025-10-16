package com.cowave.zoo.http.client.error;

import com.cowave.zoo.http.client.asserts.Asserts;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ErrorController {

    @GetMapping("/error/{id}")
    public Integer getById(@PathVariable Integer id, @RequestHeader(X_Request_ID) String xId){
        Asserts.equals(xId, "12345", "");
        return id;
    }
}
