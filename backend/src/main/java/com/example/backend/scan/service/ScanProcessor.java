package com.example.backend.scan.service;

import com.example.backend.scan.entity.AbstractScan;

/**
 * Strategy interface for specific scan processors.
 */
public interface ScanProcessor {

    /**
     * Checks if this processor supports the given application type.
     * 
     * @param appType the type of application
     * @return true if supported
     */
    boolean supports(String appType);

    /**
     * Creates the specific entity for this processor.
     * 
     * @return the newly instantiated scan entity
     */
    AbstractScan createEmptyScanEntity();
}
