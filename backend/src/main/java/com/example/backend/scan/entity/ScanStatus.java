package com.example.backend.scan.entity;

/**
 * Enum representing the status of a scan.
 */
public enum ScanStatus {
    PENDING, // Scan is queued and waiting to start
    PROCESSING, // Scan is currently running
    DONE, // Scan completed successfully
    FAILED // Scan failed
}