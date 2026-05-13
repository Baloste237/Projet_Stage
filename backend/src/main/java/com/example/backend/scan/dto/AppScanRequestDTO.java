package com.example.backend.scan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for scan request containing project name.
 */
@Schema(description = "Requête de scan contenant le nom du projet à analyser")
public class AppScanRequestDTO {
    @Schema(description = "Nom du projet à analyser", example = "MonApplicationWeb", requiredMode = Schema.RequiredMode.REQUIRED)
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