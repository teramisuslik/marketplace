package com.example.controller.response;

import com.example.controller.DTO.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private String token;
    private Role role;
}
