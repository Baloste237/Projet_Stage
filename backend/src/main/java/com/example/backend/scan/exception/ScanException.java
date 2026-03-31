package com.example.backend.scan.exception;

/**
 * Custom exception for scan-related errors.
 */
public class ScanException extends RuntimeException {

    public ScanException(String message) {
        super(message);
    }

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}