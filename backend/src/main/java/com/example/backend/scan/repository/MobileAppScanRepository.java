package com.example.backend.scan.repository;

import com.example.backend.scan.entity.MobileAppScan;
import org.springframework.stereotype.Repository;

/**
 * Repository specifically for Mobile Application scans.
 */
@Repository
public interface MobileAppScanRepository extends AbstractScanBaseRepository<MobileAppScan> {
}
