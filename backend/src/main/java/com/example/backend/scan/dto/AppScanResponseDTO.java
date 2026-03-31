package com.example.backend.scan.dto;

import com.example.backend.scan.entity.ScanStatus;
import com.example.backend.scan.entity.ScanType;


import java.time.LocalDateTime;

/**
 * DTO for scan response.
 */
public class AppScanResponseDTO {
    private Long id;
    private String projectName;
    private String fileName;
    private String filePath;
    private ScanType scanType;
    private ScanStatus status;
    private int totalVulnerabilities;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public AppScanResponseDTO() {
    }

    public AppScanResponseDTO(Long id, String projectName, String fileName, String filePath, ScanType scanType, ScanStatus status, int totalVulnerabilities, int criticalCount, int highCount, int mediumCount, int lowCount, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.projectName = projectName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.scanType = scanType;
        this.status = status;
        this.totalVulnerabilities = totalVulnerabilities;
        this.criticalCount = criticalCount;
        this.highCount = highCount;
        this.mediumCount = mediumCount;
        this.lowCount = lowCount;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static AppScanResponseDTOBuilder builder() {
        return new AppScanResponseDTOBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public ScanType getScanType() { return scanType; }
    public void setScanType(ScanType scanType) { this.scanType = scanType; }

    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }

    public int getTotalVulnerabilities() { return totalVulnerabilities; }
    public void setTotalVulnerabilities(int totalVulnerabilities) { this.totalVulnerabilities = totalVulnerabilities; }

    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }

    public int getHighCount() { return highCount; }
    public void setHighCount(int highCount) { this.highCount = highCount; }

    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }

    public int getLowCount() { return lowCount; }
    public void setLowCount(int lowCount) { this.lowCount = lowCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static class AppScanResponseDTOBuilder {
        private Long id;
        private String projectName;
        private String fileName;
        private String filePath;
        private ScanType scanType;
        private ScanStatus status;
        private int totalVulnerabilities;
        private int criticalCount;
        private int highCount;
        private int mediumCount;
        private int lowCount;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        AppScanResponseDTOBuilder() {
        }

        public AppScanResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AppScanResponseDTOBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public AppScanResponseDTOBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public AppScanResponseDTOBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public AppScanResponseDTOBuilder scanType(ScanType scanType) {
            this.scanType = scanType;
            return this;
        }

        public AppScanResponseDTOBuilder status(ScanStatus status) {
            this.status = status;
            return this;
        }

        public AppScanResponseDTOBuilder totalVulnerabilities(int totalVulnerabilities) {
            this.totalVulnerabilities = totalVulnerabilities;
            return this;
        }

        public AppScanResponseDTOBuilder criticalCount(int criticalCount) {
            this.criticalCount = criticalCount;
            return this;
        }

        public AppScanResponseDTOBuilder highCount(int highCount) {
            this.highCount = highCount;
            return this;
        }

        public AppScanResponseDTOBuilder mediumCount(int mediumCount) {
            this.mediumCount = mediumCount;
            return this;
        }

        public AppScanResponseDTOBuilder lowCount(int lowCount) {
            this.lowCount = lowCount;
            return this;
        }

        public AppScanResponseDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AppScanResponseDTOBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public AppScanResponseDTO build() {
            return new AppScanResponseDTO(id, projectName, fileName, filePath, scanType, status, totalVulnerabilities, criticalCount, highCount, mediumCount, lowCount, createdAt, completedAt);
        }
    }
}