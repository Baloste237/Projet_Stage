package com.example.backend.scan.repository;

import com.example.backend.scan.entity.SeverityEnum;
import com.example.backend.scan.entity.Vulnerabilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VulnerabiliteRepository extends JpaRepository<Vulnerabilite, Long>, JpaSpecificationExecutor<Vulnerabilite> {
    List<Vulnerabilite> findByScanId(Long scanId);
}
