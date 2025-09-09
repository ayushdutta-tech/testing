package com.project.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.security.JwtUtil;
import com.project.demo.service.RefreshTokenService;
import com.project.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.ApiResponse;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.service.AuthService;
import com.project.demo.service.LoginAuditService;
import com.project.demo.util.CookieUtil;
import com.project.demo.util.RequestInfoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final LoginAuditService loginAuditService;

    public AuthController(AuthService authService, LoginAuditService loginAuditService) {
        this.authService = authService;
        this.loginAuditService = loginAuditService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        String message = authService.register(request);
        log.info("User registered successfully: {}", request.getUsername());

        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        log.info("Login attempt for user: {}", request.getUsername());

        AuthResponse authResponse = authService.login(request);
        CookieUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());

        Map<String, Object> loginInfo = RequestInfoUtil.extractLoginInfo(
                request.getUsername(), httpRequest);
        loginAuditService.saveLoginLog(loginInfo);
        log.info("Login successful for user: {} | IP: {}",
                request.getUsername(), loginInfo.get("ip"));

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest) {

        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token not found in request body");
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        log.info("Access token refreshed successfully");
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }
    @Setter
    @Getter
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.getRefreshTokenFromCookies(request)
                .ifPresent(authService::logout);

        CookieUtil.clearRefreshTokenCookie(response);
        log.info("User logged out successfully");

        return ResponseEntity.ok(ApiResponse.success("Successfully logged out"));
    }
}
