package com.example.marketplace.service;

import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTockenUtils jwtTockenUtils;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setRole(Role.USER);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void registerUser_ShouldSaveUserWithEncodedPasswordAndRoleUser() {
        // Given
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.registerUser(userDTO);

        // Then
        verify(userRepository).save(argThat(u ->
                u.getUsername().equals(userDTO.getUsername()) &&
                        u.getPassword().equals("encodedPassword") &&
                        u.getRole() == Role.USER
        ));
    }

    @Test
    void registerSeller_ShouldSaveUserWithEncodedPasswordAndRoleSeller() {
        // Given
        userDTO.setRole(Role.SELLER);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User savedUser = userService.registerSeller(userDTO);

        // Then
        verify(userRepository).save(argThat(u ->
                u.getUsername().equals(userDTO.getUsername()) &&
                        u.getPassword().equals("encodedPassword") &&
                        u.getRole() == Role.SELLER
        ));
        assertThat(savedUser).isNotNull();
    }

    @Test
    void loginUser_WithValidCredentials_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername(userDTO.getUsername()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userDTO.getPassword(), user.getPassword()))
                .thenReturn(true);

        // When
        User result = userService.loginUser(userDTO);

        // Then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void loginUser_WithInvalidUsername_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername(userDTO.getUsername()))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.loginUser(userDTO))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void loginUser_WithInvalidPassword_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername(userDTO.getUsername()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userDTO.getPassword(), user.getPassword()))
                .thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> userService.loginUser(userDTO))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void getUserid_WithValidToken_ShouldReturnUserId() {
        // Given
        String token = "Bearer valid.jwt.token";
        String username = "testuser";
        when(jwtTockenUtils.getUsernameFromToken("valid.jwt.token")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        Long userId = userService.getUserid(token);

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void getUserid_WithInvalidToken_ShouldThrowUsernameNotFoundException() {
        // Given
        String token = "Bearer invalid.token";
        when(jwtTockenUtils.getUsernameFromToken("invalid.token")).thenReturn("unknown");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getUserid(token))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid username");
    }

    @Test
    void getRole_WithValidToken_ShouldReturnRole() {
        // Given
        String token = "Bearer valid.jwt.token";
        String username = "testuser";
        when(jwtTockenUtils.getUsernameFromToken("valid.jwt.token")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        Role role = userService.getRole(token);

        // Then
        assertThat(role).isEqualTo(Role.USER);
    }

    @Test
    void findByUsername_WithExistingUsername_ShouldReturnUserDTO() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDTO found = userService.findByUsername("testuser");

        // Then
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getPassword()).isEqualTo("encodedPassword");
        assertThat(found.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void findByUsername_WithNonExistingUsername_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Invalid username");
    }
}
