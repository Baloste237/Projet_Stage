package com.example.backend.monitoring.repository;

import com.example.backend.monitoring.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByUserIdContainingIgnoreCase(String userId, Pageable pageable);
    Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);
    long countByStatusGreaterThanEqual(int status); // e.g. >= 400 for errors
}
