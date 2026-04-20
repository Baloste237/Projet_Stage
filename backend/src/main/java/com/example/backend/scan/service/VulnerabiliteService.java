package com.example.backend.scan.service;

import com.example.backend.scan.entity.Vulnerabilite;
import java.util.List;

public interface VulnerabiliteService {
    Vulnerabilite saveVulnerabilite(Vulnerabilite vulnerabilite);
    List<Vulnerabilite> saveAllVulnerabilites(List<Vulnerabilite> vulnerabilites);
    List<Vulnerabilite> getVulnerabilitesByScanId(Long scanId);
    void deleteVulnerabilite(Long id);
}
