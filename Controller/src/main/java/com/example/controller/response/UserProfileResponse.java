package com.example.controller.response;

import com.example.controller.DTO.Role;
import lombok.Data;

@Data
public class UserProfileResponse {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
}
