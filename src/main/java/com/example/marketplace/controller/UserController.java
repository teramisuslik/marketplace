package com.example.marketplace.controller;

import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtTockenUtils jwtTockenUtils;

    @PostMapping("/register")
    public void createUser(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
    }

    @PostMapping("/register/seller")
    public void createSeller(@RequestBody UserDTO userDTO) {
        userService.registerSeller(userDTO);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserDTO userDTO) {
        User user = userService.loginUser(userDTO);
        return jwtTockenUtils.generateTocken(user);
    }

    @PostMapping("/userid")
    public Long userId(@RequestHeader("Authorization") String token) {

        return userService.getUserid(token);
    }

    @GetMapping("/get_role")
    public Role getRole(@RequestHeader("Authorization") String token) {
        return userService.getRole(token);
    }

    @GetMapping("/load_user_by_username")
    public UserDTO loadUserByUsername(@RequestParam("username") String username) {
        return userService.findByUsername(username);
    }
}
