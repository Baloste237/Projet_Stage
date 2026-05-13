package com.example.backend.scan.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="scan_history")
@Schema(description = "Historique d'un scan — capture l'état des vulnérabilités à un instant donné")
public class Historique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unique de l'entrée d'historique", example = "1")
    private Long id;

    @Schema(description = "Date et heure de l'enregistrement", example = "2026-05-12T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Nombre de vulnérabilités à cet instant", example = "12")
    private Integer nbre_vulnerabilites;
    
    @Enumerated(EnumType.STRING)
    @Schema(description = "Sévérité maximale détectée", example = "CRITICAL")
    private SeverityEnum nivmaxCri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    @Schema(description = "Scan associé à cet historique")
    private AbstractScan scan;

    public Historique() {}

    public Historique(Long id, LocalDateTime timestamp, Integer nbre_vulnerabilites, SeverityEnum nivmaxCri, AbstractScan scan) {
        this.id = id;
        this.timestamp = timestamp;
        this.nbre_vulnerabilites = nbre_vulnerabilites;
        this.nivmaxCri = nivmaxCri;
        this.scan = scan;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getNbre_vulnerabilites() { return nbre_vulnerabilites; }
    public void setNbre_vulnerabilites(Integer nbre_vulnerabilites) { this.nbre_vulnerabilites = nbre_vulnerabilites; }
    public SeverityEnum getNivmaxCri() { return nivmaxCri; }
    public void setNivmaxCri(SeverityEnum nivmaxCri) { this.nivmaxCri = nivmaxCri; }
    public AbstractScan getScan() { return scan; }
    public void setScan(AbstractScan scan) { this.scan = scan; }
}
