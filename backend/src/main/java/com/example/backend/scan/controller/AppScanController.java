package com.example.backend.scan.controller;

import com.example.backend.scan.dto.AppScanResponseDTO;
import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.service.AppScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * REST Controller for scan operations.
 */
@RestController
@RequestMapping("/api/v1/analyze")
@Tag(name = "Scan Sécurité", description = "Lancement et gestion des analyses SAST pour applications web et mobiles")
public class AppScanController {

    private static final Logger log = LoggerFactory.getLogger(AppScanController.class);

    private final AppScanService appScanService;

    public AppScanController(AppScanService appScanService) {
        this.appScanService = appScanService;
    }

    @Operation(
            summary = "Page d'accueil du dashboard",
            description = "Endpoint public de bienvenue pour le tableau de bord. Aucune authentification requise."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message de bienvenue retourné avec succès",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "\"welcome dashboard\"")))
    })
    @GetMapping("/dashboard")
    @Tag(name = "Dashboard")
    public ResponseEntity<String> dashboardmsg(){
        return new ResponseEntity<>("welcome dashboard", HttpStatus.OK);
    }

    /**
     * Endpoint to submit a ZIP file for SAST analysis.
     * 
     * @param appType     the application type (web, mobile)
     * @param file        the ZIP file to analyze
     * @param projectName the name of the project
     * @return the scan response
     */

    @Operation(
            summary = "Lancer une analyse SAST",
            description = """
                    Soumet un fichier pour analyse de sécurité statique (SAST).
                    
                    ### Types d'applications supportés
                    | Type | Fichiers acceptés | Moteur d'analyse |
                    |------|-------------------|------------------|
                    | `web` | Archive ZIP contenant le code source | Moteur SAST Python (IA/ML) |
                    | `mobile` | Fichier APK (Android Package) | MobSF (Mobile Security Framework) |
                    
                    ### Processus d'analyse
                    1. Upload du fichier sur le serveur
                    2. Envoi au moteur d'analyse approprié
                    3. Extraction et classification des vulnérabilités (CWE, CVSS)
                    4. Sauvegarde des résultats en base de données
                    
                    **Taille maximale** : 100 MB  
                    **Permissions** : ROLE_ADMIN ou ROLE_ANALYSTE_SECURITE
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analyse terminée avec succès — Résultats retournés",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AppScanResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Fichier invalide, type non supporté ou fichier vide",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Scan Error\", \"message\": \"Type d'application non supporté: xyz\"}"))),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN ou ANALYSTE requis"),
            @ApiResponse(responseCode = "500", description = "Erreur interne lors de l'analyse",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred.\"}")))
    })
    @PostMapping(value = "/{appType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<AppScanResponseDTO> analyzeCode(
            @Parameter(description = "Type d'application à analyser", required = true,
                    schema = @Schema(allowableValues = {"web", "mobile"}), example = "web")
            @PathVariable("appType") String appType,
            @Parameter(description = "Fichier à analyser (ZIP pour web, APK pour mobile)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nom du projet associé au scan", required = true, example = "MonProjetWeb")
            @RequestParam("projectName") String projectName) {

        log.info("Received request to analyze {} code for project: {}", appType, projectName);
        AppScanResponseDTO response = appScanService.processScan(appType, file, projectName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/running")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<List<AbstractScan>> getRunningScans() {
        // Here we just filter the all scans for RUNNING or PENDING. A real impl would query the DB.
        // I will do it here by fetching all and filtering, or we can use a custom query later.
        Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
        List<AbstractScan> running = appScanService.getAllScans(pageable).stream()
                .filter(s -> s.getStatus() == com.example.backend.scan.entity.ScanStatus.RUNNING || s.getStatus() == com.example.backend.scan.entity.ScanStatus.PENDING)
                .toList();
        return ResponseEntity.ok(running);
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<AbstractScan> getScanProgress(@PathVariable Long id) {
        return ResponseEntity.ok(appScanService.getScanById(id));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<String> getScanStatus(@PathVariable Long id) {
        return ResponseEntity.ok(appScanService.getScanById(id).getStatus().name());
    }


    /**
     * Endpoint to retrieve all scans.
     *
     * @return list of all scans
     */
    @Operation(
            summary = "Lister tous les scans (paginé)",
            description = """
                    Récupère la liste paginée de tous les scans effectués, triés par date de création décroissante.
                    
                    **Permissions** : ROLE_ADMIN ou ROLE_ANALYSTE_SECURITE
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page de scans récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN ou ANALYSTE requis")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<Page<AbstractScan>> getAllScans(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        log.info("Received request to retrieve all scans - Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(appScanService.getAllScans(pageable));
    }

    /**
     * Endpoint to retrieve a specific scan by its ID.
     *
     * @param id the ID of the scan
     * @return the scan entity
     */
    @Operation(
            summary = "Détail d'un scan par ID",
            description = "Récupère les informations complètes d'un scan spécifique identifié par son ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scan trouvé et retourné avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN ou ANALYSTE requis"),
            @ApiResponse(responseCode = "404", description = "Scan non trouvé avec l'ID spécifié")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<AbstractScan> getScanById(
            @Parameter(description = "ID unique du scan", required = true, example = "1")
            @PathVariable("id") Long id) {
        log.info("Received request to retrieve scan with ID: {}", id);
        return ResponseEntity.ok(appScanService.getScanById(id));
    }

    /**
     * Endpoint to delete a specific scan by its ID.
     *
     * @param id the ID of the scan to delete
     * @return 204 No Content
     */
    @Operation(
            summary = "Supprimer un scan",
            description = "Supprime définitivement un scan et toutes ses vulnérabilités associées. **Réservé aux administrateurs**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Scan supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié — Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — Rôle ADMIN requis"),
            @ApiResponse(responseCode = "404", description = "Scan non trouvé avec l'ID spécifié")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScan(
            @Parameter(description = "ID unique du scan à supprimer", required = true, example = "1")
            @PathVariable("id") Long id) {
        log.info("Received request to delete scan with ID: {}", id);
        appScanService.deleteScan(id);
        return ResponseEntity.noContent().build();
    }
}