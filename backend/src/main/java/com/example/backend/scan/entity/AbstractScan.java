package com.example.backend.scan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Abstract entity representing an application scan.
 */
@Entity
@Table(name = "abstract_scans")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanType scanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;

    @Column(nullable = false)
    private int totalVulnerabilities;

    @Column(nullable = false)
    private int criticalCount;

    @Column(nullable = false)
    private int highCount;

    @Column(nullable = false)
    private int mediumCount;

    @Column(nullable = false)
    private int lowCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public AbstractScan() {
    }

    public AbstractScan(Long id, String projectName, String fileName, String filePath, ScanType scanType, ScanStatus status, int totalVulnerabilities, int criticalCount, int highCount, int mediumCount, int lowCount, LocalDateTime createdAt, LocalDateTime completedAt) {
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
}
