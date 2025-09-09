package com.project.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    void testHelloWithoutPrincipal_returnsAnonymous() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, anonymous! This is a protected resource."));
    }
}
