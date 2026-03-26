package com.example.marketplace.jwt;

import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTockenUtilsTest {

    private JwtTockenUtils jwtTockenUtils;

    @BeforeEach
    void setUp() {
        jwtTockenUtils = new JwtTockenUtils();
        jwtTockenUtils.setSecret("0445b6b15da00f3d3836b6dbe1cd95cdb5e81066f83ffdceed3fbd9ad26d9bc2");
        jwtTockenUtils.setLifetime(60); // 1 hour
    }

    @Test
    void generateToken_ShouldCreateValidJwt() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("pwd")
                .role(Role.ADMIN)
                .build();

        // When
        String token = jwtTockenUtils.generateTocken(user);

        // Then
        assertThat(token).isNotEmpty();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtTockenUtils.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Given
        User user = User.builder().username("testuser").build();
        String token = jwtTockenUtils.generateTocken(user);

        // When
        String username = jwtTockenUtils.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void getClaimFromToken_ShouldReturnSpecificClaim() {
        // Given
        User user = User.builder().username("testuser").build();
        String token = jwtTockenUtils.generateTocken(user);

        // When
        String subject = jwtTockenUtils.getClaimFromToken(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo("testuser");
    }
}
