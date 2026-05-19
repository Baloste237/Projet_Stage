package com.example.backend.scan.controller;

import com.example.backend.scan.entity.SeverityEnum;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import com.example.backend.scan.service.VulnerabiliteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api/v1/vulnerabilities") // Mise à jour pour respecter REST api/v1
@CrossOrigin(origins = "*")
@Tag(name = "Vulnérabilités", description = "Consultation, filtrage avancé et suppression des vulnérabilités détectées")
public class VulnerabiliteController {

    private final VulnerabiliteService vulnerabiliteService;
    private final VulnerabiliteRepository vulnerabiliteRepository; // Added for native query

    public VulnerabiliteController(VulnerabiliteService vulnerabiliteService, VulnerabiliteRepository vulnerabiliteRepository) {
        this.vulnerabiliteService = vulnerabiliteService;
        this.vulnerabiliteRepository = vulnerabiliteRepository;
    }

    @Operation(
            summary = "Vulnérabilités d'un scan spécifique",
            description = "Récupère la liste complète des vulnérabilités détectées pour un scan donné, identifié par son ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des vulnérabilités retournée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN ou ANALYSTE requis"),
            @ApiResponse(responseCode = "404", description = "Scan non trouvé avec l'ID spécifié")
    })
    @GetMapping("/scan/{scanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<List<Vulnerabilite>> getVulnerabilitesByScanId(
            @Parameter(description = "ID du scan dont on veut récupérer les vulnérabilités", required = true, example = "1")
            @PathVariable Long scanId) {
        List<Vulnerabilite> vulnerabilites = vulnerabiliteService.getVulnerabilitesByScanId(scanId);
        return ResponseEntity.ok(vulnerabilites);
    }
    
    @Operation(
            summary = "Recherche avancée de vulnérabilités (paginée)",
            description = """
                    Recherche paginée des vulnérabilités avec filtres multiples.
                    Tous les filtres sont optionnels et combinables.
                    
                    ### Filtres disponibles
                    | Filtre | Description | Exemple |
                    |--------|-------------|---------|
                    | `scanId` | ID du scan parent | `1` |
                    | `severity` | Niveau de sévérité | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO` |
                    | `cwe` | Identifiant CWE (recherche partielle) | `CWE-79` |
                    | `cvss` | Score CVSS minimum | `7.5` |
                    
                    Les résultats sont triés par ID décroissant (les plus récents en premier).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page de vulnérabilités retournée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN ou ANALYSTE requis")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<Page<Vulnerabilite>> getFilteredVulnerabilities(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filtrer par ID de scan", example = "1")
            @RequestParam(required = false) Long scanId,
            @Parameter(description = "Filtrer par sévérité", example = "HIGH",
                    schema = @Schema(allowableValues = {"CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO", "all"}))
            @RequestParam(required = false) String severity,
            @Parameter(description = "Filtrer par identifiant CWE (recherche partielle)", example = "CWE-79")
            @RequestParam(required = false) String cwe,
            @Parameter(description = "Score CVSS minimum", example = "7.5")
            @RequestParam(required = false) Double cvss
    ) {
        SeverityEnum sevEnum = null;
        if (severity != null && !severity.equalsIgnoreCase("all")) {
            try {
                sevEnum = SeverityEnum.valueOf(severity.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        final SeverityEnum finalSevEnum = sevEnum;
        Specification<Vulnerabilite> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (scanId != null) {
                predicates.add(cb.equal(root.get("scan").get("id"), scanId));
            }
            if (finalSevEnum != null) {
                predicates.add(cb.equal(root.get("niv_grav"), finalSevEnum));
            }
            if (cwe != null && !cwe.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("cweId")), "%" + cwe.toLowerCase() + "%"));
            }
            if (cvss != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cvssScore"), cvss));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Vulnerabilite> resultPage = vulnerabiliteRepository.findAll(spec, pageable);
        return ResponseEntity.ok(resultPage);
    }

    @Operation(
            summary = "Supprimer une vulnérabilité",
            description = "Supprime définitivement une vulnérabilité identifiée par son ID. **Réservé aux administrateurs**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vulnérabilité supprimée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Vulnérabilité non trouvée avec l'ID spécifié")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVulnerabilite(
            @Parameter(description = "ID unique de la vulnérabilité", required = true, example = "42")
            @PathVariable Long id) {
        vulnerabiliteService.deleteVulnerabilite(id);
        return ResponseEntity.ok().build();
    }
}
