package com.project.demo.exception;

/** 409 Conflict - duplicate resource (e.g., username already taken) */
public class DuplicateResourceException extends AppException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}