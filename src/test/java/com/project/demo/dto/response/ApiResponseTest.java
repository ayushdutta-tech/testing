package com.project.demo.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponse DTO.
 */
class ApiResponseTest {

    @Test
    void success_withData_shouldCreateValidResponse() {
        ApiResponse<String> response = ApiResponse.success("Operation successful", "DATA123");

        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals("DATA123", response.getData());
        assertNull(response.getError());
    }

    @Test
    void success_withoutData_shouldCreateValidResponse() {
        ApiResponse<String> response = ApiResponse.success("No data");

        assertTrue(response.isSuccess());
        assertEquals("No data", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getError());
    }

    @Test
    void error_withMessage_shouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertFalse(response.isSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertEquals("Something went wrong", response.getError());
        assertNull(response.getData());
    }

    @Test
    void error_withMessageAndData_shouldCreateErrorResponseWithData() {
        ApiResponse<String> response = ApiResponse.error("Validation failed", "invalid_field");

        assertFalse(response.isSuccess());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("Validation failed", response.getError());
        assertEquals("invalid_field", response.getData());
    }
}
