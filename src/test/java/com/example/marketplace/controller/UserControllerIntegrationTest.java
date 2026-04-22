package com.example.marketplace.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTockenUtils jwtTockenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerAndLogin_ShouldWorkEndToEnd() throws Exception {
        // Given
        UserDTO registerDto = new UserDTO("Anthony", "secret", Role.USER);
        String json = objectMapper.writeValueAsString(registerDto);

        // When
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Then
        User savedUser = userRepository.findByUsername("Anthony").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getPassword()).isNotEqualTo("secret");

        // When
        UserDTO loginDto = new UserDTO("Anthony", "secret", null);
        String loginJson = objectMapper.writeValueAsString(loginDto);

        String token = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        String usernameFromToken = jwtTockenUtils.getUsernameFromToken(token);
        assertThat(usernameFromToken).isEqualTo("Anthony");

        // When
        String userId = mockMvc.perform(post("/api/user/userid").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        assertThat(userId).isEqualTo(savedUser.getId().toString());
    }

    @Test
    void getRole_ShouldReturnCorrectRole() throws Exception {
        // given
        User user = User.builder()
                .username("seller")
                .password("encoded")
                .role(Role.SELLER)
                .build();
        userRepository.save(user);
        String token = jwtTockenUtils.generateTocken(user);

        // when / then
        mockMvc.perform(get("/api/user/get_role").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("SELLER"));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDTO() throws Exception {
        // given
        User user = User.builder()
                .username("Vladislav")
                .password("encoded")
                .role(Role.ADMIN)
                .build();
        userRepository.save(user);

        // when / then
        mockMvc.perform(get("/api/user/load_user_by_username").param("username", "Vladislav"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Vladislav"))
                .andExpect(jsonPath("$.password").value("encoded"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
