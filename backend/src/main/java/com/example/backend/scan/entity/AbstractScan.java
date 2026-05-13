package com.example.backend.scan.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Abstract entity representing an application scan.
 */
@Entity
@Table(name = "abstract_scans")
@Inheritance(strategy = InheritanceType.JOINED)
@Schema(description = "Entité abstraite représentant un scan de sécurité applicative")
public abstract class AbstractScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unique du scan", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nom du projet analysé", example = "MonApplicationWeb")
    private String projectName;

    @Column(nullable = false)
    @Schema(description = "Nom du fichier analysé", example = "app-source.zip")
    private String fileName;

    @Column(nullable = false)
    @Schema(description = "Chemin du fichier sur le serveur", example = "/uploads/app-source.zip")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Type de scan", example = "SAST")
    private ScanType scanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Statut du scan", example = "DONE")
    private ScanStatus status;

    @Column(nullable = false)
    @Schema(description = "Nombre total de vulnérabilités détectées", example = "12")
    private int totalVulnerabilities;

    @Column(nullable = false)
    @Schema(description = "Vulnérabilités critiques (CVSS ≥ 9.0)", example = "2")
    private int criticalCount;

    @Column(nullable = false)
    @Schema(description = "Vulnérabilités élevées (CVSS 7.0-8.9)", example = "3")
    private int highCount;

    @Column(nullable = false)
    @Schema(description = "Vulnérabilités moyennes (CVSS 4.0-6.9)", example = "5")
    private int mediumCount;

    @Column(nullable = false)
    @Schema(description = "Vulnérabilités faibles (CVSS < 4.0)", example = "2")
    private int lowCount;

    @Column(nullable = false)
    @Schema(description = "Date de création du scan", example = "2026-05-12T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de fin du scan", example = "2026-05-12T10:32:15")
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
