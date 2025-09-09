package com.project.demo.service;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.model.RefreshToken;
import com.project.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock RefreshTokenService refreshTokenService;
    @Mock LoginAttemptService loginAttemptService;
    @Mock JwtUtil jwtUtil;
    @Mock UserService userService;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        // default: not blocked
        Mockito.lenient().when(loginAttemptService.isBlocked(anyString())).thenReturn(false);

        // successful authentication stub
        Authentication mockAuth = mock(Authentication.class);
        Mockito.lenient().when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        Mockito.lenient().when(mockAuth.getName()).thenReturn("validUser");

        // jwt generation
        Mockito.lenient().when(jwtUtil.generateToken(eq("validUser"))).thenReturn("access-token");

        // refresh token created for login
        RefreshToken mockRefresh = new RefreshToken();
        mockRefresh.setUsername("validUser");
        mockRefresh.setToken("refresh-token");
        mockRefresh.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        Mockito.lenient().when(refreshTokenService.createRefreshToken(eq("validUser"))).thenReturn(mockRefresh);

        // refresh flow: existing token mapping
        RefreshToken existing = new RefreshToken();
        existing.setUsername("validUser");
        existing.setToken("old-refresh");
        existing.setExpiryDate(Instant.now().plus(1, ChronoUnit.DAYS));
        Mockito.lenient().when(refreshTokenService.findByToken(eq("old-refresh"))).thenReturn(Optional.of(existing));
        Mockito.lenient().when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void login_shouldReturnAuthResponse_whenValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("validUser");
        req.setPassword("password");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("access-token", res.getAccessToken());
        assertEquals("refresh-token", res.getRefreshToken());
        verify(loginAttemptService).loginSucceeded("validuser"); // usernameKey used lowercased
    }

    @Test
    void login_shouldRecordAttempt_andThrowBadCredentials_whenInvalid() {
        // make authentication manager throw BadCredentialsException
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        var req = new LoginRequest();
        req.setUsername("someone");
        req.setPassword("wrong");

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(req));
        assertTrue(ex.getMessage().contains("Invalid"));
        verify(loginAttemptService).loginFailed("someone");
    }

    @Test
    void login_shouldThrowWhenBlocked() {
        // simulate blocked
        when(loginAttemptService.isBlocked(eq("blockeduser"))).thenReturn(true);
        when(loginAttemptService.getRemainingBlockMillis(eq("blockeduser"))).thenReturn(5000L);

        var req = new LoginRequest();
        req.setUsername("blockedUser");
        req.setPassword("x");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("blocked"));
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenValidRefreshToken() {
        // Arrange: refresh flow already setup in @BeforeEach
        RefreshToken newRt = new RefreshToken();
        newRt.setUsername("validUser");
        newRt.setToken("new-refresh-token");
        newRt.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        Mockito.lenient().when(refreshTokenService.createRefreshToken(eq("validUser"))).thenReturn(newRt);
        Mockito.lenient().when(jwtUtil.generateToken(eq("validUser"))).thenReturn("new-access-token");

        // Act
        AuthResponse resp = authService.refreshToken("old-refresh");

        // Assert
        assertNotNull(resp);
        assertEquals("new-access-token", resp.getAccessToken());
        assertEquals("new-refresh-token", resp.getRefreshToken());
        verify(refreshTokenService).deleteByToken("old-refresh");
    }

    @Test
    void refreshToken_shouldThrow_whenTokenInvalid() {
        when(refreshTokenService.findByToken(eq("nope"))).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.refreshToken("nope"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }
}
