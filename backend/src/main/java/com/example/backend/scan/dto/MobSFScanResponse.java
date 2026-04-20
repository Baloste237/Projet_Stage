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
    private CodeAnalysis codeAnalysis;

    @JsonProperty("manifest_analysis")
    private List<ManifestIssue> manifestAnalysis;

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
    public static class CodeAnalysis {
        @JsonProperty("findings")
        private Map<String, Finding> findings;

        @Data
        public static class Finding {
            private String level;       // high / warning / info / secure
            private List<String> files;
            private String description;
            private String cwe;
            private String owasp;
        }
    }

    @Data
    public static class ManifestIssue {
        private String rule;
        private String title;
        private String severity;    // high / warning / info
        private String description;
        private String cwe;
        private String owasp;
    }
}
