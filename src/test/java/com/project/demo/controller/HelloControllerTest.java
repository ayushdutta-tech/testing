package com.project.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for HelloController.
 *
 * The controller currently calls principal.getName() without null-check,
 * so an unauthenticated request will throw a NullPointerException during dispatch.
 * This test verifies both the happy path and the exception case.
 */
class HelloControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HelloController controller = new HelloController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testHelloWithPrincipal() throws Exception {
        Principal alice = () -> "alice";

        mockMvc.perform(get("/api/hello").principal(alice))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, alice! This is a protected resource."));
    }

    @Test
    void testHelloWithoutPrincipal_throwsServletExceptionWithNpeCause() {
        Exception thrown = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/hello")).andReturn();
        });

        // unwrap root cause
        Throwable cause = thrown;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String msg = "Expected root cause to be NullPointerException but was: " + cause.getClass().getName();
        assertTrue(cause instanceof NullPointerException, msg);
    }
}
