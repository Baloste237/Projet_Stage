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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<AbstractScan> getAllScans(Pageable pageable) {
        log.info("Retrieving all scans with pagination: {}", pageable);
        return abstractScanRepository.findAll(pageable);
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
                                String lowerPathForFilter = strPath.toLowerCase();
                                return !strPath.contains("/node_modules/") 
                                    && !strPath.contains("/.git/") 
                                    && !strPath.contains("/dist/") 
                                    && !strPath.contains("/build/")
                                    && !strPath.contains("/vendor/")
                                    && !strPath.contains("/libs/")
                                    && !strPath.contains("/assets/")
                                    && !lowerPathForFilter.endsWith(".spec.ts")
                                    && !lowerPathForFilter.endsWith(".test.ts")
                                    && !lowerPathForFilter.endsWith(".min.js");
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
                                        vuln.setStatus(finding.status);
                                        vuln.setRecommendation(finding.recommendation);
                                        vulnerabiliteService.saveVulnerabilite(vuln);
                                        
                                        if (!"FAUX POSITIF".equals(finding.status)) {
                                            totalVulnerabilities.incrementAndGet();
                                        }
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
                                                vuln.setStatus("CONFIRMÉE");
                                                vuln.setRecommendation("Corriger le code selon l'analyse avancée.");
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
                    
                    // Parcourir Vulns Code Analysis de façon dynamique (JsonNode)
                    if (scanResp.getCodeAnalysis() != null) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode codeAnalysisNode = mapper.valueToTree(scanResp.getCodeAnalysis());
                        
                        // 1. Sauvegarde du JSON complet brut sans parsing strict (Objectif 1)
                        try {
                            String rawJson = mapper.writeValueAsString(scanResp.getCodeAnalysis());
                            mobileScan.setRawCodeAnalysis(rawJson);
                            log.info("Code_analysis JSON brut sauvegardé pour le scan Mobile ID: {}", mobileScan.getId());
                        } catch (Exception e) {
                            log.warn("Erreur lors de la sauvegarde du JSON brut code_analysis", e);
                        }
                        
                        com.fasterxml.jackson.databind.JsonNode findingsNode = codeAnalysisNode;
                        // Gérer la structure variable : code_analysis -> findings -> ...
                        if (codeAnalysisNode.has("findings")) {
                            findingsNode = codeAnalysisNode.get("findings");
                        }

                        if (findingsNode != null && !findingsNode.isEmpty()) {
                            java.util.List<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> vulnNodes = new java.util.ArrayList<>();
                            
                            if (findingsNode.isObject()) {
                                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = findingsNode.fields();
                                while(fields.hasNext()) {
                                    vulnNodes.add(fields.next());
                                }
                            } else if (findingsNode.isArray()) {
                                int idx = 0;
                                for (com.fasterxml.jackson.databind.JsonNode node : findingsNode) {
                                    String key = node.has("title") ? node.get("title").asText() : (node.has("name") ? node.get("name").asText() : "Vuln-" + idx);
                                    vulnNodes.add(new java.util.AbstractMap.SimpleEntry<>(key, node));
                                    idx++;
                                }
                            }

                            for (java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry : vulnNodes) {
                                String key = entry.getKey();
                                com.fasterxml.jackson.databind.JsonNode vulnNode = entry.getValue();
                                if ("findings".equals(key)) continue; // sécurité supplémentaire
                                
                                try {
                                    String cwe = extractCweDynamically(vulnNode, key);
                                    
                                    String filesStr = "bytecode";
                                    if (vulnNode.has("files")) {
                                        com.fasterxml.jackson.databind.JsonNode filesNode = vulnNode.get("files");
                                        if (filesNode.isArray()) {
                                            java.util.List<String> fileList = new java.util.ArrayList<>();
                                            for (com.fasterxml.jackson.databind.JsonNode fn : filesNode) { fileList.add(fn.asText()); }
                                            filesStr = String.join(", ", fileList);
                                        } else if (filesNode.isObject()) {
                                            java.util.List<String> fileList = new java.util.ArrayList<>();
                                            java.util.Iterator<String> fileKeys = filesNode.fieldNames();
                                            while (fileKeys.hasNext()) { fileList.add(fileKeys.next()); }
                                            filesStr = String.join(", ", fileList);
                                        } else if (!filesNode.isNull()) {
                                            filesStr = filesNode.asText();
                                        }
                                    }
                                    
                                    String sevStr = "INFO";
                                    if (vulnNode.has("severity") && !vulnNode.get("severity").isNull()) {
                                        sevStr = vulnNode.get("severity").asText();
                                    } else if (vulnNode.has("level") && !vulnNode.get("level").isNull()) {
                                        sevStr = vulnNode.get("level").asText();
                                    }
                                    SeverityEnum sevEnum = mapSeverity(sevStr);
                                    
                                    if (sevEnum != SeverityEnum.INFO) { 
                                        double cvss = 5.0;
                                        if (vulnNode.has("cvss") && vulnNode.get("cvss").isNumber()) {
                                            cvss = vulnNode.get("cvss").asDouble();
                                        }
                                        
                                        String desc = "No description";
                                        if (vulnNode.has("description") && !vulnNode.get("description").isNull()) {
                                            desc = vulnNode.get("description").asText();
                                        } else if (vulnNode.has("desc") && !vulnNode.get("desc").isNull()) {
                                            desc = vulnNode.get("desc").asText();
                                        }
                                        
                                        // Troncature sécurisée pour la BDD
                                        String fullDesc = desc + " (Files: " + filesStr + ")";
                                        if (fullDesc.length() > 255) {
                                            fullDesc = fullDesc.substring(0, 250) + "...";
                                        }

                                        Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, cvss, key, mobileScan);
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
                                } catch (Exception ex) {
                                    log.warn("Impossible de parser un item code_analysis de manière dynamique: {}", key, ex);
                                }
                            }
                        }
                    }
                    
                    // Parcourir Manifest Analysis
                    if (scanResp.getManifestAnalysis() != null) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        // Ignore unknown properties to avoid crash
                        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        
                        try {
                            if (scanResp.getManifestAnalysis() instanceof java.util.List) {
                                java.util.List<?> rawList = (java.util.List<?>) scanResp.getManifestAnalysis();
                                for (Object obj : rawList) {
                                    try {
                                        MobSFScanResponse.ManifestIssue issue = mapper.convertValue(obj, MobSFScanResponse.ManifestIssue.class);
                                        SeverityEnum sevEnum = mapSeverity(issue.getEffectiveSeverity());
                                        if (sevEnum != SeverityEnum.INFO) {
                                            String desc = issue.getEffectiveDescription() != null ? issue.getEffectiveDescription() : "No description";
                                            String cwe = issue.getEffectiveCwe();
                                            String title = issue.getEffectiveTitle() != null ? issue.getEffectiveTitle() : "Manifest Issue";
                                            
                                            String fullDesc = desc + " (Manifest)";
                                            if (fullDesc.length() > 255) {
                                                fullDesc = fullDesc.substring(0, 250) + "...";
                                            }

                                            Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, 5.0, title, mobileScan);
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
                                    } catch (Exception itemEx) {
                                        log.warn("Impossible de parser un item manifest_analysis (list): {}", obj, itemEx);
                                    }
                                }
                            } else if (scanResp.getManifestAnalysis() instanceof java.util.Map) {
                                java.util.Map<?, ?> rawMap = (java.util.Map<?, ?>) scanResp.getManifestAnalysis();
                                for (java.util.Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                    Object val = entry.getValue();
                                    
                                    // MobSF frequently wraps manifest issues inside an array for keys like "manifest_findings"
                                    if (val instanceof java.util.List) {
                                        java.util.List<?> listVal = (java.util.List<?>) val;
                                        for (Object item : listVal) {
                                            try {
                                                MobSFScanResponse.ManifestIssue issue = mapper.convertValue(item, MobSFScanResponse.ManifestIssue.class);
                                                SeverityEnum sevEnum = mapSeverity(issue.getEffectiveSeverity());
                                                if (sevEnum != SeverityEnum.INFO) {
                                                    String desc = issue.getEffectiveDescription() != null ? issue.getEffectiveDescription() : "No description";
                                                    String cwe = issue.getEffectiveCwe();
                                                    String title = issue.getEffectiveTitle() != null ? issue.getEffectiveTitle() : entry.getKey().toString();
                                                    
                                                    String fullDesc = desc + " (Manifest)";
                                                    if (fullDesc.length() > 255) {
                                                        fullDesc = fullDesc.substring(0, 250) + "...";
                                                    }

                                                    Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, 5.0, title, mobileScan);
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
                                            } catch (Exception itemEx) {
                                                log.warn("Impossible de parser un item manifest_analysis list item (map): {}", item, itemEx);
                                            }
                                        }
                                    } else {
                                        // Standard map entry
                                        try {
                                            MobSFScanResponse.ManifestIssue issue = mapper.convertValue(val, MobSFScanResponse.ManifestIssue.class);
                                            SeverityEnum sevEnum = mapSeverity(issue.getEffectiveSeverity());
                                            if (sevEnum != SeverityEnum.INFO) {
                                                String desc = issue.getEffectiveDescription() != null ? issue.getEffectiveDescription() : "No description";
                                                String cwe = issue.getEffectiveCwe();
                                                String title = issue.getEffectiveTitle() != null ? issue.getEffectiveTitle() : entry.getKey().toString();
                                                
                                                String fullDesc = desc + " (Manifest)";
                                                if (fullDesc.length() > 255) {
                                                    fullDesc = fullDesc.substring(0, 250) + "...";
                                                }

                                                Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, 5.0, title, mobileScan);
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
                                        } catch (Exception itemEx) {
                                            log.warn("Impossible de parser un item manifest_analysis (map): {}", entry.getKey(), itemEx);
                                        }
                                    }
                                }
                            }
                        } catch (Exception parseEx) {
                            log.warn("Impossible de parser le manifest_analysis", parseEx);
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

    private String extractCweDynamically(com.fasterxml.jackson.databind.JsonNode vulnNode, String contextKey) {
        if (vulnNode.has("cwe") && !vulnNode.get("cwe").asText().trim().isEmpty()) {
            String cwe = vulnNode.get("cwe").asText().trim();
            log.info("CWE détecté depuis code_analysis (champ cwe) pour [{}] : {}", contextKey, cwe);
            return cwe;
        } else if (vulnNode.has("owasp") && !vulnNode.get("owasp").asText().trim().isEmpty()) {
            String owasp = vulnNode.get("owasp").asText().trim();
            log.info("CWE détecté depuis code_analysis (fallback owasp) pour [{}] : {}", contextKey, owasp);
            return owasp;
        } else if (vulnNode.has("owasp-mobile") && !vulnNode.get("owasp-mobile").asText().trim().isEmpty()) {
            String owaspMobile = vulnNode.get("owasp-mobile").asText().trim();
            log.info("CWE détecté depuis code_analysis (fallback owasp-mobile) pour [{}] : {}", contextKey, owaspMobile);
            return owaspMobile;
        }
        log.debug("Aucun CWE ou OWASP trouvé pour la vulnérabilité : {}", contextKey);
        return "N/A";
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