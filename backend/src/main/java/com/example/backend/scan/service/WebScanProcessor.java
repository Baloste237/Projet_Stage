package com.example.backend.scan.service;

import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.WebAppScan;
import org.springframework.stereotype.Component;

/**
 * Processor for Web Application Scans.
 */
@Component
public class WebScanProcessor implements ScanProcessor {

    @Override
    public boolean supports(String appType) {
        return "web".equalsIgnoreCase(appType);
    }

    @Override
    public AbstractScan createEmptyScanEntity() {
        WebAppScan scan = new WebAppScan();
        scan.setFrontendFramework("Unknown"); // default value
        return scan;
    }
}
