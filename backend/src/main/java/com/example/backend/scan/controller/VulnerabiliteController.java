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
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/vulnerabilities") // Mise à jour pour respecter REST api/v1
@CrossOrigin(origins = "*")
@Tag(name = "Vulnérabilités", description = "Consultation, filtrage avancé et suppression des vulnérabilités détectées")
@Transactional(readOnly = true) // Nécessaire pour éviter "Unable to access lob stream" de Postgres pendant la sérialisation JSON des champs @Lob
public class VulnerabiliteController {

    private final VulnerabiliteService vulnerabiliteService;
    private final VulnerabiliteRepository vulnerabiliteRepository; // Added for native query

    public VulnerabiliteController(VulnerabiliteService vulnerabiliteService, VulnerabiliteRepository vulnerabiliteRepository) {
        this.vulnerabiliteService = vulnerabiliteService;
        this.vulnerabiliteRepository = vulnerabiliteRepository;
    }

    public static class ScanDTO {
        public Long id;
        public String projectName;
        public ScanDTO(Long id, String projectName) {
            this.id = id;
            this.projectName = projectName;
        }
    }

    public static class VulnDTO {
        public Long id;
        public String type;
        public String description;
        public SeverityEnum niv_grav;
        public String cweId;
        public Double cvssScore;
        public String owaspcat;
        public String targetFile;
        public Integer targetLine;
        public String status;
        public String recommendation;
        public ScanDTO scan;
        
        public VulnDTO(Vulnerabilite v) {
            this.id = v.getId();
            this.type = v.getType();
            this.description = v.getDescription();
            this.niv_grav = v.getNiv_grav();
            this.cweId = v.getCweId();
            this.cvssScore = v.getCvssScore();
            this.owaspcat = v.getOwaspcat();
            this.targetFile = v.getTargetFile();
            this.targetLine = v.getTargetLine();
            this.status = v.getStatus();
            this.recommendation = v.getRecommendation();
            if (v.getScan() != null) {
                this.scan = new ScanDTO(v.getScan().getId(), v.getScan().getProjectName());
            }
        }
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
    public ResponseEntity<List<VulnDTO>> getVulnerabilitesByScanId(
            @Parameter(description = "ID du scan dont on veut récupérer les vulnérabilités", required = true, example = "1")
            @PathVariable Long scanId) {
        List<Vulnerabilite> vulnerabilites = vulnerabiliteService.getVulnerabilitesByScanId(scanId);
        List<VulnDTO> dtos = vulnerabilites.stream().map(VulnDTO::new).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
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
    public ResponseEntity<Page<VulnDTO>> getFilteredVulnerabilities(
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
        Page<VulnDTO> dtoPage = resultPage.map(VulnDTO::new);
        return ResponseEntity.ok(dtoPage);
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
    @Transactional // Permet la suppression en transaction
    public ResponseEntity<Void> deleteVulnerabilite(
            @Parameter(description = "ID de la vulnérabilité à supprimer", required = true, example = "1")
            @PathVariable Long id) {
        vulnerabiliteService.deleteVulnerabilite(id);
        return ResponseEntity.ok().build();
    }
}
