package com.example.backend.scan.service;

import com.example.backend.scan.dto.OwaspMappingResult;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OwaspMobileMappingService {

    // OWASP 2024 Categories
    public static final String M1_ID = "M1";
    public static final String M1_NAME = "Improper Credential Usage";

    public static final String M2_ID = "M2";
    public static final String M2_NAME = "Inadequate Supply Chain Security";

    public static final String M3_ID = "M3";
    public static final String M3_NAME = "Insecure Authentication and Authorization";

    public static final String M4_ID = "M4";
    public static final String M4_NAME = "Insufficient Input/Output Validation";

    public static final String M5_ID = "M5";
    public static final String M5_NAME = "Insecure Communication";

    public static final String M6_ID = "M6";
    public static final String M6_NAME = "Inadequate Privacy Controls";

    public static final String M7_ID = "M7";
    public static final String M7_NAME = "Insufficient Binary Protections";

    public static final String M8_ID = "M8";
    public static final String M8_NAME = "Security Misconfiguration";

    public static final String M9_ID = "M9";
    public static final String M9_NAME = "Insecure Data Storage";

    public static final String M10_ID = "M10";
    public static final String M10_NAME = "Insufficient Cryptography Key Management";

    private static final Map<String, String[]> OWASP_2024_KEYWORDS = new LinkedHashMap<>();
    
    static {
        OWASP_2024_KEYWORDS.put(M1_ID + " - " + M1_NAME, new String[]{"credential", "hardcoded", "password", "secret", "api key", "cwe-798", "cwe-259"});
        OWASP_2024_KEYWORDS.put(M2_ID + " - " + M2_NAME, new String[]{"supply chain", "third-party", "library", "dependency", "vulnerable lib", "cwe-1006"});
        OWASP_2024_KEYWORDS.put(M3_ID + " - " + M3_NAME, new String[]{"auth", "authentication", "authorization", "token", "session", "biometric", "cwe-287", "cwe-306", "cwe-862"});
        OWASP_2024_KEYWORDS.put(M4_ID + " - " + M4_NAME, new String[]{"injection", "validation", "input", "xss", "sql", "cwe-20", "cwe-89", "cwe-79", "cwe-116"});
        OWASP_2024_KEYWORDS.put(M5_ID + " - " + M5_NAME, new String[]{"ssl", "tls", "cleartext", "http", "certificate", "pinning", "cwe-295", "cwe-319", "cwe-326"});
        OWASP_2024_KEYWORDS.put(M6_ID + " - " + M6_NAME, new String[]{"privacy", "pii", "personal data", "leak", "log", "cwe-359", "cwe-532", "gdpr"});
        OWASP_2024_KEYWORDS.put(M7_ID + " - " + M7_NAME, new String[]{"reverse engineer", "obfuscat", "decompil", "debug", "anti-tamper", "cwe-693", "cwe-656"});
        OWASP_2024_KEYWORDS.put(M8_ID + " - " + M8_NAME, new String[]{"misconfigur", "debug mode", "backup", "permission", "manifest", "exported", "cwe-16", "cwe-732", "cwe-250"});
        OWASP_2024_KEYWORDS.put(M9_ID + " - " + M9_NAME, new String[]{"storage", "sqlite", "shared pref", "external storage", "unencrypt", "plaintext", "cwe-312", "cwe-922", "cwe-311"});
        OWASP_2024_KEYWORDS.put(M10_ID + " - " + M10_NAME, new String[]{"crypt", "weak algo", "md5", "sha1", "des", "ecb", "random", "cwe-327", "cwe-330", "cwe-326", "cwe-338"});
    }

    /**
     * Map a legacy MobSF category (OWASP 2016) and a CWE to an OWASP Mobile 2024 category.
     *
     * @param legacyCategory The raw category string from MobSF (e.g. "M4 Insecure Authentication")
     * @param cweId          The CWE ID (e.g. "CWE-89")
     * @param description    The description of the finding for keyword fallback
     * @return OwaspMappingResult containing the mapped values.
     */
    public OwaspMappingResult mapCategory(String legacyCategory, String cweId, String description) {
        String safeLegacy = legacyCategory != null ? legacyCategory.trim() : "";
        String lowerLegacy = safeLegacy.toLowerCase();
        
        // 1. Exact/Explicit Legacy Mapping (OWASP 2016 -> OWASP 2024)
        if (lowerLegacy.contains("m4 insecure authentication") || lowerLegacy.contains("m6 insecure authorization")) {
            return new OwaspMappingResult("2024", M3_ID, M3_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m8 code tampering") || lowerLegacy.contains("m9 reverse engineering")) {
            return new OwaspMappingResult("2024", M7_ID, M7_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m10 extraneous functionality") || lowerLegacy.contains("m1 improper platform usage")) {
            return new OwaspMappingResult("2024", M8_ID, M8_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m2 insecure data storage")) {
            return new OwaspMappingResult("2024", M9_ID, M9_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m3 insecure communication")) {
            return new OwaspMappingResult("2024", M5_ID, M5_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m5 insufficient cryptography")) {
            return new OwaspMappingResult("2024", M10_ID, M10_NAME, safeLegacy);
        }
        if (lowerLegacy.contains("m7 client code quality")) {
            return new OwaspMappingResult("2024", M4_ID, M4_NAME, safeLegacy);
        }

        // 2. Fallback to Keyword / CWE search (Dynamic Mapping)
        String searchString = (safeLegacy + " " + (cweId != null ? cweId : "") + " " + (description != null ? description : "")).toLowerCase();
        
        for (Map.Entry<String, String[]> entry : OWASP_2024_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (searchString.contains(kw)) {
                    String[] parts = entry.getKey().split(" - ", 2);
                    return new OwaspMappingResult("2024", parts[0], parts[1], safeLegacy.isEmpty() ? null : safeLegacy);
                }
            }
        }

        // 3. Default Fallback if completely unknown
        if (!safeLegacy.isEmpty() && safeLegacy.matches("(?i)^M[1-9]0?.*")) {
            return new OwaspMappingResult("2024", "Unknown", "Mapped from Legacy OWASP 2016", safeLegacy);
        }

        return new OwaspMappingResult("2024", "Unknown", "Unclassified", safeLegacy.isEmpty() ? null : safeLegacy);
    }
}
