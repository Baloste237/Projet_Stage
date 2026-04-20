package com.example.backend.scan.controller;

import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.service.VulnerabiliteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vulnerabilites")
@CrossOrigin(origins = "*")
public class VulnerabiliteController {

    private final VulnerabiliteService vulnerabiliteService;

    public VulnerabiliteController(VulnerabiliteService vulnerabiliteService) {
        this.vulnerabiliteService = vulnerabiliteService;
    }

    @GetMapping("/scan/{scanId}")
    public ResponseEntity<List<Vulnerabilite>> getVulnerabilitesByScanId(@PathVariable Long scanId) {
        List<Vulnerabilite> vulnerabilites = vulnerabiliteService.getVulnerabilitesByScanId(scanId);
        return ResponseEntity.ok(vulnerabilites);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVulnerabilite(@PathVariable Long id) {
        vulnerabiliteService.deleteVulnerabilite(id);
        return ResponseEntity.ok().build();
    }
}
