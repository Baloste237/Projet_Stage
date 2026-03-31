package com.example.backend.scan.dto;

/**
 * DTO for scan request containing project name.
 */
public class AppScanRequestDTO {
    private String projectName;

    public AppScanRequestDTO() {
    }

    public AppScanRequestDTO(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}