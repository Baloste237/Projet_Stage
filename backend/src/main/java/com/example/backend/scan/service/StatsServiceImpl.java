package com.example.backend.scan.service;

import com.example.backend.scan.dto.stats.*;
import com.example.backend.scan.entity.MobileAppScan;
import com.example.backend.scan.entity.SeverityEnum;
import com.example.backend.scan.entity.Vulnerabilite;
import com.example.backend.scan.entity.WebAppScan;
import com.example.backend.scan.repository.MobileAppScanRepository;
import com.example.backend.scan.repository.VulnerabiliteRepository;
import com.example.backend.scan.repository.WebAppScanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service de statistiques.
 * Calcule les métriques OWASP Web et Mobile depuis la base de données.
 */
@Service
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    private final VulnerabiliteRepository vulnRepo;
    private final WebAppScanRepository   webRepo;
    private final MobileAppScanRepository mobileRepo;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter ISO_FMT   = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ── Mapping OWASP Web ──────────────────────────────────────────────────
    // Catégories couvertes par le moteur SAST IA
    private static final Map<String, String[]> OWASP_WEB_KEYWORDS = new LinkedHashMap<>();
    static {
        OWASP_WEB_KEYWORDS.put("A03 - Injection",
                new String[]{"injection","sql","sqli","xss","cross-site","xpath",
                             "command","ldap","nosql","template","cwe-89","cwe-79",
                             "cwe-78","cwe-91","cwe-643"});
        OWASP_WEB_KEYWORDS.put("A07 - Authentication Failures",
                new String[]{"auth","credential","password","session","token",
                             "brute","jwt","login","cwe-287","cwe-306","cwe-521",
                             "cwe-798","cwe-307"});
        OWASP_WEB_KEYWORDS.put("A01 - Broken Access Control",
                new String[]{"access control","authorization","privilege","path traversal",
                             "idor","directory","traversal","cwe-22","cwe-284",
                             "cwe-639","cwe-732","cwe-862"});
        OWASP_WEB_KEYWORDS.put("A02 - Cryptographic Failures",
                new String[]{"crypt","hash","ssl","tls","cipher","aes","rsa","md5",
                             "sha1","weak","entropy","cwe-326","cwe-327","cwe-330",
                             "cwe-295","cwe-311"});
        OWASP_WEB_KEYWORDS.put("A10 - Mishandling of Exceptional Conditions",
                new String[]{"exception","error handling","null pointer","uncaught",
                             "stack trace","disclosure","cwe-755","cwe-390",
                             "cwe-388","cwe-252","cwe-476"});
    }

    // ── Mapping OWASP Mobile Top 10 2024 ──────────────────────────────────
    private static final Map<String, String[]> OWASP_MOBILE_KEYWORDS = new LinkedHashMap<>();
    static {
        OWASP_MOBILE_KEYWORDS.put("M1 - Improper Credential Usage",
                new String[]{"credential","hardcoded","password","secret","api key",
                             "cwe-798","cwe-259"});
        OWASP_MOBILE_KEYWORDS.put("M2 - Inadequate Supply Chain Security",
                new String[]{"supply chain","third-party","library","dependency",
                             "vulnerable lib","cwe-1006"});
        OWASP_MOBILE_KEYWORDS.put("M3 - Insecure Authentication/Authorization",
                new String[]{"auth","authentication","authorization","token","session",
                             "biometric","cwe-287","cwe-306","cwe-862"});
        OWASP_MOBILE_KEYWORDS.put("M4 - Insufficient Input/Output Validation",
                new String[]{"injection","validation","input","xss","sql","cwe-20",
                             "cwe-89","cwe-79","cwe-116"});
        OWASP_MOBILE_KEYWORDS.put("M5 - Insecure Communication",
                new String[]{"ssl","tls","cleartext","http","certificate","pinning",
                             "cwe-295","cwe-319","cwe-326"});
        OWASP_MOBILE_KEYWORDS.put("M6 - Inadequate Privacy Controls",
                new String[]{"privacy","pii","personal data","leak","log","cwe-359",
                             "cwe-532","gdpr"});
        OWASP_MOBILE_KEYWORDS.put("M7 - Insufficient Binary Protections",
                new String[]{"reverse engineer","obfuscat","decompil","debug","anti-tamper",
                             "cwe-693","cwe-656"});
        OWASP_MOBILE_KEYWORDS.put("M8 - Security Misconfiguration",
                new String[]{"misconfigur","debug mode","backup","permission","manifest",
                             "exported","cwe-16","cwe-732","cwe-250"});
        OWASP_MOBILE_KEYWORDS.put("M9 - Insecure Data Storage",
                new String[]{"storage","sqlite","shared pref","external storage","unencrypt",
                             "plaintext","cwe-312","cwe-922","cwe-311"});
        OWASP_MOBILE_KEYWORDS.put("M10 - Insufficient Cryptography",
                new String[]{"crypt","weak algo","md5","sha1","des","ecb","random",
                             "cwe-327","cwe-330","cwe-326","cwe-338"});
    }

    public StatsServiceImpl(VulnerabiliteRepository vulnRepo,
                            WebAppScanRepository webRepo,
                            MobileAppScanRepository mobileRepo) {
        this.vulnRepo   = vulnRepo;
        this.webRepo    = webRepo;
        this.mobileRepo = mobileRepo;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WEB STATS
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public StatsWebDTO getWebStats() {
        log.info("[Stats] Calcul des statistiques web OWASP");

        StatsWebDTO dto = new StatsWebDTO();

        long total    = vulnRepo.countWebVulnerabilities();
        long critical = vulnRepo.countWebBySeverity(SeverityEnum.CRITICAL);
        long high     = vulnRepo.countWebBySeverity(SeverityEnum.HIGH);
        long medium   = vulnRepo.countWebBySeverity(SeverityEnum.MEDIUM);
        long low      = vulnRepo.countWebBySeverity(SeverityEnum.LOW);

        dto.setTotalVulnerabilities(total);
        dto.setCriticalCount(critical);
        dto.setHighCount(high);
        dto.setMediumCount(medium);
        dto.setLowCount(low);
        dto.setTotalScans(webRepo.count());

        // Projet le plus vulnérable
        List<String> top = vulnRepo.findMostVulnerableWebProject();
        dto.setMostVulnerableProject(top.isEmpty() ? "N/A" : top.get(0));

        // Dernière date de scan
        LocalDateTime lastDate = vulnRepo.findLastWebScanDate();
        dto.setLastScanDate(lastDate != null ? lastDate.format(ISO_FMT) : null);

        // Catégories OWASP
        List<Vulnerabilite> allWebVulns = vulnRepo.findAllWebVulnerabilities();
        dto.setOwaspCategories(buildOwaspWebCategories(allWebVulns, total));

        // Tendance mensuelle (12 derniers mois)
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        List<Object[]> trendRows = vulnRepo.findWebMonthlyTrend(since);
        dto.setScanTrend(mapTrend(trendRows));

        return dto;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MOBILE STATS
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public StatsMobileDTO getMobileStats() {
        log.info("[Stats] Calcul des statistiques mobile OWASP");

        StatsMobileDTO dto = new StatsMobileDTO();

        long total    = vulnRepo.countMobileVulnerabilities();
        long critical = vulnRepo.countMobileBySeverity(SeverityEnum.CRITICAL);
        long high     = vulnRepo.countMobileBySeverity(SeverityEnum.HIGH);
        long medium   = vulnRepo.countMobileBySeverity(SeverityEnum.MEDIUM);
        long low      = vulnRepo.countMobileBySeverity(SeverityEnum.LOW);

        dto.setTotalVulnerabilities(total);
        dto.setCriticalCount(critical);
        dto.setHighCount(high);
        dto.setMediumCount(medium);
        dto.setLowCount(low);
        dto.setTotalApkAnalyzed(mobileRepo.count());

        // Score de risque moyen (CVSS normalisé sur 10 → /10)
        Double avgCvss = vulnRepo.findAverageMobileCvssScore();
        dto.setAverageRiskScore(avgCvss != null ? Math.round(avgCvss * 10.0) / 10.0 : 0.0);

        // Projet le plus vulnérable
        List<String> top = vulnRepo.findMostVulnerableMobileProject();
        dto.setMostVulnerableProject(top.isEmpty() ? "N/A" : top.get(0));

        // Dernière date de scan
        LocalDateTime lastDate = vulnRepo.findLastMobileScanDate();
        dto.setLastScanDate(lastDate != null ? lastDate.format(ISO_FMT) : null);

        // Catégories OWASP Mobile
        List<Vulnerabilite> allMobileVulns = vulnRepo.findAllMobileVulnerabilities();
        dto.setOwaspCategories(buildOwaspMobileCategories(allMobileVulns, total));

        // Tendance mensuelle
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        List<Object[]> trendRows = vulnRepo.findMobileMonthlyTrend(since);
        dto.setScanTrend(mapTrend(trendRows));

        return dto;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DASHBOARD GLOBAL
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public DashboardStatsDTO getDashboardStats() {
        log.info("[Stats] Calcul du résumé dashboard global");

        DashboardStatsDTO dto = new DashboardStatsDTO();

        dto.setTotalWebVulnerabilities(vulnRepo.countWebVulnerabilities());
        dto.setTotalMobileVulnerabilities(vulnRepo.countMobileVulnerabilities());
        dto.setTotalWebScans(webRepo.count());
        dto.setTotalMobileScans(mobileRepo.count());
        dto.setTotalCritical(
            vulnRepo.countWebBySeverity(SeverityEnum.CRITICAL) +
            vulnRepo.countMobileBySeverity(SeverityEnum.CRITICAL)
        );
        dto.setTotalHigh(
            vulnRepo.countWebBySeverity(SeverityEnum.HIGH) +
            vulnRepo.countMobileBySeverity(SeverityEnum.HIGH)
        );

        LocalDateTime lastWeb = vulnRepo.findLastWebScanDate();
        LocalDateTime lastMob = vulnRepo.findLastMobileScanDate();
        dto.setLastWebScanDate(lastWeb != null ? lastWeb.format(ISO_FMT) : null);
        dto.setLastMobileScanDate(lastMob != null ? lastMob.format(ISO_FMT) : null);

        return dto;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ══════════════════════════════════════════════════════════════════════

    /** Mappe les vulnérabilités web vers les catégories OWASP définies */
    private List<OwaspCategoryDTO> buildOwaspWebCategories(List<Vulnerabilite> vulns, long totalVulns) {
        List<OwaspCategoryDTO> result = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : OWASP_WEB_KEYWORDS.entrySet()) {
            String   catName    = entry.getKey();
            String[] keywords   = entry.getValue();
            String   catId      = catName.substring(0, 3);  // "A03", "A07"…

            List<Vulnerabilite> matched = vulns.stream()
                .filter(v -> matchesKeywords(v, keywords))
                .collect(Collectors.toList());

            long count    = matched.size();
            double pct    = totalVulns > 0 ? Math.round((count * 1000.0 / totalVulns)) / 10.0 : 0.0;
            long critical = countBySeverity(matched, SeverityEnum.CRITICAL);
            long high     = countBySeverity(matched, SeverityEnum.HIGH);
            long medium   = countBySeverity(matched, SeverityEnum.MEDIUM);
            long low      = countBySeverity(matched, SeverityEnum.LOW);
            String last   = getLastDetected(matched);

            result.add(new OwaspCategoryDTO(catId, catName, count, pct,
                    critical, high, medium, low, last));
        }
        return result;
    }

    /** Mappe les vulnérabilités mobiles vers OWASP Mobile Top 10 2024 */
    private List<OwaspCategoryDTO> buildOwaspMobileCategories(List<Vulnerabilite> vulns, long totalVulns) {
        List<OwaspCategoryDTO> result = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : OWASP_MOBILE_KEYWORDS.entrySet()) {
            String   catName  = entry.getKey();
            String   catId    = catName.substring(0, 2);  // "M1"…"M10"
            int dashIdx = catName.indexOf(' ');
            catId = dashIdx > 0 ? catName.substring(0, dashIdx).trim() : catId;
            final String finalCatId = catId;

            List<Vulnerabilite> matched = vulns.stream()
                .filter(v -> finalCatId.equals(v.getOwaspId()))
                .collect(Collectors.toList());

            long count    = matched.size();
            double pct    = totalVulns > 0 ? Math.round((count * 1000.0 / totalVulns)) / 10.0 : 0.0;
            long critical = countBySeverity(matched, SeverityEnum.CRITICAL);
            long high     = countBySeverity(matched, SeverityEnum.HIGH);
            long medium   = countBySeverity(matched, SeverityEnum.MEDIUM);
            long low      = countBySeverity(matched, SeverityEnum.LOW);
            String last   = getLastDetected(matched);

            result.add(new OwaspCategoryDTO(catId, catName, count, pct,
                    critical, high, medium, low, last));
        }
        return result;
    }

    /** Vérifie si une vulnérabilité correspond à un ou plusieurs mots-clés */
    private boolean matchesKeywords(Vulnerabilite v, String[] keywords) {
        String combined = buildSearchString(v).toLowerCase();
        for (String kw : keywords) {
            if (combined.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private String buildSearchString(Vulnerabilite v) {
        StringBuilder sb = new StringBuilder();
        if (v.getType()        != null) sb.append(v.getType()).append(" ");
        if (v.getDescription() != null) sb.append(v.getDescription()).append(" ");
        if (v.getCweId()       != null) sb.append(v.getCweId()).append(" ");
        if (v.getOwaspcat()    != null) sb.append(v.getOwaspcat()).append(" ");
        return sb.toString();
    }

    private long countBySeverity(List<Vulnerabilite> list, SeverityEnum sev) {
        return list.stream().filter(v -> sev.equals(v.getNiv_grav())).count();
    }

    private String getLastDetected(List<Vulnerabilite> matched) {
        return matched.stream()
            .map(v -> v.getScan() != null ? v.getScan().getCreatedAt() : null)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .map(d -> d.format(ISO_FMT))
            .orElse(null);
    }

    /** Convertit les rows JPQL (period, crit, high, med, low, scans) en DTOs */
    private List<ScanTrendDTO> mapTrend(List<Object[]> rows) {
        List<ScanTrendDTO> list = new ArrayList<>();
        for (Object[] row : rows) {
            String period = row[0] != null ? row[0].toString() : "";
            long   crit   = toLong(row[1]);
            long   high   = toLong(row[2]);
            long   med    = toLong(row[3]);
            long   low    = toLong(row[4]);
            long   scans  = toLong(row[5]);
            list.add(new ScanTrendDTO(period, crit, high, med, low, scans));
        }
        return list;
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return 0L;
    }
}
