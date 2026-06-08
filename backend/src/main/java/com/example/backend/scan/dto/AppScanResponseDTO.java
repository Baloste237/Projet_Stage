package com.example.backend.scan.dto;

import com.example.backend.scan.entity.ScanStatus;
import com.example.backend.scan.entity.ScanType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for scan response.
 */
@Schema(description = "Réponse détaillée d'un scan de sécurité avec statistiques des vulnérabilités")
public class AppScanResponseDTO {
    @Schema(description = "ID unique du scan", example = "1")
    private Long id;

    @Schema(description = "Nom du projet analysé", example = "MonApplicationWeb")
    private String projectName;

    @Schema(description = "Nom du fichier analysé", example = "app-source.zip")
    private String fileName;

    @Schema(description = "Chemin du fichier sur le serveur", example = "/uploads/app-source.zip")
    private String filePath;

    @Schema(description = "Type de scan effectué", example = "SAST")
    private ScanType scanType;

    @Schema(description = "Statut actuel du scan", example = "DONE")
    private ScanStatus status;

    @Schema(description = "Nombre total de vulnérabilités détectées", example = "12")
    private int totalVulnerabilities;

    @Schema(description = "Nombre de vulnérabilités critiques (CVSS ≥ 9.0)", example = "2")
    private int criticalCount;

    @Schema(description = "Nombre de vulnérabilités élevées (CVSS 7.0-8.9)", example = "3")
    private int highCount;

    @Schema(description = "Nombre de vulnérabilités moyennes (CVSS 4.0-6.9)", example = "5")
    private int mediumCount;

    @Schema(description = "Nombre de vulnérabilités faibles (CVSS < 4.0)", example = "2")
    private int lowCount;

    @Schema(description = "Date et heure de création du scan", example = "2026-05-12T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date et heure de fin du scan", example = "2026-05-12T10:32:15")
    private LocalDateTime completedAt;

    @Schema(description = "Progression du scan (0-100)", example = "45")
    private int progress;

    @Schema(description = "Étape actuelle du scan", example = "Analyse statique en cours")
    private String currentStep;

    @Schema(description = "Logs du scan")
    private String logs;

    @Schema(description = "Date de début réel du scan")
    private LocalDateTime startedAt;

    @Schema(description = "Temps d'exécution en ms")
    private Long executionTime;

    public AppScanResponseDTO() {
    }

    public AppScanResponseDTO(Long id, String projectName, String fileName, String filePath, ScanType scanType, ScanStatus status, int totalVulnerabilities, int criticalCount, int highCount, int mediumCount, int lowCount, LocalDateTime createdAt, LocalDateTime completedAt, int progress, String currentStep, String logs, LocalDateTime startedAt, Long executionTime) {
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
        this.progress = progress;
        this.currentStep = currentStep;
        this.logs = logs;
        this.startedAt = startedAt;
        this.executionTime = executionTime;
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

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getLogs() { return logs; }
    public void setLogs(String logs) { this.logs = logs; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }

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
        private int progress;
        private String currentStep;
        private String logs;
        private LocalDateTime startedAt;
        private Long executionTime;

        AppScanResponseDTOBuilder() {
        }

        public AppScanResponseDTOBuilder id(Long id) { this.id = id; return this; }
        public AppScanResponseDTOBuilder projectName(String projectName) { this.projectName = projectName; return this; }
        public AppScanResponseDTOBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public AppScanResponseDTOBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public AppScanResponseDTOBuilder scanType(ScanType scanType) { this.scanType = scanType; return this; }
        public AppScanResponseDTOBuilder status(ScanStatus status) { this.status = status; return this; }
        public AppScanResponseDTOBuilder totalVulnerabilities(int totalVulnerabilities) { this.totalVulnerabilities = totalVulnerabilities; return this; }
        public AppScanResponseDTOBuilder criticalCount(int criticalCount) { this.criticalCount = criticalCount; return this; }
        public AppScanResponseDTOBuilder highCount(int highCount) { this.highCount = highCount; return this; }
        public AppScanResponseDTOBuilder mediumCount(int mediumCount) { this.mediumCount = mediumCount; return this; }
        public AppScanResponseDTOBuilder lowCount(int lowCount) { this.lowCount = lowCount; return this; }
        public AppScanResponseDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AppScanResponseDTOBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public AppScanResponseDTOBuilder progress(int progress) { this.progress = progress; return this; }
        public AppScanResponseDTOBuilder currentStep(String currentStep) { this.currentStep = currentStep; return this; }
        public AppScanResponseDTOBuilder logs(String logs) { this.logs = logs; return this; }
        public AppScanResponseDTOBuilder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public AppScanResponseDTOBuilder executionTime(Long executionTime) { this.executionTime = executionTime; return this; }

        public AppScanResponseDTO build() {
            return new AppScanResponseDTO(id, projectName, fileName, filePath, scanType, status, totalVulnerabilities, criticalCount, highCount, mediumCount, lowCount, createdAt, completedAt, progress, currentStep, logs, startedAt, executionTime);
        }
    }
}