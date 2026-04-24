package com.example.marketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.marketplace.DTO.UpdateProfileRequest;
import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.DTO.UserProfileDTO;
import com.example.marketplace.config.SecurityConfig;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.service.ShopOrderService;
import com.example.marketplace.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ShopOrderService shopOrderService;

    @MockitoBean
    private JwtTockenUtils jwtTockenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldCallService() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("john");
        userDTO.setPassword("secret");
        userDTO.setRole(Role.USER);
        String json = objectMapper.writeValueAsString(userDTO);

        // When / Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userService).registerUser(any(UserDTO.class));
    }

    @Test
    void registerSeller_ShouldCallService() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("seller");
        userDTO.setPassword("secret");
        userDTO.setRole(Role.SELLER);
        String json = objectMapper.writeValueAsString(userDTO);

        // When / Then
        mockMvc.perform(post("/api/user/register/seller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userService).registerSeller(any(UserDTO.class));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("john");
        userDTO.setPassword("secret");
        userDTO.setRole(Role.USER);
        String json = objectMapper.writeValueAsString(userDTO);
        User user = User.builder().username("john").role(Role.USER).build();
        String expectedToken = "jwt.token";

        when(userService.loginUser(any(UserDTO.class))).thenReturn(user);
        when(jwtTockenUtils.generateTocken(user)).thenReturn(expectedToken);

        // When / Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }

    @Test
    void userId_ShouldReturnUserId() throws Exception {
        // Given
        String token = "Bearer valid.token";
        Long userId = 123L;
        when(userService.getUserid(token)).thenReturn(userId);

        // When / Then
        mockMvc.perform(post("/api/user/userid").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("123"));
    }

    @Test
    void getRole_ShouldReturnRole() throws Exception {
        // given
        String token = "Bearer valid.token";
        when(userService.getRole(token)).thenReturn(Role.ADMIN);

        // when / then
        mockMvc.perform(get("/api/user/get_role").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("ADMIN"));
    }

    @Test
    void profile_ShouldReturnProfile() throws Exception {
        String token = "Bearer t";
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername("john");
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPhone("+79990001122");
        dto.setRole(Role.USER);
        when(userService.getProfile(token)).thenReturn(dto);

        mockMvc.perform(get("/api/user/profile").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("+79990001122"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getProfile(token);
    }

    @Test
    void updateProfile_ShouldCallService() throws Exception {
        String token = "Bearer t";
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName("New Name");
        req.setEmail("new@mail.ru");
        req.setPhone("+7888");

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).updateProfile(eq(token), refEq(req));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDTO() throws Exception {
        // Given
        String username = "john";
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("john");
        userDTO.setPassword("encoded");
        userDTO.setRole(Role.USER);
        when(userService.findByUsername(username)).thenReturn(userDTO);

        // When / Then
        mockMvc.perform(get("/api/user/load_user_by_username").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.password").value("encoded"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
