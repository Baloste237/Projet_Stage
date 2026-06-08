package com.example.backend.scan.dto.stats;

/**
 * DTO résumé global dashboard : stats web + mobile combinées.
 */
public class DashboardStatsDTO {

    private long totalWebVulnerabilities;
    private long totalMobileVulnerabilities;
    private long totalWebScans;
    private long totalMobileScans;
    private long totalCritical;
    private long totalHigh;
    private String lastWebScanDate;
    private String lastMobileScanDate;

    public DashboardStatsDTO() {}

    public long getTotalWebVulnerabilities() { return totalWebVulnerabilities; }
    public void setTotalWebVulnerabilities(long v) { this.totalWebVulnerabilities = v; }

    public long getTotalMobileVulnerabilities() { return totalMobileVulnerabilities; }
    public void setTotalMobileVulnerabilities(long v) { this.totalMobileVulnerabilities = v; }

    public long getTotalWebScans() { return totalWebScans; }
    public void setTotalWebScans(long v) { this.totalWebScans = v; }

    public long getTotalMobileScans() { return totalMobileScans; }
    public void setTotalMobileScans(long v) { this.totalMobileScans = v; }

    public long getTotalCritical() { return totalCritical; }
    public void setTotalCritical(long v) { this.totalCritical = v; }

    public long getTotalHigh() { return totalHigh; }
    public void setTotalHigh(long v) { this.totalHigh = v; }

    public String getLastWebScanDate() { return lastWebScanDate; }
    public void setLastWebScanDate(String v) { this.lastWebScanDate = v; }

    public String getLastMobileScanDate() { return lastMobileScanDate; }
    public void setLastMobileScanDate(String v) { this.lastMobileScanDate = v; }
}
