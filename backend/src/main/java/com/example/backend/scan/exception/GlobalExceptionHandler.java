package com.example.backend.scan.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ScanException.class)
    public ResponseEntity<Map<String, String>> handleScanException(ScanException ex) {
        log.error("Scan exception: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Scan Error");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "File Size Exceeded");
        error.put("message", "The uploaded file is too large. Maximum allowed size is 10MB.");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}