package com.example.backend.scan.entity;

/**
 * Enum representing the status of a scan.
 */
public enum ScanStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}