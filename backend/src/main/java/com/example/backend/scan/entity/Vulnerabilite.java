package com.example.backend.scan.entity;


import jakarta.persistence.*;

@Entity
@Table(name="vulnerabilites")
public class Vulnerabilite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;
    @Enumerated(EnumType.STRING)
    private SeverityEnum niv_grav;
    private String cweId;
    private Double cvssScore;
    private String owaspcat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SeverityEnum getNiv_grav() {
        return niv_grav;
    }

    public void setNiv_grav(SeverityEnum niv_grav) {
        this.niv_grav = niv_grav;
    }

    public String getCweId() {
        return cweId;
    }

    public void setCweId(String cweId) {
        this.cweId = cweId;
    }

    public Double getCvssScore() {
        return cvssScore;
    }

    public void setCvssScore(Double cvssScore) {
        this.cvssScore = cvssScore;
    }

    public String getOwaspcat() {
        return owaspcat;
    }

    public void setOwaspcat(String owaspcat) {
        this.owaspcat = owaspcat;
    }

    public AbstractScan getScan() {
        return scan;
    }

    public void setScan(AbstractScan scan) {
        this.scan = scan;
    }
}
