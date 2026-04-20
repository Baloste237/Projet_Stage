package com.example.backend.scan.repository;

import com.example.backend.scan.entity.Historique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueRepository extends JpaRepository<Historique, Long> {
    List<Historique> findByScanId(Long scanId);
}
