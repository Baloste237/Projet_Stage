package com.example.backend.scan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MobSFScanResponse {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("version_name")
    private String versionName;

    @JsonProperty("min_sdk")
    private String minSdk;

    @JsonProperty("target_sdk")
    private String targetSdk;

    @JsonProperty("permissions")
    private Map<String, PermissionDetail> permissions;

    @JsonProperty("android_api")
    private Map<String, Object> androidApi;

    @JsonProperty("code_analysis")
    private Object codeAnalysis;

    @JsonProperty("manifest_analysis")
    private Object manifestAnalysis;

    @JsonProperty("security_score")
    private Integer securityScore;

    @JsonProperty("average_cvss")
    private Double averageCvss;

    // Classes internes
    @Data
    public static class PermissionDetail {
        private String status;     // dangerous / normal / signature
        private String info;
        private String description;
    }

    @Data
    public static class Finding {
        private String level;       // high / warning / info / secure
        private String severity;
        private Object files;
        private String description;
        private String desc;
        private String cwe;
        private String owasp;
        @JsonProperty("cvss")
        private Double cvss;

        public String getEffectiveSeverity() {
            return (severity != null) ? severity : level;
        }
        public String getEffectiveDescription() {
            return (description != null) ? description : desc;
        }
        public String getEffectiveCwe() {
            if (cwe != null && !cwe.trim().isEmpty()) return cwe;
            if (owasp != null && !owasp.trim().isEmpty()) return owasp;
            return "N/A";
        }
    }

    @Data
    public static class ManifestIssue {
        private String rule;
        private String title;
        private String name;
        private String severity;    // high / warning / info
        private String stat;
        private String description;
        private String desc;
        private String cwe;
        private String owasp;

        public String getEffectiveSeverity() {
            return (severity != null) ? severity : stat;
        }
        public String getEffectiveDescription() {
            return (description != null) ? description : desc;
        }
        public String getEffectiveTitle() {
            return (title != null) ? title : name;
        }
        public String getEffectiveCwe() {
            if (cwe != null && !cwe.trim().isEmpty()) return cwe;
            if (owasp != null && !owasp.trim().isEmpty()) return owasp;
            return "N/A";
        }
    }
}
