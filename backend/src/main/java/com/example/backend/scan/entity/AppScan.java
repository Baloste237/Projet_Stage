package com.example.backend.scan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an application scan.
 */
@Entity
@Table(name = "app_scans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanType scanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;

    @Column(nullable = false)
    private int totalVulnerabilities;

    @Column(nullable = false)
    private int criticalCount;

    @Column(nullable = false)
    private int highCount;

    @Column(nullable = false)
    private int mediumCount;

    @Column(nullable = false)
    private int lowCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}