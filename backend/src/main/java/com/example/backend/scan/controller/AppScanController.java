package com.example.backend.scan.controller;

import com.example.backend.scan.dto.AppScanResponseDTO;
import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.service.AppScanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST Controller for scan operations.
 */
@RestController
@RequestMapping("/api/v1/analyze")
public class AppScanController {

    private static final Logger log = LoggerFactory.getLogger(AppScanController.class);

    private final AppScanService appScanService;

    public AppScanController(AppScanService appScanService) {
        this.appScanService = appScanService;
    }

    @GetMapping("/dashboard")

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



    @PostMapping(value = "/{appType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE')")
    public ResponseEntity<AppScanResponseDTO> analyzeCode(
            @PathVariable("appType") String appType,
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectName") String projectName) {

        log.info("Received request to analyze {} code for project: {}", appType, projectName);
        AppScanResponseDTO response = appScanService.processScan(appType, file, projectName);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to retrieve all scans.
     *
     * @return list of all scans
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE')")
    public ResponseEntity<List<AbstractScan>> getAllScans() {
        log.info("Received request to retrieve all scans");
        return ResponseEntity.ok(appScanService.getAllScans());
    }

    /**
     * Endpoint to retrieve a specific scan by its ID.
     *
     * @param id the ID of the scan
     * @return the scan entity
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYSTE')")
    public ResponseEntity<AbstractScan> getScanById(@PathVariable("id") Long id) {
        log.info("Received request to retrieve scan with ID: {}", id);
        return ResponseEntity.ok(appScanService.getScanById(id));
    }

    /**
     * Endpoint to delete a specific scan by its ID.
     *
     * @param id the ID of the scan to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScan(@PathVariable("id") Long id) {
        log.info("Received request to delete scan with ID: {}", id);
        appScanService.deleteScan(id);
        return ResponseEntity.noContent().build();
    }
}