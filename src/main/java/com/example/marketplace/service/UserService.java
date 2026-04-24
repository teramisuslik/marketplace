package com.example.marketplace.service;

import com.example.marketplace.DTO.UpdateProfileRequest;
import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.DTO.UserProfileDTO;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTockenUtils jwtTockenUtils;

    public void registerUser(UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(Role.USER)
                .fullName(trimFullName(userDTO.getFullName()))
                .build();

        userRepository.save(user);
    }

    public User registerSeller(UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(Role.SELLER)
                .fullName(trimFullName(userDTO.getFullName()))
                .build();

        return userRepository.save(user);
    }

    public User loginUser(UserDTO userDTO) {
        return userRepository
                .findByUsername(userDTO.getUsername())
                .filter(e -> passwordEncoder.matches(userDTO.getPassword(), e.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
    }

    public Long getUserid(String token) {
        token = token.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        return user.getId();
    }

    public Role getRole(String token) {
        token = token.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        return user.getRole();
    }

    public UserDTO findByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setRole(user.getRole());
        userDTO.setFullName(user.getFullName());
        return userDTO;
    }

    public UserProfileDTO getProfile(String authorization) {
        String token = authorization.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        return new UserProfileDTO(
                user.getUsername(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    public void updateProfile(String authorization, UpdateProfileRequest body) {
        String token = authorization.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        user.setFullName(trimFullName(body.getFullName()));
        user.setEmail(trimNullable(body.getEmail()));
        user.setPhone(trimNullable(body.getPhone()));
        userRepository.save(user);
    }

    private static String trimFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        String t = fullName.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimNullable(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
