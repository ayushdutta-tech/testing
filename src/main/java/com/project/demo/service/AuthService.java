package com.project.demo.service;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.exception.UserBlockedException;
import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserService userService,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    // Implementing registration in AuthService using UserService
    public String register(RegisterRequest request) {
        // Create a new user object from the register request
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword()); // The password will be encoded in UserService

        // Delegate registration to UserService
        return userService.register(newUser);
    }

    public AuthResponse login(LoginRequest request) {
        final String rawUsername = request.getUsername();
        final String usernameKey = rawUsername == null ? "" : rawUsername.trim().toLowerCase();

        // Check if user is blocked (time-sliced limiter)
        if (loginAttemptService.isBlocked(usernameKey)) {
            long ms = loginAttemptService.getRemainingBlockMillis(usernameKey);
            long seconds = (ms + 999) / 1000; // ceil
            throw new UserBlockedException(
                    "User is blocked due to too many failed login attempts. Try again after " + seconds + " seconds."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            rawUsername,
                            request.getPassword()
                    )
            );

            // success → clear attempts
            loginAttemptService.loginSucceeded(usernameKey);

            // Prefer authentication.getName(), but fall back to the provided username (rawUsername)
            String authName = null;
            if (authentication != null) {
                try {
                    authName = authentication.getName();
                } catch (Exception ex) {
                    logger.warn("Unable to read authentication.getName(): {}", ex.getMessage());
                }
            }
            String effectiveUsername = (authName != null && !authName.isBlank()) ? authName : rawUsername;
            if (effectiveUsername == null) {
                logger.error("No username available from authentication or request");
                throw new IllegalStateException("No username available after authentication");
            }

            String accessToken = jwtUtil.generateToken(effectiveUsername);

            // create refresh token and defend against null
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(effectiveUsername);
            if (refreshToken == null) {
                logger.error("RefreshTokenService.createRefreshToken returned null for user={}", effectiveUsername);
                throw new IllegalStateException("Failed to create refresh token for user: " + effectiveUsername);
            }

            String refreshTokenStr = refreshToken.getToken();
            if (refreshTokenStr == null) {
                logger.error("Created RefreshToken has null token string for user={}", effectiveUsername);
                throw new IllegalStateException("Created refresh token has null token value for user: " + effectiveUsername);
            }

            return new AuthResponse(accessToken, refreshTokenStr);

        } catch (BadCredentialsException e) {
            // failure → record attempt
            loginAttemptService.loginFailed(usernameKey);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(rt -> {
                    String username = rt.getUsername();
                    refreshTokenService.deleteByToken(refreshToken);

                    String newAccessToken = jwtUtil.generateToken(username);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(username);
                    if (newRefreshToken == null || newRefreshToken.getToken() == null) {
                        logger.error("Failed to create new refresh token during refresh flow for user={}", username);
                        throw new IllegalStateException("Failed to create new refresh token for user: " + username);
                    }

                    return new AuthResponse(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }
}
