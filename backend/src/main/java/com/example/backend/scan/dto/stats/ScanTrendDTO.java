package com.example.backend.scan.dto.stats;

/**
 * DTO pour l'évolution des scans dans le temps (trend mensuel).
 */
public class ScanTrendDTO {

    private String period;       // ex: "2026-01", "2026-02"
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private long totalScans;

    public ScanTrendDTO() {}

    public ScanTrendDTO(String period, long criticalCount, long highCount,
                        long mediumCount, long lowCount, long totalScans) {
        this.period = period;
        this.criticalCount = criticalCount;
        this.highCount = highCount;
        this.mediumCount = mediumCount;
        this.lowCount = lowCount;
        this.totalScans = totalScans;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public long getCriticalCount() { return criticalCount; }
    public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }

    public long getHighCount() { return highCount; }
    public void setHighCount(long highCount) { this.highCount = highCount; }

    public long getMediumCount() { return mediumCount; }
    public void setMediumCount(long mediumCount) { this.mediumCount = mediumCount; }

    public long getLowCount() { return lowCount; }
    public void setLowCount(long lowCount) { this.lowCount = lowCount; }

    public long getTotalScans() { return totalScans; }
    public void setTotalScans(long totalScans) { this.totalScans = totalScans; }
}
