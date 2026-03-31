package com.example.backend.scan.repository;

import com.example.backend.scan.entity.WebAppScan;
import org.springframework.stereotype.Repository;

/**
 * Repository specifically for Web Application scans.
 */
@Repository
public interface WebAppScanRepository extends AbstractScanBaseRepository<WebAppScan> {
}
