package com.example.backend.scan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MobSFUploadResponse {
    @JsonProperty("analyzer")
    private String analyzer;

    @JsonProperty("status")
    private String status;

    @JsonProperty("hash")
    private String hash;          // Hash MD5 de l'APK — identifiant unique MobSF

    @JsonProperty("scan_type")
    private String scanType;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("app_name")
    private String appName;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("version_name")
    private String versionName;
}
