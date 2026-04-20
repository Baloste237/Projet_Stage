package com.example.backend.scan.controller;

import com.example.backend.scan.entity.Historique;
import com.example.backend.scan.service.HistoriqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historiques")
@CrossOrigin(origins = "*")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    public HistoriqueController(HistoriqueService historiqueService) {
        this.historiqueService = historiqueService;
    }

    @GetMapping("/scan/{scanId}")
    public ResponseEntity<List<Historique>> getHistoriquesByScanId(@PathVariable Long scanId) {
        List<Historique> historiques = historiqueService.getHistoriqueByScanId(scanId);
        return ResponseEntity.ok(historiques);
    }
}
