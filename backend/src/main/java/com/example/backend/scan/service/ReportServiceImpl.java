package com.example.backend.scan.service;

import com.example.backend.scan.entity.AbstractScan;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.repository.AbstractScanRepository;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final AbstractScanRepository abstractScanRepository;
    private final VulnerabiliteRepository vulnerabiliteRepository;

    public ReportServiceImpl(AbstractScanRepository abstractScanRepository, VulnerabiliteRepository vulnerabiliteRepository) {
        this.abstractScanRepository = abstractScanRepository;
        this.vulnerabiliteRepository = vulnerabiliteRepository;
    }

    @Override
    public String generateReportJson(Long scanId) throws JsonProcessingException {
        log.info("Début de génération du rapport JSON pour le scan ID: {}", scanId);
        
        AbstractScan scan = abstractScanRepository.findById(scanId)
                .orElseThrow(() -> {
                    log.error("Scan introuvable pour la génération JSON. ID: {}", scanId);
                    return new RuntimeException("Scan not found with id : " + scanId);
                });
        
        List<Vulnerabilite> vulnerabilites = vulnerabiliteRepository.findByScanId(scanId);
        if (vulnerabilites.isEmpty()) {
            log.warn("Aucune vulnérabilité trouvée pour le scan ID: {}. Le rapport sera généré avec une liste vide.", scanId);
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("scanId", scan.getId());
        reportData.put("projectName", scan.getProjectName());
        reportData.put("fileName", scan.getFileName());
        reportData.put("scanType", scan.getScanType());
        reportData.put("status", scan.getStatus());
        reportData.put("createdAt", scan.getCreatedAt() != null ? scan.getCreatedAt().toString() : null);
        reportData.put("completedAt", scan.getCompletedAt() != null ? scan.getCompletedAt().toString() : null);
        
        // KPIs
        Map<String, Integer> kpis = new HashMap<>();
        kpis.put("totalVulnerabilities", scan.getTotalVulnerabilities());
        kpis.put("critical", scan.getCriticalCount());
        kpis.put("high", scan.getHighCount());
        kpis.put("medium", scan.getMediumCount());
        kpis.put("low", scan.getLowCount());
        reportData.put("kpi", kpis);

        // List vulnerabilities
        reportData.put("vulnerabilities", vulnerabilites.stream().map(v -> {
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("type", v.getType());
            vMap.put("description", v.getDescription());
            vMap.put("severity", v.getNiv_grav());
            vMap.put("cweId", v.getCweId());
            vMap.put("cvssScore", v.getCvssScore());
            vMap.put("owaspCategory", v.getOwaspcat());
            vMap.put("legacyCategory", v.getLegacyCategory());
            vMap.put("owaspVersion", v.getOwaspVersion());
            vMap.put("owaspId", v.getOwaspId());
            vMap.put("owaspName", v.getOwaspName());
            vMap.put("targetFile", v.getTargetFile());
            vMap.put("targetLine", v.getTargetLine());
            return vMap;
        }).toList());

        ObjectMapper mapper = new ObjectMapper();
        log.info("Rapport JSON pour scan ID {} généré avec succès.", scanId);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportData);
    }

    @Override
    public byte[] generateReportPdf(Long scanId) throws Exception {
        log.info("Début de génération du rapport PDF pour le scan ID: {}", scanId);
        
        AbstractScan scan = abstractScanRepository.findById(scanId)
                .orElseThrow(() -> {
                    log.error("Scan introuvable pour la génération PDF. ID: {}", scanId);
                    return new RuntimeException("Scan not found with id : " + scanId);
                });
        
        List<Vulnerabilite> vulnerabilites = vulnerabiliteRepository.findByScanId(scanId);

        Document document = new Document(PageSize.A4.rotate()); // Landscape format works better for huge tables
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        Paragraph title = new Paragraph("Rapport de Sécurité SAST - AppShield Pro", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(Chunk.NEWLINE);

        // Project Info
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        
        document.add(new Paragraph("Informations Générales", headingFont));
        document.add(new Paragraph("Projet : " + scan.getProjectName(), normalFont));
        document.add(new Paragraph("Fichier Cible : " + scan.getFileName(), normalFont));
        document.add(new Paragraph("Type : " + scan.getScanType(), normalFont));
        document.add(new Paragraph("Statut : " + scan.getStatus(), normalFont));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        if (scan.getCompletedAt() != null) {
            document.add(new Paragraph("Date d'analyse : " + scan.getCompletedAt().format(dtf), normalFont));
        }

        document.add(Chunk.NEWLINE);

        // KPI Summary
        document.add(new Paragraph("Résumé des Vulnérabilités (KPIs)", headingFont));
        document.add(new Paragraph("Total : " + scan.getTotalVulnerabilities(), normalFont));
        document.add(new Paragraph("CRITICAL : " + scan.getCriticalCount(), normalFont));
        document.add(new Paragraph("HIGH : " + scan.getHighCount(), normalFont));
        document.add(new Paragraph("MEDIUM : " + scan.getMediumCount(), normalFont));
        document.add(new Paragraph("LOW : " + scan.getLowCount(), normalFont));
        
        document.add(Chunk.NEWLINE);

        // Vulnerabilities Table
        document.add(new Paragraph("Détails des Vulnérabilités", headingFont));
        document.add(Chunk.NEWLINE);

        if (vulnerabilites.isEmpty()) {
            document.add(new Paragraph("Félicitations, aucune vulnérabilité détectée.", normalFont));
        } else {
            PdfPTable table = new PdfPTable(6); 
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.2f, 1f, 3.5f, 1f, 2f});

            addTableHeader(table, "Catégorie", "Sévérité", "CVSS", "Description", "CWE", "Fichier/Ligne");

            for (Vulnerabilite v : vulnerabilites) {
                String catStr = v.getOwaspVersion() != null 
                    ? "OWASP " + v.getOwaspVersion() + ":\n" + v.getOwaspId() + " - " + v.getOwaspName() + "\n(Détection: " + v.getLegacyCategory() + ")" 
                    : (v.getOwaspcat() != null ? v.getOwaspcat() : v.getType());
                table.addCell(getNormalCell(catStr));
                table.addCell(getSeverityCell(v.getNiv_grav() != null ? v.getNiv_grav().name() : "N/A"));
                table.addCell(getNormalCell(v.getCvssScore() != null ? v.getCvssScore().toString() : "N/A"));
                table.addCell(getNormalCell(v.getDescription()));
                table.addCell(getNormalCell(v.getCweId()));
                
                String targetInfo = (v.getTargetFile() != null ? v.getTargetFile() : "Inconnu") + 
                                    (v.getTargetLine() != null ? " (L:" + v.getTargetLine() + ")" : "");
                table.addCell(getNormalCell(targetInfo));
            }

            document.add(table);
        }
        
        document.close();
        log.info("Rapport PDF pour scan ID {} généré avec succès.", scanId);
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell(new Phrase(headerTitle, headerFont));
            header.setBackgroundColor(new BaseColor(41, 128, 185)); // Bleu 
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            table.addCell(header);
        }
    }

    private PdfPCell getNormalCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(5);
        return cell;
    }
    
    private PdfPCell getSeverityCell(String severity) {
        PdfPCell cell = new PdfPCell(new Phrase(severity, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            cell.setBackgroundColor(new BaseColor(255, 204, 204));
        } else if ("HIGH".equalsIgnoreCase(severity)) {
            cell.setBackgroundColor(new BaseColor(255, 229, 204));
        } else if ("MEDIUM".equalsIgnoreCase(severity)) {
            cell.setBackgroundColor(new BaseColor(255, 255, 204));
        } else if ("LOW".equalsIgnoreCase(severity)) {
            cell.setBackgroundColor(new BaseColor(204, 255, 204));
        }
        return cell;
    }
}
