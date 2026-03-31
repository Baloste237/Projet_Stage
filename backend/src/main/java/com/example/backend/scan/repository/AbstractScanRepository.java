package com.example.backend.scan.repository;

import com.example.backend.scan.entity.AbstractScan;
import org.springframework.stereotype.Repository;

/**
 * Repository for querying any scan type generically.
 */
@Repository
public interface AbstractScanRepository extends AbstractScanBaseRepository<AbstractScan> {
}
