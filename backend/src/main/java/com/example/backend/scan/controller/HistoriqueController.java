package com.example.backend.scan.controller;

import com.example.backend.scan.entity.Historique;
import com.example.backend.scan.service.HistoriqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historiques")
@CrossOrigin(origins = "*")
@Tag(name = "Historique", description = "Consultation de l'historique et évolution des scans")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    public HistoriqueController(HistoriqueService historiqueService) {
        this.historiqueService = historiqueService;
    }

    @Operation(summary = "Historique d'un scan",
            description = "Récupère l'historique complet d'un scan : timestamps, nombre de vulnérabilités et sévérité maximale à chaque exécution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "404", description = "Aucun historique trouvé pour ce scan")
    })
    @GetMapping("/scan/{scanId}")
    public ResponseEntity<List<Historique>> getHistoriquesByScanId(
            @Parameter(description = "ID du scan", required = true, example = "1")
            @PathVariable Long scanId) {
        List<Historique> historiques = historiqueService.getHistoriqueByScanId(scanId);
        return ResponseEntity.ok(historiques);
    }
}
