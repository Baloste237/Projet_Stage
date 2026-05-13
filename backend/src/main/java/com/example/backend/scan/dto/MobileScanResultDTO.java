package com.example.backend.scan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Résultat détaillé d'un scan de sécurité mobile (APK via MobSF)")
public class MobileScanResultDTO {
    @Schema(description = "ID du scan", example = "1")
    private Long scanId;

    @Schema(description = "Nom du package Android", example = "com.example.myapp")
    private String packageName;

    @Schema(description = "Version de l'application", example = "1.2.3")
    private String versionApp;

    @Schema(description = "Statut du scan", example = "DONE")
    private String statut;

    @Schema(description = "Nombre total de vulnérabilités", example = "8")
    private Integer nbVulnerabilites;

    @Schema(description = "Niveau de criticité maximum détecté", example = "HIGH")
    private String criticiteMax;

    @Schema(description = "Score CVSS maximum détecté", example = "8.5")
    private Double scoreCvssMax;

    @Schema(description = "Liste des vulnérabilités détectées")
    private List<VulnerabiliteDTO> vulnerabilites;

    @Data
    @Builder
    @Schema(description = "Détail d'une vulnérabilité mobile")
    public static class VulnerabiliteDTO {
        @Schema(description = "Type de vulnérabilité", example = "Insecure Data Storage")
        private String type;

        @Schema(description = "Description technique", example = "Application stores sensitive data in shared preferences without encryption")
        private String description;

        @Schema(description = "Niveau de gravité", example = "HIGH")
        private String niveauGravite;

        @Schema(description = "Identifiant CWE", example = "CWE-312")
        private String cweId;

        @Schema(description = "Score CVSS v3", example = "7.5")
        private Double cvssScore;

        @Schema(description = "Catégorie OWASP Mobile", example = "M2: Insecure Data Storage")
        private String owaspCategorie;

        @Schema(description = "Score de confiance du modèle ML (0.0 à 1.0)", example = "0.92")
        private Double confianceML;

        @Schema(description = "Fichier ou manifest concerné", example = "AndroidManifest.xml")
        private String localisation;

        @Schema(description = "Recommandation de correction", example = "Utiliser EncryptedSharedPreferences pour le stockage sécurisé")
        private String recommandation;
    }
}
