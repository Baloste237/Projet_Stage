package com.example.backend.scan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity representing a Mobile Application scan.
 */
@Entity
@Table(name = "mobile_app_scans")
public class MobileAppScan extends AbstractScan {

    private String targetOs;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String rawCodeAnalysis;

    public MobileAppScan() {
        super();
    }

    public String getTargetOs() { return targetOs; }
    public void setTargetOs(String targetOs) { this.targetOs = targetOs; }

    public String getRawCodeAnalysis() { return rawCodeAnalysis; }
    public void setRawCodeAnalysis(String rawCodeAnalysis) { this.rawCodeAnalysis = rawCodeAnalysis; }
}
