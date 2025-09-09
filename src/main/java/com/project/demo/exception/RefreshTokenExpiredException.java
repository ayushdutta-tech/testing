package com.project.demo.exception;

import java.time.Instant;

/** 401 Unauthorized - refresh token expired */
public class RefreshTokenExpiredException extends AppException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }

    public RefreshTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenExpiredException(String message, Instant expiredAt) {
        super(message + " (expired at: " + expiredAt + ")");
    }
}