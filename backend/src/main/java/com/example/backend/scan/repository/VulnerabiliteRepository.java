package com.example.backend.scan.repository;

import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.entity.SeverityEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VulnerabiliteRepository
        extends JpaRepository<Vulnerabilite, Long>, JpaSpecificationExecutor<Vulnerabilite> {

    List<Vulnerabilite> findByScanId(Long scanId);

    // ── Comptages globaux par sévérité ─────────────────────────────────────

    @Query("SELECT COUNT(v) FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan")
    long countWebVulnerabilities();

    @Query("SELECT COUNT(v) FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan")
    long countMobileVulnerabilities();

    @Query("SELECT COUNT(v) FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan " +
           "AND v.niv_grav = :severity")
    long countWebBySeverity(@Param("severity") SeverityEnum severity);

    @Query("SELECT COUNT(v) FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan " +
           "AND v.niv_grav = :severity")
    long countMobileBySeverity(@Param("severity") SeverityEnum severity);

    // ── Application la plus vulnérable ────────────────────────────────────

    @Query("SELECT s.projectName FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan " +
           "GROUP BY s.projectName ORDER BY COUNT(v) DESC")
    List<String> findMostVulnerableWebProject();

    @Query("SELECT s.projectName FROM Vulnerabilite v " +
           "JOIN v.scan s WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan " +
           "GROUP BY s.projectName ORDER BY COUNT(v) DESC")
    List<String> findMostVulnerableMobileProject();

    // ── Tendances mensuelles web ───────────────────────────────────────────

    @Query("SELECT CONCAT(YEAR(s.createdAt), '-', LPAD(CAST(MONTH(s.createdAt) AS string), 2, '0')), " +
           "SUM(CASE WHEN v.niv_grav = 'CRITICAL' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'HIGH' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'MEDIUM' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'LOW' THEN 1 ELSE 0 END), " +
           "COUNT(DISTINCT s.id) " +
           "FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan " +
           "AND s.createdAt >= :since " +
           "GROUP BY YEAR(s.createdAt), MONTH(s.createdAt) " +
           "ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<Object[]> findWebMonthlyTrend(@Param("since") LocalDateTime since);

    @Query("SELECT CONCAT(YEAR(s.createdAt), '-', LPAD(CAST(MONTH(s.createdAt) AS string), 2, '0')), " +
           "SUM(CASE WHEN v.niv_grav = 'CRITICAL' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'HIGH' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'MEDIUM' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.niv_grav = 'LOW' THEN 1 ELSE 0 END), " +
           "COUNT(DISTINCT s.id) " +
           "FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan " +
           "AND s.createdAt >= :since " +
           "GROUP BY YEAR(s.createdAt), MONTH(s.createdAt) " +
           "ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)")
    List<Object[]> findMobileMonthlyTrend(@Param("since") LocalDateTime since);

    // ── Toutes les vulnérabilités web (pour mapping OWASP côté service) ──

    @Query("SELECT v FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan")
    List<Vulnerabilite> findAllWebVulnerabilities();

    @Query("SELECT v FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan")
    List<Vulnerabilite> findAllMobileVulnerabilities();

    // ── Dernières dates ───────────────────────────────────────────────────

    @Query("SELECT MAX(s.createdAt) FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.WebAppScan")
    LocalDateTime findLastWebScanDate();

    @Query("SELECT MAX(s.createdAt) FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan")
    LocalDateTime findLastMobileScanDate();

    // ── Score CVSS moyen mobile ───────────────────────────────────────────

    @Query("SELECT AVG(v.cvssScore) FROM Vulnerabilite v JOIN v.scan s " +
           "WHERE TYPE(s) = com.example.backend.scan.entity.MobileAppScan " +
           "AND v.cvssScore IS NOT NULL")
    Double findAverageMobileCvssScore();
}
