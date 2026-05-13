package com.example.backend.monitoring.aop;

import com.example.backend.Security.dto.UserInfoDto;
import com.example.backend.monitoring.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingAspect {

    private final AuditLogService auditLogService;

    // Pointcuts
    @Pointcut("execution(* com.example.backend.Security.controller.UserInfoController.getUSerInfo(..))")
    public void loginPointcut() {}

    @Pointcut("execution(* com.example.backend.scan.controller.AppScanController.analyzeCode(..))")
    public void scanPointcut() {}

    @Pointcut("execution(* com.example.backend.scan.controller.ReportController.download*(..))")
    public void reportPointcut() {}

    @Pointcut("within(com.example.backend..controller..*)")
    public void allControllersPointcut() {}

    // Login Logging
    @Around("loginPointcut()")
    public Object logLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String username = "unknown";
        if (args.length > 0 && args[0] instanceof UserInfoDto) {
            username = ((UserInfoDto) args[0]).getUserName(); // Assume username is here
        }

        try {
            Object result = joinPoint.proceed();
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (response.getStatusCode().is2xxSuccessful()) {
                auditLogService.logAction(username, "LOGIN_SUCCESS", "/api/auth/login", "POST", response.getStatusCode().value(), "User logged in successfully", "INFO");
            } else {
                auditLogService.logAction(username, "LOGIN_FAILED", "/api/auth/login", "POST", response.getStatusCode().value(), "Login failed", "WARN");
            }
            return result;
        } catch (Exception e) {
            auditLogService.logAction(username, "LOGIN_ERROR", "/api/auth/login", "POST", 500, e.getMessage(), "ERROR");
            throw e;
        }
    }

    // Scan Logging
    @Around("scanPointcut()")
    public Object logScan(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = getCurrentUser();
        Object[] args = joinPoint.getArgs();
        String appType = args.length > 0 ? args[0].toString() : "unknown";
        String projectName = args.length > 2 ? args[2].toString() : "unknown";
        
        auditLogService.logAction(username, "SCAN_STARTED", "/api/v1/analyze/" + appType, "POST", 200, "Started scan for project: " + projectName, "INFO");
        
        try {
            Object result = joinPoint.proceed();
            auditLogService.logAction(username, "SCAN_FINISHED", "/api/v1/analyze/" + appType, "POST", 200, "Finished scan for project: " + projectName, "INFO");
            return result;
        } catch (Exception e) {
            auditLogService.logAction(username, "SCAN_FAILED", "/api/v1/analyze/" + appType, "POST", 500, "Scan failed: " + e.getMessage(), "ERROR");
            throw e;
        }
    }

    // Report Logging
    @AfterReturning(pointcut = "reportPointcut()", returning = "result")
    public void logReportGeneration(JoinPoint joinPoint, Object result) {
        String username = getCurrentUser();
        auditLogService.logAction(username, "GENERATE_REPORT", "/api/reports/...", "GET", 200, "Generated report", "INFO");
    }

    // Global Exception Logging
    @AfterThrowing(pointcut = "allControllersPointcut()", throwing = "exception")
    public void logExceptions(JoinPoint joinPoint, Throwable exception) {
        String username = getCurrentUser();
        String methodName = joinPoint.getSignature().getName();
        auditLogService.logAction(username, "SYSTEM_ERROR", methodName, "UNKNOWN", 500, exception.getMessage(), "ERROR");
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
