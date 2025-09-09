package com.project.demo.exception;

/** 401 Unauthorized - invalid token provided */
public class InvalidTokenException extends AppException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}