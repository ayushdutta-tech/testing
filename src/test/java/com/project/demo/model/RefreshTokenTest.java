package com.project.demo.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefreshToken entity.
 */
class RefreshTokenTest {

    @Test
    void testDefaultConstructorAndSettersGetters() {
        RefreshToken token = new RefreshToken();

        Long id = 1L;
        String tokenValue = "abc123";
        String username = "testUser";
        Instant expiryDate = Instant.now().plusSeconds(3600);

        token.setId(id);
        token.setToken(tokenValue);
        token.setUsername(username);
        token.setExpiryDate(expiryDate);

        assertEquals(id, token.getId());
        assertEquals(tokenValue, token.getToken());
        assertEquals(username, token.getUsername());
        assertEquals(expiryDate, token.getExpiryDate());
    }

    @Test
    void testAllFieldsIndependently() {
        RefreshToken token = new RefreshToken();

        token.setId(42L);
        assertEquals(42L, token.getId());

        token.setToken("refresh-xyz");
        assertEquals("refresh-xyz", token.getToken());

        token.setUsername("alice");
        assertEquals("alice", token.getUsername());

        Instant expiry = Instant.now();
        token.setExpiryDate(expiry);
        assertEquals(expiry, token.getExpiryDate());
    }
}
