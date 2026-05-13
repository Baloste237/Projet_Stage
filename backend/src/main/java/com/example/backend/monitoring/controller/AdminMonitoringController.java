package com.example.backend.monitoring.controller;

import com.example.backend.monitoring.entity.AuditLog;
import com.example.backend.monitoring.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Monitoring & Logs", description = "Journaux d'audit, métriques système et monitoring — ROLE_ADMIN")
public class AdminMonitoringController {

    private final AuditLogRepository auditLogRepository;

    @Operation(summary = "Consulter les logs d'audit (paginé)",
            description = "Recherche paginée des journaux d'audit avec filtres optionnels par utilisateur, action, statut HTTP et niveau de log.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page de logs récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — ADMIN requis")
    })
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getLogs(
            @Parameter(description = "Filtrer par ID/email utilisateur") @RequestParam(required = false) String userId,
            @Parameter(description = "Filtrer par action (ex: LOGIN_SUCCESS, SCAN_APK)") @RequestParam(required = false) String action,
            @Parameter(description = "Filtrer par code statut HTTP", example = "200") @RequestParam(required = false) Integer status,
            @Parameter(description = "Filtrer par niveau de log (INFO, WARN, ERROR)") @RequestParam(required = false) String logLevel,
            @Parameter(description = "Numéro de page", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null && !userId.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("userId")), "%" + userId.toLowerCase() + "%"));
            }
            if (action != null && !action.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("action")), "%" + action.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (logLevel != null && !logLevel.isEmpty()) {
                predicates.add(cb.equal(root.get("logLevel"), logLevel));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ResponseEntity.ok(auditLogRepository.findAll(spec, pageable));
    }

    @Operation(summary = "Statistiques de monitoring",
            description = "Retourne les métriques système : nombre total de logs, erreurs, utilisateurs actifs et scans en cours.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"totalLogs\": 1250, \"errorCount\": 42, \"activeUsers\": 3, \"scansInProgress\": 1}"))),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — ADMIN requis")
    })
    @GetMapping("/monitoring")
    public ResponseEntity<Map<String, Object>> getMonitoringStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalLogs = auditLogRepository.count();
        long errors = auditLogRepository.countByStatusGreaterThanEqual(400);
        
        // Count scans in progress (mock logic or querying specific logs)
        // For simplicity, we just return basic stats here. Real active scans could be tracked in a ConcurrentHashMap in a service.
        stats.put("totalLogs", totalLogs);
        stats.put("errorCount", errors);
        stats.put("activeUsers", 1); // Mocked or calculated
        stats.put("scansInProgress", 0); // Mocked or calculated
        
        return ResponseEntity.ok(stats);
    }
}
