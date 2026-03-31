package com.example.backend.scan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entity representing a Web Application scan.
 */
@Entity
@Table(name = "web_app_scans")
public class WebAppScan extends AbstractScan {

    private String frontendFramework;

    public WebAppScan() {
        super();
    }

    public String getFrontendFramework() { return frontendFramework; }
    public void setFrontendFramework(String frontendFramework) { this.frontendFramework = frontendFramework; }
}
