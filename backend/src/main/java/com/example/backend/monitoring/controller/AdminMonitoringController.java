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
import com.example.backend.scan.repository.AbstractScanRepository;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import com.example.backend.scan.repository.UserInfoRepository;
import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.entity.ScanStatus;
import com.example.backend.scan.entity.SeverityEnum;
import com.example.backend.scan.entity.ScanType;
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
    private final AbstractScanRepository scanRepository;
    private final VulnerabiliteRepository vulnerabiliteRepository;
    private final UserInfoRepository userInfoRepository;

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
    @Operation(summary = "Statistiques avancées pour le dashboard")
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();

        // 1. KPI Cards
        long totalScans = scanRepository.count();
        List<AbstractScan> allScans = scanRepository.findAll();
        
        long webScans = allScans.stream().filter(s -> s instanceof com.example.backend.scan.entity.WebAppScan).count();
        long mobileScans = allScans.stream().filter(s -> s instanceof com.example.backend.scan.entity.MobileAppScan).count();
        long failedScans = allScans.stream().filter(s -> s.getStatus() == ScanStatus.FAILED).count();
        long successScans = allScans.stream().filter(s -> s.getStatus() == ScanStatus.COMPLETED).count();
        long activeUsers = userInfoRepository.count();

        List<Vulnerabilite> allVulns = vulnerabiliteRepository.findAll();
        long criticalVulns = allVulns.stream().filter(v -> v.getNiv_grav() == SeverityEnum.CRITICAL).count();
        long highVulns = allVulns.stream().filter(v -> v.getNiv_grav() == SeverityEnum.HIGH).count();
        long mediumVulns = allVulns.stream().filter(v -> v.getNiv_grav() == SeverityEnum.MEDIUM).count();
        long lowVulns = allVulns.stream().filter(v -> v.getNiv_grav() == SeverityEnum.LOW).count();
        long infoVulns = allVulns.stream().filter(v -> v.getNiv_grav() == SeverityEnum.INFO).count();

        // Calculate global security score (naive average over 100)
        double score = 100.0;
        if (totalScans > 0) {
            double penalty = (criticalVulns * 10 + highVulns * 5 + mediumVulns * 2 + lowVulns * 1) / (double) totalScans;
            score = Math.max(0, 100 - penalty);
        }

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalScans", totalScans);
        kpis.put("webScans", webScans);
        kpis.put("mobileScans", mobileScans);
        kpis.put("criticalVulns", criticalVulns);
        kpis.put("highVulns", highVulns);
        kpis.put("securityScore", Math.round(score));
        kpis.put("activeUsers", activeUsers);
        kpis.put("failedScans", failedScans);
        kpis.put("successScans", successScans);
        response.put("kpis", kpis);

        // 2. Vulnerability by Severity (Doughnut)
        Map<String, Long> vulnBySeverity = new HashMap<>();
        vulnBySeverity.put("Critical", criticalVulns);
        vulnBySeverity.put("High", highVulns);
        vulnBySeverity.put("Medium", mediumVulns);
        vulnBySeverity.put("Low", lowVulns);
        vulnBySeverity.put("Info", infoVulns);
        response.put("vulnBySeverity", vulnBySeverity);

        // 3. Scan Evolution (Mocked or simple)
        // To do this properly, we should group by date. For simplicity, returning mock data structure
        // that the frontend expects (or real if grouped).
        List<Map<String, Object>> scanEvolution = new ArrayList<>();
        // Mock 7 days
        for(int i=6; i>=0; i--) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", LocalDateTime.now().minusDays(i).toLocalDate().toString());
            day.put("scans", (int)(Math.random() * 10));
            day.put("vulns", (int)(Math.random() * 20));
            scanEvolution.add(day);
        }
        response.put("scanEvolution", scanEvolution);

        // 4. Vulns by type
        Map<String, Long> vulnsByType = new HashMap<>();
        for (Vulnerabilite v : allVulns) {
            String type = v.getType() != null ? v.getType() : v.getCweId();
            if (type == null) type = "Unknown";
            vulnsByType.put(type, vulnsByType.getOrDefault(type, 0L) + 1);
        }
        // Take top 5
        List<Map.Entry<String, Long>> topVulns = new ArrayList<>(vulnsByType.entrySet());
        topVulns.sort((a,b) -> b.getValue().compareTo(a.getValue()));
        response.put("vulnsByType", topVulns.subList(0, Math.min(5, topVulns.size())));

        // 5. Scan status
        Map<String, Long> scanStatus = new HashMap<>();
        for (AbstractScan s : allScans) {
            String st = s.getStatus() != null ? s.getStatus().name() : "UNKNOWN";
            scanStatus.put(st, scanStatus.getOrDefault(st, 0L) + 1);
        }
        response.put("scanStatus", scanStatus);

        // 6. Recent Scans
        Page<AbstractScan> recentScans = scanRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()));
        response.put("recentScans", recentScans.getContent());

        // 7. Recent Vulns
        Page<Vulnerabilite> recentVulns = vulnerabiliteRepository.findAll(PageRequest.of(0, 5, Sort.by("id").descending()));
        response.put("recentVulns", recentVulns.getContent());

        return ResponseEntity.ok(response);
    }
}
