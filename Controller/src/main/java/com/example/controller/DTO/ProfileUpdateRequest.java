package com.example.controller.DTO;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String email;
    private String phone;
}
