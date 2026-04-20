package com.example.backend.scan.service;

import com.example.backend.scan.dto.AppScanResponseDTO;
import com.example.backend.scan.dto.MobSFScanResponse;
import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.ScanStatus;
import com.example.backend.scan.entity.ScanType;
import com.example.backend.scan.exception.ScanException;
import com.example.backend.scan.repository.AbstractScanRepository;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.entity.Historique;
import com.example.backend.scan.entity.WebAppScan;
import com.example.backend.scan.entity.SeverityEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implementation of AppScanService orchestrating different scan types.
 */
@Service

public class AppScanServiceImpl implements AppScanService {

    private static final Logger log = LoggerFactory.getLogger(AppScanServiceImpl.class);
    private final AbstractScanRepository abstractScanRepository;
    private final List<ScanProcessor> processors;
    private final VulnerabiliteService vulnerabiliteService;
    private final HistoriqueService historiqueService;
    private final MobSFClient mobsfClient;

    public AppScanServiceImpl(AbstractScanRepository abstractScanRepository, 
                              List<ScanProcessor> processors,
                              VulnerabiliteService vulnerabiliteService,
                              HistoriqueService historiqueService,
                              MobSFClient mobsfClient) {
        this.abstractScanRepository = abstractScanRepository;
        this.processors = processors;
        this.vulnerabiliteService = vulnerabiliteService;
        this.historiqueService = historiqueService;
        this.mobsfClient = mobsfClient;
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${sast.engine.url:http://localhost:8000/api/v1/analyze}")
    private String sastEngineUrl;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Override
    public AbstractScan createScan(AbstractScan scan) {
        log.info("Creating new scan for project: {}", scan.getProjectName());
        scan.setCreatedAt(LocalDateTime.now());
        return abstractScanRepository.save(scan);
    }

    @Override
    public AbstractScan getScanById(Long id) {
        log.info("Retrieving scan with ID: {}", id);
        return abstractScanRepository.findById(id)
                .orElseThrow(() -> new ScanException("Scan not found with ID: " + id));
    }

    @Override
    public List<AbstractScan> getAllScans() {
        log.info("Retrieving all scans");
        return abstractScanRepository.findAll();
    }

    @Override
    public void deleteScan(Long id) {
        log.info("Deleting scan with ID: {}", id);
        if (!abstractScanRepository.existsById(id)) {
            throw new ScanException("Scan not found with ID: " + id);
        }
        abstractScanRepository.deleteById(id);
    }

    @Override
    public AppScanResponseDTO processScan(String appType, MultipartFile file, String projectName) {
        log.info("Processing {} scan for project: {}", appType, projectName);

        // Find processor
        ScanProcessor processor = processors.stream()
                .filter(p -> p.supports(appType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Type d'application non supporté : " + appType));

        // Validate file
        validateFile(file);

        // Save file
        String filePath = saveFile(file);

        // Create specific scan entity
        AbstractScan scan = processor.createEmptyScanEntity();
        scan.setProjectName(projectName);
        scan.setFileName(file.getOriginalFilename());
        scan.setFilePath(filePath);
        scan.setScanType(ScanType.SAST);
        scan.setStatus(ScanStatus.PENDING);
        scan.setTotalVulnerabilities(0);
        scan.setCriticalCount(0);
        scan.setHighCount(0);
        scan.setMediumCount(0);
        scan.setLowCount(0);
        scan.setCreatedAt(LocalDateTime.now());

        scan = createScan(scan);

        // Execute scan processing with IA
        executeSastScan(scan);

        // Convert to DTO
        return mapToResponseDTO(scan);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ScanException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ScanException("File size exceeds maximum allowed size of 100MB");
        }
        if (!isValidFile(file)) {
            throw new ScanException("Only ZIP or APK files are allowed");
        }
    }

    private boolean isValidFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".zip") || lower.endsWith(".apk") || lower.endsWith(".ipa");
    }

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved to: {}", filePath.toString());
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage(), e);
            throw new ScanException("Failed to save file", e);
        }
    }

    private void executeSastScan(AbstractScan scan) {
        try {
            scan.setStatus(ScanStatus.PROCESSING);
            abstractScanRepository.save(scan);
            log.info("Scan status updated to PROCESSING for ID: {}", scan.getId());

            if (scan instanceof WebAppScan) {
                WebAppScan webScan = (WebAppScan) scan;
                java.util.concurrent.atomic.AtomicInteger totalVulnerabilities = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger critCount = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger highCount = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger medCount = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger lowCount = new java.util.concurrent.atomic.AtomicInteger(0);

                RestTemplate restTemplate = new RestTemplate();
                Path zipPath = Paths.get(scan.getFilePath());
                
                Path tempExtractionDir = Files.createTempDirectory("sast_scan_" + scan.getId() + "_");
                try {
                    FileExtractionUtil.extractZipSafely(zipPath, tempExtractionDir);
                    
                    try (java.util.stream.Stream<Path> pathStream = Files.walk(tempExtractionDir)) {
                        pathStream.filter(Files::isRegularFile)
                            .filter(p -> {
                                String strPath = p.toString().replace("\\", "/");
                                return !strPath.contains("/node_modules/") 
                                    && !strPath.contains("/.git/") 
                                    && !strPath.contains("/dist/") 
                                    && !strPath.contains("/build/");
                            })
                            .forEach(filePath -> {
                                String relativePath = tempExtractionDir.relativize(filePath).toString().replace("\\", "/");
                                String lowerPath = relativePath.toLowerCase();

                                // 1. Scan via Regex local
                                if (lowerPath.endsWith(".java") || lowerPath.endsWith(".js") || lowerPath.endsWith(".ts") 
                                     || lowerPath.endsWith(".py") || lowerPath.endsWith(".php") || lowerPath.endsWith(".html") 
                                     || lowerPath.endsWith(".xml") || lowerPath.endsWith(".json")) {
                                    
                                    List<VulnerabilityDetector.Finding> localFindings = VulnerabilityDetector.scanFile(filePath, relativePath);
                                    for (VulnerabilityDetector.Finding finding : localFindings) {
                                        Vulnerabilite vuln = new Vulnerabilite(null, finding.cweId, finding.description, finding.severity, finding.cweId, finding.adjustedScore, finding.ruleName, webScan);
                                        vulnerabiliteService.saveVulnerabilite(vuln);
                                        
                                        totalVulnerabilities.incrementAndGet();
                                        switch(finding.severity) {
                                            case CRITICAL: critCount.incrementAndGet(); break;
                                            case HIGH: highCount.incrementAndGet(); break;
                                            case MEDIUM: medCount.incrementAndGet(); break;
                                            case LOW: lowCount.incrementAndGet(); break;
                                            default: break;
                                        }
                                    }
                                }

                                // 2. Scan via IA (seulement pour .java pour garder l'existant)
                                if (lowerPath.endsWith(".java")) {
                                    try {
                                        String code = new String(Files.readAllBytes(filePath), java.nio.charset.StandardCharsets.UTF_8);
                                        com.example.backend.scan.dto.ia.IaAnalysisRequestDTO req = new com.example.backend.scan.dto.ia.IaAnalysisRequestDTO(code, relativePath);
                                        com.example.backend.scan.dto.ia.IaAnalysisResponseDTO res = restTemplate.postForObject(sastEngineUrl, req, com.example.backend.scan.dto.ia.IaAnalysisResponseDTO.class);
                                        
                                        if (res != null && res.isVulnerable() && res.getCvssScores() != null) {
                                            for (com.example.backend.scan.dto.ia.IaAnalysisResponseDTO.IaCvssResult cvss : res.getCvssScores()) {
                                                SeverityEnum sevEnum = mapSeverity(cvss.getSeverity());
                                                Vulnerabilite vuln = new Vulnerabilite(null, cvss.getCweId(), cvss.getRationale() + " (File: " + relativePath + ")", sevEnum, cvss.getCweId(), cvss.getAdjustedScore(), "IA Detection", webScan);
                                                vulnerabiliteService.saveVulnerabilite(vuln);
                                                
                                                totalVulnerabilities.incrementAndGet();
                                                switch(sevEnum) {
                                                    case CRITICAL: critCount.incrementAndGet(); break;
                                                    case HIGH: highCount.incrementAndGet(); break;
                                                    case MEDIUM: medCount.incrementAndGet(); break;
                                                    case LOW: lowCount.incrementAndGet(); break;
                                                    default: break;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("Error analyzing file {} with IA engine", relativePath, e);
                                    }
                                }
                            });
                    }
                } finally {
                    FileExtractionUtil.deleteDirectory(tempExtractionDir.toFile());
                }

                scan.setStatus(ScanStatus.DONE);
                scan.setTotalVulnerabilities(totalVulnerabilities.get());
                scan.setCriticalCount(critCount.get());
                scan.setHighCount(highCount.get());
                scan.setMediumCount(medCount.get());
                scan.setLowCount(lowCount.get());
                scan.setCompletedAt(LocalDateTime.now());
                abstractScanRepository.save(scan);

                SeverityEnum maxSev = calculateMaxSeverity(critCount.get(), highCount.get(), medCount.get(), lowCount.get());
                Historique historique = new Historique(null, LocalDateTime.now(), webScan.getTotalVulnerabilities(), maxSev, webScan);
                historiqueService.saveHistorique(historique);
                log.info("Persisted vulnerabilities and history for WebAppScan ID: {}", scan.getId());
            } else if (scan instanceof com.example.backend.scan.entity.MobileAppScan) {
                com.example.backend.scan.entity.MobileAppScan mobileScan = (com.example.backend.scan.entity.MobileAppScan) scan;
                int totalVulnerabilities = 0;
                int critCount = 0;
                int highCount = 0;
                int medCount = 0;
                int lowCount = 0;

                try {
                    // Upload
                    java.io.File apkFile = new java.io.File(scan.getFilePath());
                    com.example.backend.scan.dto.MobSFUploadResponse uploadResp = mobsfClient.uploadApk(apkFile);
                    
                    mobileScan.setProjectName(uploadResp.getPackageName() != null ? uploadResp.getPackageName() : mobileScan.getProjectName());
                    mobileScan.setTargetOs("Android");
                    
                    // Lancer Analyse
                    com.example.backend.scan.dto.MobSFScanResponse scanResp = mobsfClient.lancerAnalyse(uploadResp.getHash());
                    
                    // Parcourir Vulns Code Analysis
                    if (scanResp.getCodeAnalysis() != null && scanResp.getCodeAnalysis().getFindings() != null) {
                        for (java.util.Map.Entry<String, MobSFScanResponse.CodeAnalysis.Finding> entry : scanResp.getCodeAnalysis().getFindings().entrySet()) {
                            String key = entry.getKey();
                            MobSFScanResponse.CodeAnalysis.Finding finding = entry.getValue();
                            
                            SeverityEnum sevEnum = mapSeverity(finding.getLevel());
                            if (sevEnum != SeverityEnum.INFO) { 
                                Vulnerabilite v = new Vulnerabilite(null, finding.getCwe() != null ? finding.getCwe() : "CWE-unknown", finding.getDescription() + " (Files: " + (finding.getFiles() != null ? String.join(", ", finding.getFiles()) : "bytecode") + ")", sevEnum, finding.getCwe() != null ? finding.getCwe() : "CWE-unknown", 5.0, key, mobileScan);
                                vulnerabiliteService.saveVulnerabilite(v);
                                
                                totalVulnerabilities++;
                                switch(sevEnum) {
                                    case CRITICAL: critCount++; break;
                                    case HIGH: highCount++; break;
                                    case MEDIUM: medCount++; break;
                                    case LOW: lowCount++; break;
                                    default: break;
                                }
                            }
                        }
                    }
                    
                    // Parcourir Manifest Analysis dynamique
                    if (scanResp.getManifestAnalysis() != null) {
                        try {
                            if (scanResp.getManifestAnalysis() instanceof java.util.List) {
                                java.util.List<?> rawList = (java.util.List<?>) scanResp.getManifestAnalysis();
                                com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                for (Object obj : rawList) {
                                    MobSFScanResponse.ManifestIssue issue = objMapper.convertValue(obj, MobSFScanResponse.ManifestIssue.class);
                                    SeverityEnum sevEnum = mapSeverity(issue.getSeverity());
                                    if (sevEnum != SeverityEnum.INFO) {
                                        Vulnerabilite v = new Vulnerabilite(null, issue.getCwe() != null ? issue.getCwe() : "CWE-unknown", issue.getDescription() + " (Manifest)", sevEnum, issue.getCwe() != null ? issue.getCwe() : "CWE-unknown", 5.0, issue.getTitle(), mobileScan);
                                        vulnerabiliteService.saveVulnerabilite(v);
                                        
                                        totalVulnerabilities++;
                                        switch(sevEnum) {
                                            case CRITICAL: critCount++; break;
                                            case HIGH: highCount++; break;
                                            case MEDIUM: medCount++; break;
                                            case LOW: lowCount++; break;
                                            default: break;
                                        }
                                    }
                                }
                            } else if (scanResp.getManifestAnalysis() instanceof java.util.Map) {
                                // Parfois MobSF renvoie un objet associatif vide ou avec structure inattendue
                                log.warn("[MobSF] manifestAnalysis a renvoyé un objet Map, ignoré ou parsé différemment.");
                            }
                        } catch (Exception parseEx) {
                            log.error("Erreur parsing ManifestAnalysis ignorée : {}", parseEx.getMessage());
                        }
                    }

                    // Parcourir Permissions Analysis
                    if (scanResp.getPermissions() != null) {
                        for (java.util.Map.Entry<String, com.example.backend.scan.dto.MobSFScanResponse.PermissionDetail> entry : scanResp.getPermissions().entrySet()) {
                            if ("dangerous".equalsIgnoreCase(entry.getValue().getStatus())) {
                                Vulnerabilite v = new Vulnerabilite(null, "CWE-276", "Permission Android Dangereuse: " + entry.getKey() + " - " + entry.getValue().getDescription(), SeverityEnum.LOW, "CWE-276", 2.0, "Permission Insecure", mobileScan);
                                vulnerabiliteService.saveVulnerabilite(v);
                                
                                totalVulnerabilities++;
                                lowCount++;
                            }
                        }
                    }

                } catch(Exception e) {
                    log.error("MobSF analysis failed for scan ID {}", scan.getId(), e);
                    throw e;
                }
                
                scan.setStatus(ScanStatus.DONE);
                scan.setTotalVulnerabilities(totalVulnerabilities);
                scan.setCriticalCount(critCount);
                scan.setHighCount(highCount);
                scan.setMediumCount(medCount);
                scan.setLowCount(lowCount);
                scan.setCompletedAt(LocalDateTime.now());
                abstractScanRepository.save(scan);

                SeverityEnum maxSev = calculateMaxSeverity(critCount, highCount, medCount, lowCount);
                Historique historique = new Historique(null, LocalDateTime.now(), mobileScan.getTotalVulnerabilities(), maxSev, mobileScan);
                historiqueService.saveHistorique(historique);
                log.info("Persisted vulnerabilities and history for MobileAppScan ID: {}", scan.getId());
            }

            log.info("Scan completed for ID: {}", scan.getId());
        } catch (Exception e) {
            scan.setStatus(ScanStatus.FAILED);
            abstractScanRepository.save(scan);
            log.error("Scan processing failed for ID: {}", scan.getId(), e);
            throw new ScanException("Scan processing failed", e);
        }
    }

    private SeverityEnum mapSeverity(String severity) {
        if (severity == null) return SeverityEnum.INFO;
        switch (severity.toUpperCase()) {
            case "CRITICAL":
            case "DANGEROUS":
                return SeverityEnum.CRITICAL;
            case "HIGH": 
                return SeverityEnum.HIGH;
            case "MEDIUM":
            case "WARNING":
                return SeverityEnum.MEDIUM;
            case "LOW": 
                return SeverityEnum.LOW;
            default: 
                return SeverityEnum.INFO;
        }
    }

    private SeverityEnum calculateMaxSeverity(int critCount, int highCount, int medCount, int lowCount) {
        if (critCount > 0) return SeverityEnum.CRITICAL;
        if (highCount > 0) return SeverityEnum.HIGH;
        if (medCount > 0) return SeverityEnum.MEDIUM;
        if (lowCount > 0) return SeverityEnum.LOW;
        return SeverityEnum.INFO;
    }

    private AppScanResponseDTO mapToResponseDTO(AbstractScan scan) {
        AppScanResponseDTO dto = new AppScanResponseDTO();
        dto.setId(scan.getId());
        dto.setProjectName(scan.getProjectName());
        dto.setFileName(scan.getFileName());
        dto.setFilePath(scan.getFilePath());
        dto.setScanType(scan.getScanType());
        dto.setStatus(scan.getStatus());
        dto.setTotalVulnerabilities(scan.getTotalVulnerabilities());
        dto.setCriticalCount(scan.getCriticalCount());
        dto.setHighCount(scan.getHighCount());
        dto.setMediumCount(scan.getMediumCount());
        dto.setLowCount(scan.getLowCount());
        dto.setCreatedAt(scan.getCreatedAt());
        dto.setCompletedAt(scan.getCompletedAt());
        return dto;
    }
}