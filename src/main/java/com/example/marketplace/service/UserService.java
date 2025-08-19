package com.example.marketplace.service;

import com.example.marketplace.DTO.UserDTO;
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
    //private final ProductService productService;
    private final JwtTockenUtils jwtTockenUtils;

    public void registerUser(UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public User registerSeller(UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(Role.SELLER)
                .build();

        return userRepository.save(user);
    }

    public User loginUser(UserDTO userDTO) {
        return userRepository.findByUsername(userDTO.getUsername())
                .filter(e -> passwordEncoder.matches(userDTO.getPassword(), e.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

    }

    public Long getUserid(String token) {
        token = token.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        return user.getId();
    }

    public Role getRole(String token) {
        token = token.substring(7);
        String username = jwtTockenUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        return user.getRole();
    }

    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid username"));

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

}
