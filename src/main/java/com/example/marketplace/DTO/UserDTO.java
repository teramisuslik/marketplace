package com.example.marketplace.DTO;

import com.example.marketplace.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String password;
    private Role role;

    /** Имя пользователя; при регистрации сохраняется в БД */
    private String fullName;
}
