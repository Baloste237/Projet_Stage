package com.example.backend.monitoring.service;

import com.example.backend.monitoring.entity.AuditLog;
import com.example.backend.monitoring.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void logAction(String userId, String action, String endpoint, String method, Integer status, String details, String logLevel) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .endpoint(endpoint)
                    .method(method)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .details(details)
                    .logLevel(logLevel)
                    .build();

            AuditLog savedLog = auditLogRepository.save(auditLog);

            // Broadcast real-time activity
            messagingTemplate.convertAndSend("/topic/logs", savedLog);
            
            // Broadcast metrics if needed
            sendMetricsUpdate();
        } catch (Exception e) {
            log.error("Failed to save audit log: ", e);
        }
    }

    @Async
    public void sendMetricsUpdate() {
        // Simplified metrics for real-time dashboard
        try {
            long errorCount = auditLogRepository.countByStatusGreaterThanEqual(400);
            String metrics = "{\"errorCount\": " + errorCount + "}";
            messagingTemplate.convertAndSend("/topic/metrics", metrics);
        } catch (Exception e) {
            log.error("Failed to send metrics: ", e);
        }
    }
}
