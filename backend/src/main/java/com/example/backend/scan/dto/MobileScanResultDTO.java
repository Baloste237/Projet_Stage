package com.example.backend.scan.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileScanResultDTO {
    private Long scanId;
    private String packageName;
    private String versionApp;
    private String statut;
    private Integer nbVulnerabilites;
    private String criticiteMax;
    private Double scoreCvssMax;
    private List<VulnerabiliteDTO> vulnerabilites;

    @Data
    @Builder
    public static class VulnerabiliteDTO {
        private String type;
        private String description;
        private String niveauGravite;
        private String cweId;
        private Double cvssScore;
        private String owaspCategorie;
        private Double confianceML;
        private String localisation;   // fichier ou manifest
        private String recommandation;
    }
}
