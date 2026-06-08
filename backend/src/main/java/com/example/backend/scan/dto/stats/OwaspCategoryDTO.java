package com.example.backend.scan.dto.stats;

/**
 * DTO représentant une catégorie OWASP avec ses statistiques.
 */
public class OwaspCategoryDTO {

    private String categoryId;       // ex: "A03", "M9"
    private String categoryName;     // ex: "Injection", "Insecure Data Storage"
    private long count;              // nombre de vulnérabilités dans cette catégorie
    private double percentage;       // pourcentage sur le total
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;
    private String lastDetected;     // date de dernière détection ISO-8601

    public OwaspCategoryDTO() {}

    public OwaspCategoryDTO(String categoryId, String categoryName, long count, double percentage,
                            long criticalCount, long highCount, long mediumCount, long lowCount,
                            String lastDetected) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.count = count;
        this.percentage = percentage;
        this.criticalCount = criticalCount;
        this.highCount = highCount;
        this.mediumCount = mediumCount;
        this.lowCount = lowCount;
        this.lastDetected = lastDetected;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public long getCriticalCount() { return criticalCount; }
    public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }
    public long getHighCount() { return highCount; }
    public void setHighCount(long highCount) { this.highCount = highCount; }
    public long getMediumCount() { return mediumCount; }
    public void setMediumCount(long mediumCount) { this.mediumCount = mediumCount; }
    public long getLowCount() { return lowCount; }
    public void setLowCount(long lowCount) { this.lowCount = lowCount; }
    public String getLastDetected() { return lastDetected; }
    public void setLastDetected(String lastDetected) { this.lastDetected = lastDetected; }
}
