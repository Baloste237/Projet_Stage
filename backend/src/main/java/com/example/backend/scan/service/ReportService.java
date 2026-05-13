package com.example.backend.scan.service;

public interface ReportService {
    /**
     * Genere le rapport JSON d'un scan specifique
     */
    String generateReportJson(Long scanId) throws com.fasterxml.jackson.core.JsonProcessingException;
    
    /**
     * Genere le rapport PDF d'un scan specifique
     */
    byte[] generateReportPdf(Long scanId) throws Exception;
}
