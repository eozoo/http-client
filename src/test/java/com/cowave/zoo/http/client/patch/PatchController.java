package com.cowave.zoo.http.client.patch;

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
public class PatchController {

    @PatchMapping("/modify/{id}")
    public PatchBody modifyById(@RequestHeader(X_Request_ID) String requestId, @PathVariable Integer id, @RequestBody PatchBody patchBody){
        Asserts.equals(requestId, "12345", "");
        Asserts.equals(id, 5, "");
        return patchBody;
    }
}
