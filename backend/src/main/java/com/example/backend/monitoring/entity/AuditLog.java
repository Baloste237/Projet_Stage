package com.example.backend.monitoring.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Journal d'audit — trace chaque action système (connexion, scan, erreur)")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unique du log", example = "1")
    private Long id;

    @Schema(description = "Identifiant de l'utilisateur (email ou username)", example = "admin@vulnscan.com")
    private String userId; // Can be email or username
    
    @Schema(description = "Action effectuée", example = "LOGIN_SUCCESS", allowableValues = {"LOGIN_SUCCESS", "LOGIN_FAILURE", "SCAN_WEB", "SCAN_APK", "GENERATE_REPORT", "DELETE_SCAN"})
    private String action; // e.g., "LOGIN_SUCCESS", "SCAN_APK", "GENERATE_REPORT"
    
    @Schema(description = "Endpoint API appelé", example = "/api/v1/analyze/web")
    private String endpoint;
    
    @Schema(description = "Méthode HTTP", example = "POST", allowableValues = {"GET", "POST", "PUT", "PATCH", "DELETE"})
    private String method; // GET, POST, etc.
    
    @Schema(description = "Code de statut HTTP de la réponse", example = "200")
    private Integer status; // 200, 400, 500, etc.
    
    @Schema(description = "Date et heure de l'événement", example = "2026-05-12T10:30:00")
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    @Schema(description = "Détails supplémentaires (JSON)", example = "{\"scanId\": 1, \"duration\": \"2.3s\"}")
    private String details; // Any extra details, JSON string
    
    @Schema(description = "Niveau de log", example = "INFO", allowableValues = {"INFO", "WARN", "ERROR"})
    private String logLevel; // INFO, WARN, ERROR
}
