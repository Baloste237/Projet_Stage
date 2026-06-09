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
import jakarta.annotation.PostConstruct;

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
    private final AsyncExecutorService asyncExecutorService;
    private final OwaspMobileMappingService owaspMappingService;

    public AppScanServiceImpl(AbstractScanRepository abstractScanRepository, 
                              List<ScanProcessor> processors,
                              VulnerabiliteService vulnerabiliteService,
                              HistoriqueService historiqueService,
                              MobSFClient mobsfClient,
                              AsyncExecutorService asyncExecutorService,
                              OwaspMobileMappingService owaspMappingService) {
        this.abstractScanRepository = abstractScanRepository;
        this.processors = processors;
        this.vulnerabiliteService = vulnerabiliteService;
        this.historiqueService = historiqueService;
        this.mobsfClient = mobsfClient;
        this.asyncExecutorService = asyncExecutorService;
        this.owaspMappingService = owaspMappingService;
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${sast.engine.url:http://localhost:8000/api/v1/analyze}")
    private String sastEngineUrl;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    /**
     * Vérifie au démarrage que le moteur SAST FastAPI est joignable.
     * Un avertissement clair est loggé si la connexion échoue, pour alerter
     * immédiatement le développeur sans bloquer le démarrage de l'application.
     */
    @PostConstruct
    public void validateSastEngineConnection() {
        String healthUrl = sastEngineUrl.replace("/analyze", "/health");
        try {
            RestTemplate rt = new RestTemplate();
            rt.getForObject(healthUrl, String.class);
            log.info("✅ [SAST] Moteur IA connecté avec succès : {}", sastEngineUrl);
        } catch (Exception e) {
            log.error("❌ [SAST] MOTEUR IA INACCESSIBLE à l'URL : {}", sastEngineUrl);
            log.error("❌ [SAST] Les analyses Java (IA) ne fonctionneront pas. Cause : {}", e.getMessage());
            log.error("❌ [SAST] Vérifiez que FastAPI est démarré et que la propriété 'sast.engine.url' est correcte dans application.properties");
            log.error("❌ [SAST] URL de santé testée : {}", healthUrl);
        }
    }

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
        scan.setStartedAt(LocalDateTime.now());
        scan.setProgress(5);
        scan.setCurrentStep("Initialisation");
        scan.setLogs("Scan reçu et initialisé.\\n");

        scan = createScan(scan);

        // Execute scan processing asynchronously
        final AbstractScan finalScan = scan;
        asyncExecutorService.execute(() -> executeSastScan(finalScan));

        // Return DTO immediately (202 Accepted logic is in controller, here we return PENDING DTO)
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

    private void updateScanProgress(AbstractScan scan, int progress, String step, String logLine) {
        try {
            AbstractScan currentScan = abstractScanRepository.findById(scan.getId()).orElse(scan);
            currentScan.setProgress(progress);
            currentScan.setCurrentStep(step);
            String existingLogs = currentScan.getLogs();
            if (existingLogs == null) existingLogs = "";
            currentScan.setLogs(existingLogs + logLine + "\n");
            AbstractScan saved = abstractScanRepository.save(currentScan);
            
            // Sync with local reference
            scan.setProgress(saved.getProgress());
            scan.setCurrentStep(saved.getCurrentStep());
            scan.setLogs(saved.getLogs());
            log.info("Scan ID {}: progress updated to {}% - {} - {}", scan.getId(), progress, step, logLine);
        } catch (Exception e) {
            log.error("Failed to update progress for scan ID {}", scan.getId(), e);
        }
    }

    private void executeSastScan(AbstractScan scanParam) {
        AbstractScan currentScanForCatch = scanParam;
        try {
            // ── CORRECTIF BUG#1 : Recharger l'entité dans le thread async pour éviter JPA detached ──
            final Long scanId = scanParam.getId();
            AbstractScan loadedScan = abstractScanRepository.findById(scanId)
                .orElseThrow(() -> new com.example.backend.scan.exception.ScanException("Scan introuvable pour exécution async : " + scanId));
            
            currentScanForCatch = loadedScan;
            final AbstractScan scan = loadedScan; // Variable final pour les expressions lambda
            
            log.info("[ASYNC] Entité scan rechargée dans le thread async — ID: {}, classe: {}", scan.getId(), scan.getClass().getSimpleName());

            scan.setStatus(ScanStatus.RUNNING);
            scan.setStartedAt(LocalDateTime.now());
            abstractScanRepository.save(scan);
            log.info("Scan status updated to RUNNING for ID: {}", scan.getId());
            updateScanProgress(scan, 5, "Initialisation", "Démarrage de l'analyse SAST...");

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
                    updateScanProgress(scan, 10, "Extraction", "Extraction de l'archive ZIP...");
                    FileExtractionUtil.extractZipSafely(zipPath, tempExtractionDir);
                    
                    updateScanProgress(scan, 30, "Scan SAST Local", "Démarrage de l'analyse locale (détection Regex)...");
                    
                    try {
                        java.nio.file.Files.walkFileTree(tempExtractionDir, new java.nio.file.SimpleFileVisitor<Path>() {
                            @Override
                            public java.nio.file.FileVisitResult visitFile(Path filePath, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                                if (!attrs.isRegularFile()) return java.nio.file.FileVisitResult.CONTINUE;
                                
                                String strPath = filePath.toString().replace("\\", "/");
                                String lowerPathForFilter = strPath.toLowerCase();
                                
                                if (strPath.contains("/node_modules/") 
                                    || strPath.contains("/.git/") 
                                    || strPath.contains("/dist/") 
                                    || strPath.contains("/build/")
                                    || strPath.contains("/vendor/")
                                    || strPath.contains("/libs/")
                                    || strPath.contains("/assets/")
                                    || lowerPathForFilter.endsWith(".spec.ts")
                                    || lowerPathForFilter.endsWith(".test.ts")
                                    || lowerPathForFilter.endsWith(".min.js")) {
                                    return java.nio.file.FileVisitResult.CONTINUE;
                                }

                                String relativePath = tempExtractionDir.relativize(filePath).toString().replace("\\", "/");
                                String lowerPath = relativePath.toLowerCase();

                                // ── CORRECTIF BUG#5 : try/catch global par fichier pour éviter l'interruption du stream ──
                                try {

                                // 1. Scan via Regex local
                                if (lowerPath.endsWith(".java") || lowerPath.endsWith(".js") || lowerPath.endsWith(".ts") 
                                     || lowerPath.endsWith(".py") || lowerPath.endsWith(".php") || lowerPath.endsWith(".html") 
                                     || lowerPath.endsWith(".xml") || lowerPath.endsWith(".json")) {
                                    
                                    List<VulnerabilityDetector.Finding> localFindings = VulnerabilityDetector.scanFile(filePath, relativePath);
                                    log.debug("[REGEX] {} findings pour : {}", localFindings.size(), relativePath);
                                    for (VulnerabilityDetector.Finding finding : localFindings) {
                                        try {
                                            Vulnerabilite vuln = new Vulnerabilite(null, finding.cweId, finding.description, finding.severity, finding.cweId, finding.adjustedScore, finding.ruleName, webScan);
                                            vuln.setStatus(finding.status);
                                            vuln.setRecommendation(finding.recommendation);
                                            Vulnerabilite saved = vulnerabiliteService.saveVulnerabilite(vuln);
                                            log.debug("[REGEX] Vuln sauvegardée id={} scan_id={} type={}", saved.getId(), webScan.getId(), finding.cweId);
                                            
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
                                        } catch (Exception saveEx) {
                                            log.error("[REGEX] Erreur sauvegarde vuln '{}' pour fichier '{}' : {}", finding.cweId, relativePath, saveEx.getMessage(), saveEx);
                                        }
                                    }
                                }

                                // 2. Scan via IA (seulement pour .java)
                                if (lowerPath.endsWith(".java")) {
                                    try {
                                        if (scan.getProgress() < 60) {
                                            updateScanProgress(scan, 60, "Analyse IA", "Lancement du moteur d'analyse IA pour les fichiers Java...");
                                        }
                                        String code = new String(Files.readAllBytes(filePath), java.nio.charset.StandardCharsets.UTF_8);
                                        com.example.backend.scan.dto.ia.IaAnalysisRequestDTO req = new com.example.backend.scan.dto.ia.IaAnalysisRequestDTO(code, relativePath);
                                        com.example.backend.scan.dto.ia.IaAnalysisResponseDTO res = restTemplate.postForObject(sastEngineUrl, req, com.example.backend.scan.dto.ia.IaAnalysisResponseDTO.class);
                                        log.debug("[IA] Réponse FastAPI pour {} : is_vulnerable={} cvss_count={}",
                                            relativePath,
                                            res != null ? res.isVulnerable() : "null",
                                            res != null && res.getCvssScores() != null ? res.getCvssScores().size() : 0);
                                        
                                        if (res != null && res.isVulnerable() && res.getCvssScores() != null) {
                                            for (com.example.backend.scan.dto.ia.IaAnalysisResponseDTO.IaCvssResult cvss : res.getCvssScores()) {
                                                try {
                                                    SeverityEnum sevEnum = mapSeverity(cvss.getSeverity());
                                                    Vulnerabilite vuln = new Vulnerabilite(null, cvss.getCweId(), cvss.getRationale() + " (File: " + relativePath + ")", sevEnum, cvss.getCweId(), cvss.getAdjustedScore(), "IA Detection", webScan);
                                                    vuln.setStatus("CONFIRMÉE");
                                                    vuln.setRecommendation("Corriger le code selon l'analyse avancée.");
                                                    Vulnerabilite saved = vulnerabiliteService.saveVulnerabilite(vuln);
                                                    log.debug("[IA] Vuln sauvegardée id={} scan_id={} cwe={}", saved.getId(), webScan.getId(), cvss.getCweId());
                                                    
                                                    totalVulnerabilities.incrementAndGet();
                                                    switch(sevEnum) {
                                                        case CRITICAL: critCount.incrementAndGet(); break;
                                                        case HIGH: highCount.incrementAndGet(); break;
                                                        case MEDIUM: medCount.incrementAndGet(); break;
                                                        case LOW: lowCount.incrementAndGet(); break;
                                                        default: break;
                                                    }
                                                } catch (Exception saveEx) {
                                                    log.error("[IA] Erreur sauvegarde vuln IA '{}' pour '{}' : {}", cvss.getCweId(), relativePath, saveEx.getMessage(), saveEx);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("[IA] Erreur appel moteur IA pour '{}' : {}", relativePath, e.getMessage(), e);
                                    }
                                }

                                } catch (Exception fileEx) {
                                    // ── CORRECTIF BUG#5 : catch global par fichier — le stream continue malgré l'erreur ──
                                    log.error("[SCAN] Erreur inattendue lors de l'analyse du fichier '{}' : {}", relativePath, fileEx.getMessage(), fileEx);
                                }
                                return java.nio.file.FileVisitResult.CONTINUE;
                            }

                            @Override
                            public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) {
                                log.warn("[SCAN] Impossible d'accéder au fichier/dossier : {} - {}", file, exc.getMessage());
                                return java.nio.file.FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        log.error("[SCAN] Erreur globale lors du parcours de l'arborescence", e);
                    }
                } finally {
                    FileExtractionUtil.deleteDirectory(tempExtractionDir.toFile());
                }

                updateScanProgress(scan, 90, "Sauvegarde", "Finalisation et enregistrement des vulnérabilités...");
                
                scan.setStatus(ScanStatus.COMPLETED);
                scan.setTotalVulnerabilities(totalVulnerabilities.get());
                scan.setCriticalCount(critCount.get());
                scan.setHighCount(highCount.get());
                scan.setMediumCount(medCount.get());
                scan.setLowCount(lowCount.get());
                scan.setCompletedAt(LocalDateTime.now());
                if (scan.getStartedAt() != null) {
                    long duration = java.time.Duration.between(scan.getStartedAt(), LocalDateTime.now()).toMillis();
                    scan.setExecutionTime(duration);
                }
                abstractScanRepository.save(scan);

                SeverityEnum maxSev = calculateMaxSeverity(critCount.get(), highCount.get(), medCount.get(), lowCount.get());
                Historique historique = new Historique(null, LocalDateTime.now(), webScan.getTotalVulnerabilities(), maxSev, webScan);
                historiqueService.saveHistorique(historique);
                log.info("Persisted vulnerabilities and history for WebAppScan ID: {}", scan.getId());
                
                updateScanProgress(scan, 100, "Terminé", "Scan de sécurité web terminé avec succès !");
            } else if (scan instanceof com.example.backend.scan.entity.MobileAppScan) {
                com.example.backend.scan.entity.MobileAppScan mobileScan = (com.example.backend.scan.entity.MobileAppScan) scan;
                int totalVulnerabilities = 0;
                int critCount = 0;
                int highCount = 0;
                int medCount = 0;
                int lowCount = 0;

                try {
                    // Upload
                    updateScanProgress(scan, 15, "Upload MobSF", "Téléversement de l'APK vers le serveur MobSF...");
                    java.io.File apkFile = new java.io.File(scan.getFilePath());
                    com.example.backend.scan.dto.MobSFUploadResponse uploadResp = mobsfClient.uploadApk(apkFile);
                    
                    mobileScan.setProjectName(uploadResp.getPackageName() != null ? uploadResp.getPackageName() : mobileScan.getProjectName());
                    mobileScan.setTargetOs("Android");
                    
                    // Lancer Analyse
                    updateScanProgress(scan, 40, "Analyse MobSF", "Démarrage de l'analyse statique MobSF...");
                    com.example.backend.scan.dto.MobSFScanResponse scanResp = mobsfClient.lancerAnalyse(uploadResp.getHash());
                    
                    // Parcourir Vulns Code Analysis de façon dynamique (JsonNode)
                    updateScanProgress(scan, 60, "Traitement Code Analysis", "Analyse du code source et détection des faiblesses...");
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
                                    
                                    // MobSF embeds cvss/severity/description inside a "metadata" sub-object
                                    com.fasterxml.jackson.databind.JsonNode metaNode = vulnNode.has("metadata") ? vulnNode.get("metadata") : vulnNode;

                                    String sevStr = "INFO";
                                    if (vulnNode.has("severity") && !vulnNode.get("severity").isNull()) {
                                        sevStr = vulnNode.get("severity").asText();
                                    } else if (vulnNode.has("level") && !vulnNode.get("level").isNull()) {
                                        sevStr = vulnNode.get("level").asText();
                                    } else if (metaNode.has("severity") && !metaNode.get("severity").isNull()) {
                                        sevStr = metaNode.get("severity").asText();
                                    }
                                    SeverityEnum sevEnum = mapSeverity(sevStr);
                                    
                                    if (sevEnum != SeverityEnum.INFO) {
                                        // Read CVSS from metadata first (real MobSF location), fallback to root
                                        double cvss = 5.0;
                                        if (metaNode.has("cvss") && metaNode.get("cvss").isNumber()) {
                                            cvss = metaNode.get("cvss").asDouble();
                                        } else if (vulnNode.has("cvss") && vulnNode.get("cvss").isNumber()) {
                                            cvss = vulnNode.get("cvss").asDouble();
                                        }
                                        log.debug("CVSS extrait pour [{}] : {}", key, cvss);
                                        
                                        // Read description from metadata first, fallback to root
                                        String desc = "No description";
                                        if (metaNode.has("description") && !metaNode.get("description").isNull()) {
                                            desc = metaNode.get("description").asText();
                                        } else if (metaNode.has("desc") && !metaNode.get("desc").isNull()) {
                                            desc = metaNode.get("desc").asText();
                                        } else if (vulnNode.has("description") && !vulnNode.get("description").isNull()) {
                                            desc = vulnNode.get("description").asText();
                                        } else if (vulnNode.has("desc") && !vulnNode.get("desc").isNull()) {
                                            desc = vulnNode.get("desc").asText();
                                        }
                                        
                                        // Troncature sécurisée pour la BDD
                                        String fullDesc = desc + " (Files: " + filesStr + ")";
                                        if (fullDesc.length() > 255) {
                                            fullDesc = fullDesc.substring(0, 250) + "...";
                                        }

                                        com.example.backend.scan.dto.OwaspMappingResult mapRes = owaspMappingService.mapCategory(key, cwe, fullDesc);
                                        Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, cvss, mapRes.getOwaspId() + " - " + mapRes.getOwaspName(), mobileScan);
                                        v.setLegacyCategory(mapRes.getLegacyCategory());
                                        v.setOwaspVersion(mapRes.getOwaspVersion());
                                        v.setOwaspId(mapRes.getOwaspId());
                                        v.setOwaspName(mapRes.getOwaspName());
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
                    updateScanProgress(scan, 75, "Traitement Manifest", "Analyse du fichier AndroidManifest.xml...");
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
                                            double cvss = issue.getEffectiveCvss(); // real CVSS from MobSF
                                            
                                            String fullDesc = desc + " (Manifest)";
                                            if (fullDesc.length() > 255) {
                                                fullDesc = fullDesc.substring(0, 250) + "...";
                                            }

                                            com.example.backend.scan.dto.OwaspMappingResult mapRes = owaspMappingService.mapCategory(title, cwe, fullDesc);
                                            Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, cvss, mapRes.getOwaspId() + " - " + mapRes.getOwaspName(), mobileScan);
                                            v.setLegacyCategory(mapRes.getLegacyCategory());
                                            v.setOwaspVersion(mapRes.getOwaspVersion());
                                            v.setOwaspId(mapRes.getOwaspId());
                                            v.setOwaspName(mapRes.getOwaspName());
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
                                                    double cvss = issue.getEffectiveCvss(); // real CVSS from MobSF
                                                    
                                                    String fullDesc = desc + " (Manifest)";
                                                    if (fullDesc.length() > 255) {
                                                        fullDesc = fullDesc.substring(0, 250) + "...";
                                                    }

                                                    com.example.backend.scan.dto.OwaspMappingResult mapRes = owaspMappingService.mapCategory(title, cwe, fullDesc);
                                                    Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, cvss, mapRes.getOwaspId() + " - " + mapRes.getOwaspName(), mobileScan);
                                                    v.setLegacyCategory(mapRes.getLegacyCategory());
                                                    v.setOwaspVersion(mapRes.getOwaspVersion());
                                                    v.setOwaspId(mapRes.getOwaspId());
                                                    v.setOwaspName(mapRes.getOwaspName());
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
                                                double cvss = issue.getEffectiveCvss(); // real CVSS from MobSF
                                                
                                                String fullDesc = desc + " (Manifest)";
                                                if (fullDesc.length() > 255) {
                                                    fullDesc = fullDesc.substring(0, 250) + "...";
                                                }

                                                com.example.backend.scan.dto.OwaspMappingResult mapRes = owaspMappingService.mapCategory(title, cwe, fullDesc);
                                                Vulnerabilite v = new Vulnerabilite(null, cwe, fullDesc, sevEnum, cwe, cvss, mapRes.getOwaspId() + " - " + mapRes.getOwaspName(), mobileScan);
                                                v.setLegacyCategory(mapRes.getLegacyCategory());
                                                v.setOwaspVersion(mapRes.getOwaspVersion());
                                                v.setOwaspId(mapRes.getOwaspId());
                                                v.setOwaspName(mapRes.getOwaspName());
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
                    updateScanProgress(scan, 90, "Traitement Permissions", "Vérification des permissions demandées par l'application...");
                    if (scanResp.getPermissions() != null) {
                        for (java.util.Map.Entry<String, com.example.backend.scan.dto.MobSFScanResponse.PermissionDetail> entry : scanResp.getPermissions().entrySet()) {
                            if ("dangerous".equalsIgnoreCase(entry.getValue().getStatus())) {
                                String fullDesc = "Permission Android Dangereuse: " + entry.getKey() + " - " + entry.getValue().getDescription();
                                com.example.backend.scan.dto.OwaspMappingResult mapRes = owaspMappingService.mapCategory("Permission Insecure", "CWE-276", fullDesc);
                                Vulnerabilite v = new Vulnerabilite(null, "CWE-276", fullDesc, SeverityEnum.LOW, "CWE-276", 2.0, mapRes.getOwaspId() + " - " + mapRes.getOwaspName(), mobileScan);
                                v.setLegacyCategory(mapRes.getLegacyCategory());
                                v.setOwaspVersion(mapRes.getOwaspVersion());
                                v.setOwaspId(mapRes.getOwaspId());
                                v.setOwaspName(mapRes.getOwaspName());
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
                
                scan.setStatus(ScanStatus.COMPLETED);
                scan.setTotalVulnerabilities(totalVulnerabilities);
                scan.setCriticalCount(critCount);
                scan.setHighCount(highCount);
                scan.setMediumCount(medCount);
                scan.setLowCount(lowCount);
                scan.setCompletedAt(LocalDateTime.now());
                if (scan.getStartedAt() != null) {
                    long duration = java.time.Duration.between(scan.getStartedAt(), LocalDateTime.now()).toMillis();
                    scan.setExecutionTime(duration);
                }
                abstractScanRepository.save(scan);

                SeverityEnum maxSev = calculateMaxSeverity(critCount, highCount, medCount, lowCount);
                Historique historique = new Historique(null, LocalDateTime.now(), mobileScan.getTotalVulnerabilities(), maxSev, mobileScan);
                historiqueService.saveHistorique(historique);
                log.info("Persisted vulnerabilities and history for MobileAppScan ID: {}", scan.getId());
                
                updateScanProgress(scan, 100, "Terminé", "Scan de sécurité mobile terminé avec succès !");
            }

            log.info("Scan completed for ID: {}", scan.getId());
        } catch (Exception e) {
            currentScanForCatch.setStatus(ScanStatus.FAILED);
            currentScanForCatch.setCompletedAt(LocalDateTime.now());
            if (currentScanForCatch.getStartedAt() != null) {
                long duration = java.time.Duration.between(currentScanForCatch.getStartedAt(), LocalDateTime.now()).toMillis();
                currentScanForCatch.setExecutionTime(duration);
            }
            abstractScanRepository.save(currentScanForCatch);
            updateScanProgress(currentScanForCatch, 100, "Échoué", "Erreur d'analyse : " + e.getMessage());
            log.error("Scan processing failed for ID: {}", currentScanForCatch.getId(), e);
            throw new ScanException("Scan processing failed", e);
        }
    }

    private String extractCweDynamically(com.fasterxml.jackson.databind.JsonNode vulnNode, String contextKey) {
        // MobSF wraps CWE, CVSS, OWASP inside a "metadata" sub-object per finding.
        // We must check metadata first, then fall back to the root node.
        com.fasterxml.jackson.databind.JsonNode meta = vulnNode.has("metadata") ? vulnNode.get("metadata") : vulnNode;

        if (meta.has("cwe") && !meta.get("cwe").asText("").trim().isEmpty() && !"None".equalsIgnoreCase(meta.get("cwe").asText("").trim())) {
            String cwe = meta.get("cwe").asText().trim();
            log.info("CWE détecté depuis metadata (champ cwe) pour [{}] : {}", contextKey, cwe);
            return cwe;
        }
        if (meta.has("owasp-mobile") && !meta.get("owasp-mobile").asText("").trim().isEmpty() && !"None".equalsIgnoreCase(meta.get("owasp-mobile").asText("").trim())) {
            String owaspMobile = meta.get("owasp-mobile").asText().trim();
            log.info("CWE détecté depuis metadata (fallback owasp-mobile) pour [{}] : {}", contextKey, owaspMobile);
            return owaspMobile;
        }
        if (meta.has("owasp") && !meta.get("owasp").asText("").trim().isEmpty() && !"None".equalsIgnoreCase(meta.get("owasp").asText("").trim())) {
            String owasp = meta.get("owasp").asText().trim();
            log.info("CWE détecté depuis metadata (fallback owasp) pour [{}] : {}", contextKey, owasp);
            return owasp;
        }
        // Fallback: search directly at root level (for non-standard MobSF responses)
        if (vulnNode.has("cwe") && !vulnNode.get("cwe").asText("").trim().isEmpty() && !"None".equalsIgnoreCase(vulnNode.get("cwe").asText("").trim())) {
            return vulnNode.get("cwe").asText().trim();
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
        dto.setProgress(scan.getProgress());
        dto.setCurrentStep(scan.getCurrentStep());
        dto.setLogs(scan.getLogs());
        dto.setStartedAt(scan.getStartedAt());
        dto.setExecutionTime(scan.getExecutionTime());
        return dto;
    }
}
