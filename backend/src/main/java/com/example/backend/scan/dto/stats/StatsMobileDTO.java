package com.example.backend.scan.dto.stats;

import java.util.List;

/**
 * DTO de statistiques globales Mobile (OWASP Mobile Top 10 2024).
 */
public class StatsMobileDTO {

    private long totalVulnerabilities;
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private long totalApkAnalyzed;
    private double averageRiskScore;
    private String mostVulnerableProject;
    private String lastScanDate;

    private List<OwaspCategoryDTO> owaspCategories;
    private List<ScanTrendDTO>     scanTrend;

    public StatsMobileDTO() {}

    public long getTotalVulnerabilities() { return totalVulnerabilities; }
    public void setTotalVulnerabilities(long totalVulnerabilities) { this.totalVulnerabilities = totalVulnerabilities; }

    public long getCriticalCount() { return criticalCount; }
    public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }

    public long getHighCount() { return highCount; }
    public void setHighCount(long highCount) { this.highCount = highCount; }

    public long getMediumCount() { return mediumCount; }
    public void setMediumCount(long mediumCount) { this.mediumCount = mediumCount; }

    public long getLowCount() { return lowCount; }
    public void setLowCount(long lowCount) { this.lowCount = lowCount; }

    public long getTotalApkAnalyzed() { return totalApkAnalyzed; }
    public void setTotalApkAnalyzed(long totalApkAnalyzed) { this.totalApkAnalyzed = totalApkAnalyzed; }

    public double getAverageRiskScore() { return averageRiskScore; }
    public void setAverageRiskScore(double averageRiskScore) { this.averageRiskScore = averageRiskScore; }

    public String getMostVulnerableProject() { return mostVulnerableProject; }
    public void setMostVulnerableProject(String mostVulnerableProject) { this.mostVulnerableProject = mostVulnerableProject; }

    public String getLastScanDate() { return lastScanDate; }
    public void setLastScanDate(String lastScanDate) { this.lastScanDate = lastScanDate; }

    public List<OwaspCategoryDTO> getOwaspCategories() { return owaspCategories; }
    public void setOwaspCategories(List<OwaspCategoryDTO> owaspCategories) { this.owaspCategories = owaspCategories; }

    public List<ScanTrendDTO> getScanTrend() { return scanTrend; }
    public void setScanTrend(List<ScanTrendDTO> scanTrend) { this.scanTrend = scanTrend; }
}
