package com.example.backend.scan.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name="vulnerabilites")
@Schema(description = "Entité représentant une vulnérabilité de sécurité détectée lors d'un scan")
public class Vulnerabilite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unique de la vulnérabilité", example = "42")
    private Long id;

    @Column(length = 1000)
    @Schema(description = "Type / catégorie de la vulnérabilité", example = "SQL Injection")
    private String type;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description technique détaillée de la vulnérabilité")
    private String description;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Niveau de gravité", example = "HIGH")
    private SeverityEnum niv_grav;

    @Column(length = 255)
    @Schema(description = "Identifiant CWE", example = "CWE-89")
    private String cweId;

    @Schema(description = "Score CVSS v3 (0.0 à 10.0)", example = "8.6")
    private Double cvssScore;

    @Column(length = 1000)
    @Schema(description = "Catégorie OWASP", example = "A03:2021 - Injection")
    private String owaspcat;
    
    // Champs optionnels de localisation dans le projet
    @Column(name = "target_file", length = 1000)
    @Schema(description = "Fichier source contenant la vulnérabilité", example = "src/main/java/com/example/UserController.java")
    private String targetFile;
    
    @Column(name = "target_line")
    @Schema(description = "Numéro de ligne dans le fichier source", example = "45")
    private Integer targetLine;

    @Column(name = "status")
    @Schema(description = "Statut de validation", example = "À VALIDER", allowableValues = {"À VALIDER", "CONFIRMÉ", "FAUX POSITIF", "CORRIGÉ"})
    private String status = "À VALIDER";

    @Column(name = "recommendation", length = 1000)
    @Schema(description = "Recommandation de correction", example = "Utiliser des requêtes préparées (PreparedStatement)")
    private String recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    @Schema(description = "Scan parent associé à cette vulnérabilité")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "logs", "rawCodeAnalysis"})
    private AbstractScan scan;

    public Vulnerabilite() {
    }

    public Vulnerabilite(Long id, String type, String description, SeverityEnum niv_grav, String cweId, Double cvssScore, String owaspcat, AbstractScan scan) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.niv_grav = niv_grav;
        this.cweId = cweId;
        this.cvssScore = cvssScore;
        this.owaspcat = owaspcat;
        this.scan = scan;
    }

    public Vulnerabilite(Long id, String type, String description, SeverityEnum niv_grav, String cweId, Double cvssScore, String owaspcat, String targetFile, Integer targetLine, AbstractScan scan) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.niv_grav = niv_grav;
        this.cweId = cweId;
        this.cvssScore = cvssScore;
        this.owaspcat = owaspcat;
        this.targetFile = targetFile;
        this.targetLine = targetLine;
        this.scan = scan;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SeverityEnum getNiv_grav() { return niv_grav; }
    public void setNiv_grav(SeverityEnum niv_grav) { this.niv_grav = niv_grav; }
    public String getCweId() { return cweId; }
    public void setCweId(String cweId) { this.cweId = cweId; }
    public Double getCvssScore() { return cvssScore; }
    public void setCvssScore(Double cvssScore) { this.cvssScore = cvssScore; }
    public String getOwaspcat() { return owaspcat; }
    public void setOwaspcat(String owaspcat) { this.owaspcat = owaspcat; }
    public AbstractScan getScan() { return scan; }
    public void setScan(AbstractScan scan) { this.scan = scan; }
    public String getTargetFile() { return targetFile; }
    public void setTargetFile(String targetFile) { this.targetFile = targetFile; }
    public Integer getTargetLine() { return targetLine; }
    public void setTargetLine(Integer targetLine) { this.targetLine = targetLine; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
