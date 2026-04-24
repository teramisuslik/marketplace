package com.example.controller.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.controller.DTO.Role;
import com.example.controller.DTO.UserDTO;
import com.example.controller.client.UserClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    @Mock
    private JwtTokenUtils jwtUtils;

    @Mock
    private UserClient userClient;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtTokenFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthHeader_shouldProceedWithoutAuthentication() throws ServletException, IOException {
        // Given – no Authorization header
        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidAuthHeader_shouldProceedWithoutAuthentication() throws ServletException, IOException {
        // Given – header without "Bearer "
        request.addHeader("Authorization", "Invalid token");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validToken_shouldAuthenticate() throws ServletException, IOException {
        // Given
        String token = "Bearer valid.jwt.token";
        request.addHeader("Authorization", token);
        String username = "testuser";
        when(jwtUtils.getUsernameFromToken(anyString())).thenReturn(username);
        UserDTO userDto = new UserDTO();
        userDto.setUsername(username);
        userDto.setPassword("password");
        userDto.setRole(Role.USER);
        when(userClient.findUserByUsername(username)).thenReturn(userDto);
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .roles("USER")
                .build();
        when(jwtUtils.validateToken(anyString(), eq(userDetails))).thenReturn(true);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                username, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_validTokenButUserNotFound_shouldProceedWithoutAuthentication()
            throws ServletException, IOException {
        // Given
        String token = "Bearer valid.jwt.token";
        request.addHeader("Authorization", token);
        String username = "unknown";
        when(jwtUtils.getUsernameFromToken(anyString())).thenReturn(username);
        when(userClient.findUserByUsername(username)).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidToken_shouldProceedWithoutAuthentication() throws ServletException, IOException {
        // Given
        String token = "Bearer invalid.token";
        request.addHeader("Authorization", token);
        String username = "testuser";
        when(jwtUtils.getUsernameFromToken(anyString())).thenReturn(username);
        UserDTO userDto = new UserDTO();
        userDto.setUsername(username);
        userDto.setPassword("password");
        userDto.setRole(Role.USER);
        when(userClient.findUserByUsername(username)).thenReturn(userDto);
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .roles("USER")
                .build();
        when(jwtUtils.validateToken(anyString(), eq(userDetails))).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
