package com.example.marketplace.DTO;

import com.example.marketplace.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
}
