package com.example.controller.client;

import com.example.controller.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user", url="http://localhost:8082")
public interface UserClient {

    @PostMapping("/api/user/register")
    void createUser(@RequestBody UserDTO userDTO);

    @PostMapping("/api/user/register/seller")
    void createSeller(@RequestBody UserDTO userDTO);

    @PostMapping("/api/user/login")
    String login(@RequestBody UserDTO userDTO);

    @GetMapping("/api/user/load_user_by_username")
    UserDTO findUserByUsername(@RequestParam("username") String username);
}
