package com.example.controller.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtTokenUtilsTest {

    private JwtTokenUtils jwtTokenUtils;
    private final String testSecret = "0445b6b15da00f3d3836b6dbe1cd95cdb5e81066f83ffdceed3fbd9ad26d9bc2";

    @BeforeEach
    void setUp() {
        jwtTokenUtils = new JwtTokenUtils();
        jwtTokenUtils.setSecret(testSecret);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(jwtTokenUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        String extracted = jwtTokenUtils.getUsernameFromToken(token);
        assertThat(extracted).isEqualTo(username);
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        String username = "john";
        UserDetails userDetails =
                User.withUsername(username).password("").roles("USER").build();
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(jwtTokenUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        boolean valid = jwtTokenUtils.validateToken(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() {
        String username = "expired";
        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(jwtTokenUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        UserDetails userDetails =
                User.withUsername(username).password("").roles("USER").build();

        boolean valid = jwtTokenUtils.validateToken(token, userDetails);
        assertThat(valid).isFalse();
    }
}
