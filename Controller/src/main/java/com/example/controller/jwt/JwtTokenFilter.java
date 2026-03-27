package com.example.controller.jwt;

import com.example.controller.DTO.UserDTO;
import com.example.controller.client.UserClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserClient userClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authToken = extractBearerToken(request.getHeader("Authorization"));
        if (authToken == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String username = jwtTokenUtils.getUsernameFromToken(authToken);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDTO userDto = userClient.findUserByUsername(username);
            if (userDto == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Преобразуем в UserDetails
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(userDto.getUsername())
                    .password(userDto.getPassword())
                    .roles(userDto.getRole().name())
                    .build();
            if (jwtTokenUtils.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Swagger уже шлёт префикс Bearer; если вставить токен с ещё одним "Bearer " или в кавычках из JSON — парсинг JWT
     * ломается и Spring Security даёт 403.
     */
    private static String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        String value = authHeader.trim();
        if (value.length() >= 7 && value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        } else {
            return null;
        }
        while (value.length() >= 7 && value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        }
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value.isEmpty() ? null : value;
    }
}