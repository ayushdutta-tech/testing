package com.project.demo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthFilter.
 *
 * Scenarios:
 *  - valid Bearer token: JwtUtil.isValid -> true, username extracted, user loaded and SecurityContext set
 *  - invalid Bearer token: JwtUtil.isValid -> false -> SecurityContext remains null
 *  - no Authorization header -> filter should simply pass the chain and not set auth
 */
class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        // clear before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        // Arrange
        String token = "valid-token";
        String header = "Bearer " + token;
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", header);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtUtil.isValid(eq(token))).thenReturn(true);
        when(jwtUtil.extractUsername(eq(token))).thenReturn("alice");

        // create a simple UserDetails - authorities empty
        UserDetails userDetails = new User("alice", "pwd", Collections.emptyList());
        when(userDetailsService.loadUserByUsername(eq("alice"))).thenReturn(userDetails);

        // Act
        filter.doFilterInternal(req, resp, chain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set in SecurityContext");
        assertEquals("alice", auth.getName());
        // ensure the chain proceeded
        assertEquals(200, resp.getStatus()); // default status untouched
        verify(jwtUtil).isValid(token);
        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername("alice");
    }

    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        // Arrange
        String token = "invalid-token";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtUtil.isValid(eq(token))).thenReturn(false);

        // Act
        filter.doFilterInternal(req, resp, chain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Authentication should NOT be set for invalid token");
        verify(jwtUtil).isValid(token);
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_doesNotSetAuthentication() throws Exception {
        // Arrange
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(req, resp, chain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should be null when no Authorization header is present");
        verifyNoInteractions(jwtUtil, userDetailsService);
    }
}
