package com.example.backend.scan.repository;

import com.example.backend.scan.entity.AbstractScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for all scan types.
 */
@NoRepositoryBean
public interface AbstractScanBaseRepository<T extends AbstractScan> extends JpaRepository<T, Long> {
}
