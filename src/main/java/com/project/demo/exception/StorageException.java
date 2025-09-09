package com.project.demo.exception;

/** 500 Internal Server Error - file read/write/storage problem */
public class StorageException extends AppException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}