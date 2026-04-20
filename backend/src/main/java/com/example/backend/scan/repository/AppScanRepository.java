package com.example.backend.scan.repository;

import com.example.backend.scan.entity.AppScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for AppScan entity.
 */
@Repository
public interface AppScanRepository extends JpaRepository<AppScan, Long> {
}