package com.example.serviceforproduct.client;

import com.example.serviceforproduct.entity.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user", url="http://localhost:8082")
public interface UserClient {

    @PostMapping("/api/user/userid")
    Long getUserId(@RequestHeader("Authorization") String token);

    @GetMapping("/api/user/get_role")
    Role getRole(@RequestHeader("Authorization") String token);
}
