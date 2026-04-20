package com.example.backend.scan.repository;

import com.example.backend.scan.entity.Vulnerabilite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VulnerabiliteRepository extends JpaRepository<Vulnerabilite, Long> {
    List<Vulnerabilite> findByScanId(Long scanId);
}
