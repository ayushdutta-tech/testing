package com.project.demo.exception;

/**
 * Base class for application runtime exceptions.
 * All custom unchecked exceptions should extend this class.
 */
public abstract class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}