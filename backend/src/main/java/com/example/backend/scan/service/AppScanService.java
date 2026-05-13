package com.example.backend.scan.service;

import com.example.backend.scan.dto.AppScanResponseDTO;
import com.example.backend.scan.entity.AbstractScan;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing application scans.
 */
public interface AppScanService {

    /**
     * Creates a new scan.
     * 
     * @param scan the scan to create
     * @return the created scan
     */
    AbstractScan createScan(AbstractScan scan);

    /**
     * Retrieves a scan by its ID.
     * 
     * @param id the scan ID
     * @return the scan
     */
    AbstractScan getScanById(Long id);

    /**
     * Retrieves all scans with pagination.
     * 
     * @param pageable pagination information
     * @return page of scans
     */
    Page<AbstractScan> getAllScans(Pageable pageable);

    /**
     * Deletes a scan by its ID.
     * 
     * @param id the scan ID
     */
    void deleteScan(Long id);

    /**
     * Processes a scan from a ZIP file based on app type.
     * 
     * @param appType     the type of the application
     * @param file        the ZIP file
     * @param projectName the project name
     * @return the scan response DTO
     */
    AppScanResponseDTO processScan(String appType, MultipartFile file, String projectName);
}