package com.example.backend.scan.dto.ia;

public class IaAnalysisRequestDTO {
    private String code;
    private String filename;

    public IaAnalysisRequestDTO() {}

    public IaAnalysisRequestDTO(String code, String filename) {
        this.code = code;
        this.filename = filename;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
