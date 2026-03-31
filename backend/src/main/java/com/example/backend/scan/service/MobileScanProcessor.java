package com.example.backend.scan.service;

import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.MobileAppScan;
import org.springframework.stereotype.Component;

/**
 * Processor for Mobile Application Scans.
 */
@Component
public class MobileScanProcessor implements ScanProcessor {

    @Override
    public boolean supports(String appType) {
        return "mobile".equalsIgnoreCase(appType);
    }

    @Override
    public AbstractScan createEmptyScanEntity() {
        MobileAppScan scan = new MobileAppScan();
        scan.setTargetOs("Unknown"); // default value
        return scan;
    }
}
