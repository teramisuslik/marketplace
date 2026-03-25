package com.example.controller.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilsTest {

    private JwtTokenUtils jwtUtils;
    private final String secret = "0445b6b15da00f3d3836b6dbe1cd95cdb5e81066f83ffdceed3fbd9ad26d9bc2";
    private Key signingKey;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtTokenUtils();
        jwtUtils.setSecret(secret);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        // Given
        String username = "testuser";
        String token = createToken(username, 1000 * 60 * 60); // 1 hour validity

        // When
        String extracted = jwtUtils.getUsernameFromToken(token);

        // Then
        assertEquals(username, extracted);
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        // Given
        String username = "testuser";
        String token = createToken(username, 1000 * 60 * 60);
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .roles("USER")
                .build();

        // When
        boolean valid = jwtUtils.validateToken(token, userDetails);

        // Then
        assertTrue(valid);
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        // Given
        String username = "testuser";
        String token = createToken(username, -1000);
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .roles("USER")
                .build();

        // When
        boolean valid = jwtUtils.validateToken(token, userDetails);

        // Then
        assertFalse(valid);
    }

    @Test
    void validateToken_wrongUsername_shouldReturnFalse() {
        // Given
        String token = createToken("testuser", 1000 * 60 * 60);
        UserDetails userDetails = User.builder()
                .username("wronguser")
                .password("")
                .roles("USER")
                .build();

        // When
        boolean valid = jwtUtils.validateToken(token, userDetails);

        // Then
        assertFalse(valid);
    }

    private String createToken(String username, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}