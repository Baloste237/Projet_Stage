package com.example.backend.scan.dto;

public class OwaspMappingResult {
    private String owaspVersion;
    private String owaspId;
    private String owaspName;
    private String legacyCategory;

    public OwaspMappingResult(String owaspVersion, String owaspId, String owaspName, String legacyCategory) {
        this.owaspVersion = owaspVersion;
        this.owaspId = owaspId;
        this.owaspName = owaspName;
        this.legacyCategory = legacyCategory;
    }

    public String getOwaspVersion() {
        return owaspVersion;
    }

    public void setOwaspVersion(String owaspVersion) {
        this.owaspVersion = owaspVersion;
    }

    public String getOwaspId() {
        return owaspId;
    }

    public void setOwaspId(String owaspId) {
        this.owaspId = owaspId;
    }

    public String getOwaspName() {
        return owaspName;
    }

    public void setOwaspName(String owaspName) {
        this.owaspName = owaspName;
    }

    public String getLegacyCategory() {
        return legacyCategory;
    }

    public void setLegacyCategory(String legacyCategory) {
        this.legacyCategory = legacyCategory;
    }
}
