package com.digitalmarketplace.security;

import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-hs256-that-is-long-enough-1234";

    private final JwtService jwtService = new JwtService(SECRET, 3600000);

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setRole(UserRole.SELLER);
        return user;
    }

    @Test
    void generateAndParseTokenRoundTripsClaims() {
        String token = jwtService.generateToken(user());

        Claims claims = jwtService.parseToken(token);

        assertEquals("42", claims.getSubject());
        assertEquals("alice@example.com", claims.get("email"));
        assertEquals("Alice", claims.get("name"));
        assertEquals(List.of("ROLE_SELLER"), claims.get("roles", List.class));
    }

    @Test
    void parseTokenRejectsTamperedToken() {
        String token = jwtService.generateToken(user());
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThrows(JwtException.class, () -> jwtService.parseToken(tampered));
    }

    @Test
    void parseTokenRejectsExpiredToken() {
        JwtService shortLived = new JwtService(SECRET, -1000);
        String token = shortLived.generateToken(user());

        assertThrows(JwtException.class, () -> jwtService.parseToken(token));
    }

    @Test
    void getExpirationSecondsReturnsSeconds() {
        assertEquals(3600, jwtService.getExpirationSeconds());
    }
}
