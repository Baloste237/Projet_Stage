package com.example.backend.scan.controller;

import com.example.backend.scan.dto.stats.DashboardStatsDTO;
import com.example.backend.scan.dto.stats.StatsMobileDTO;
import com.example.backend.scan.dto.stats.StatsWebDTO;
import com.example.backend.scan.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposant les endpoints de statistiques OWASP.
 * Accessible uniquement aux rôles ADMIN et ANALYSTE_SECURITE.
 */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistiques", description = "Statistiques OWASP Web & Mobile calculées depuis la base de données")
public class StatsController {

    private static final Logger log = LoggerFactory.getLogger(StatsController.class);

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // ── Dashboard global ─────────────────────────────────────────────────

    @Operation(
        summary  = "Résumé global dashboard",
        description = "Retourne un résumé agrégé des statistiques web + mobile (totaux, critiques, derniers scans)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Résumé retourné"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<DashboardStatsDTO> getDashboard() {
        log.info("[Stats] GET /api/stats/dashboard");
        return ResponseEntity.ok(statsService.getDashboardStats());
    }

    // ── Statistiques Web ─────────────────────────────────────────────────

    @Operation(
        summary  = "Statistiques OWASP Web",
        description = "Retourne les statistiques détaillées des vulnérabilités web : totaux par sévérité, " +
                      "catégories OWASP (Injection, Auth, Access Control, Crypto, Error Handling), tendance mensuelle."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques web retournées"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/web")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<StatsWebDTO> getWebStats() {
        log.info("[Stats] GET /api/stats/web");
        return ResponseEntity.ok(statsService.getWebStats());
    }

    @Operation(
        summary  = "Statistiques OWASP Web (alias)",
        description = "Alias de /api/stats/web pour la compatibilité frontend."
    )
    @GetMapping("/owasp-web")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<StatsWebDTO> getOwaspWeb() {
        log.info("[Stats] GET /api/stats/owasp-web");
        return ResponseEntity.ok(statsService.getWebStats());
    }

    // ── Statistiques Mobile ──────────────────────────────────────────────

    @Operation(
        summary  = "Statistiques OWASP Mobile",
        description = "Retourne les statistiques détaillées des vulnérabilités mobiles : totaux, " +
                      "APKs analysés, score de risque moyen, catégories OWASP Mobile Top 10 2024, tendance mensuelle."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques mobile retournées"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/mobile")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<StatsMobileDTO> getMobileStats() {
        log.info("[Stats] GET /api/stats/mobile");
        return ResponseEntity.ok(statsService.getMobileStats());
    }

    @Operation(
        summary  = "Statistiques OWASP Mobile (alias)",
        description = "Alias de /api/stats/mobile pour la compatibilité frontend."
    )
    @GetMapping("/owasp-mobile")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<StatsMobileDTO> getOwaspMobile() {
        log.info("[Stats] GET /api/stats/owasp-mobile");
        return ResponseEntity.ok(statsService.getMobileStats());
    }
}
