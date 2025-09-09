package com.project.demo.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthResponse DTO.
 */
class AuthResponseTest {

    @Test
    void constructor_shouldSetAccessAndRefreshToken() {
        AuthResponse response = new AuthResponse("access123", "refresh456");

        assertEquals("access123", response.getAccessToken());
        assertEquals("refresh456", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        AuthResponse response = new AuthResponse();

        response.setAccessToken("accessXYZ");
        response.setRefreshToken("refreshXYZ");
        response.setTokenType("CustomBearer");

        assertEquals("accessXYZ", response.getAccessToken());
        assertEquals("refreshXYZ", response.getRefreshToken());
        assertEquals("CustomBearer", response.getTokenType());
    }
}
