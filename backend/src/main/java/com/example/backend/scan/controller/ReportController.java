package com.example.backend.scan.controller;

import com.example.backend.scan.service.AppScanService;
import com.example.backend.scan.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Rapports", description = "Génération et téléchargement de rapports PDF ou JSON")
public class ReportController {

    private final ReportService reportService;
    private final AppScanService appScanService;

    public ReportController(ReportService reportService, AppScanService appScanService) {
        this.reportService = reportService;
        this.appScanService = appScanService;
    }

    @Operation(summary = "Générer un rapport de scan", description = "Génère un rapport détaillé (PDF ou JSON) pour un scan. Permissions : ADMIN ou ANALYSTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "400", description = "Format non supporté"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Scan non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur de génération")
    })
    @GetMapping("/{scanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE_SECURITE')")
    public ResponseEntity<?> getReport(
            @Parameter(description = "ID du scan", required = true, example = "1") @PathVariable Long scanId,
            @Parameter(description = "Format: json ou pdf", schema = @Schema(allowableValues = { "json",
                    "pdf" }, defaultValue = "json")) @RequestParam(defaultValue = "json") String format) {
        try {
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = reportService.generateReportPdf(scanId);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "report_scan_" + scanId + ".pdf");
                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            } else if ("json".equalsIgnoreCase(format)) {
                String jsonBody = reportService.generateReportJson(scanId);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentDispositionFormData("attachment", "report_scan_" + scanId + ".json");
                return new ResponseEntity<>(jsonBody, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Format non supporté. Utilisez 'pdf' ou 'json'.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération du rapport : " + e.getMessage());
        }
    }

    @Operation(summary = "Supprimer un rapport/scan", description = "Supprime le scan et ses données. Réservé ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — ADMIN requis"),
            @ApiResponse(responseCode = "500", description = "Erreur de suppression")
    })
    @DeleteMapping("/{scanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReport(
            @Parameter(description = "ID du scan à supprimer", required = true, example = "1") @PathVariable Long scanId) {
        try {
            appScanService.deleteScan(scanId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur de suppression: " + e.getMessage());
        }
    }
}
