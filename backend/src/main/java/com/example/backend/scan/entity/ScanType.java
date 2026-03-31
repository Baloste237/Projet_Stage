package com.example.backend.scan.entity;

/**
 * Enum representing the type of scan: SAST, DAST, or HYBRID.
 */
public enum ScanType {
    SAST, // Static Application Security Testing
    DAST, // Dynamic Application Security Testing
    HYBRID // Combination of SAST and DAST
}