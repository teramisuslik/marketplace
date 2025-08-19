package com.example.serviceforcast.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user", url="http://localhost:8082")
public interface UserClient {

    @PostMapping("/api/user/userid")
    Long getUserId(@RequestHeader("Authorization") String token);
}
