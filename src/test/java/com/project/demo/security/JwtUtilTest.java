package com.project.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Secret key must be at least 32 chars for HMAC-SHA256
        String secret = "12345678901234567890123456789012";
        long expirationMs = 3600000; // 1 hour
        jwtUtil = new JwtUtil(secret, expirationMs);
    }

    @Test
    void generateAndValidateToken_shouldReturnValid() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void isValid_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.isValid("invalid.token.value"));
    }
}
