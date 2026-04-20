package com.example.backend.scan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="scan_history")
public class Historique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private Integer nbre_vulnerabilites;
    
    @Enumerated(EnumType.STRING)
    private SeverityEnum nivmaxCri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
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
