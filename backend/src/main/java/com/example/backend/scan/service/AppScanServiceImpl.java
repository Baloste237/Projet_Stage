package com.example.backend.scan.service;

import com.example.backend.scan.dto.AppScanResponseDTO;
import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.ScanStatus;
import com.example.backend.scan.entity.ScanType;
import com.example.backend.scan.exception.ScanException;
import com.example.backend.scan.repository.AbstractScanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public AppScanServiceImpl(AbstractScanRepository abstractScanRepository, List<ScanProcessor> processors) {
        this.abstractScanRepository = abstractScanRepository;
        this.processors = processors;
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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

        // Simulate scan processing
        simulateScanProcessing(scan);

        // Convert to DTO
        return mapToResponseDTO(scan);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ScanException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ScanException("File size exceeds maximum allowed size of 10MB");
        }
        if (!isZipFile(file)) {
            throw new ScanException("Only ZIP files are allowed");
        }
    }

    private boolean isZipFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        return (contentType != null && contentType.equals("application/zip")) ||
                (fileName != null && fileName.toLowerCase().endsWith(".zip"));
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

    private void simulateScanProcessing(AbstractScan scan) {
        try {
            scan.setStatus(ScanStatus.PROCESSING);
            abstractScanRepository.save(scan);
            log.info("Scan status updated to PROCESSING for ID: {}", scan.getId());

            Thread.sleep(2000); // 2 seconds simulation

            scan.setStatus(ScanStatus.DONE);
            scan.setTotalVulnerabilities(5);
            scan.setCriticalCount(1);
            scan.setHighCount(2);
            scan.setMediumCount(1);
            scan.setLowCount(1);
            scan.setCompletedAt(LocalDateTime.now());
            abstractScanRepository.save(scan);
            log.info("Scan completed for ID: {}", scan.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scan.setStatus(ScanStatus.FAILED);
            abstractScanRepository.save(scan);
            log.error("Scan processing interrupted for ID: {}", scan.getId());
            throw new ScanException("Scan processing was interrupted", e);
        }
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