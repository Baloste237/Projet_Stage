package com.example.backend.scan.service;

import com.example.backend.scan.dto.stats.DashboardStatsDTO;
import com.example.backend.scan.dto.stats.StatsMobileDTO;
import com.example.backend.scan.dto.stats.StatsWebDTO;

/**
 * Service de calcul des statistiques de sécurité.
 */
public interface StatsService {

    /** Statistiques web OWASP complètes */
    StatsWebDTO getWebStats();

    /** Statistiques mobile OWASP complètes */
    StatsMobileDTO getMobileStats();

    /** Résumé global dashboard */
    DashboardStatsDTO getDashboardStats();
}
