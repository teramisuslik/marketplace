package com.example.controller.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String password;
    private Role role;

    /** Имя; передаётся в сервис пользователей при регистрации */
    private String fullName;
}
