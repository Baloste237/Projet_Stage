package com.example.backend.scan.dto.ia;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IaAnalysisResponseDTO {
    @JsonProperty("is_vulnerable")
    private boolean isVulnerable;
    
    @JsonProperty("overall_severity")
    private String overallSeverity;
    
    @JsonProperty("top_cvss_score")
    private Double topCvssScore;

    @JsonProperty("ml")
    private IaMlResult ml;
    
    @JsonProperty("ast")
    private IaAstInfo ast;

    @JsonProperty("cwe_findings")
    private List<IaCweResult> cweFindings;

    @JsonProperty("cvss_scores")
    private List<IaCvssResult> cvssScores;

    public boolean isVulnerable() { return isVulnerable; }
    public void setVulnerable(boolean vulnerable) { isVulnerable = vulnerable; }

    public String getOverallSeverity() { return overallSeverity; }
    public void setOverallSeverity(String overallSeverity) { this.overallSeverity = overallSeverity; }

    public Double getTopCvssScore() { return topCvssScore; }
    public void setTopCvssScore(Double topCvssScore) { this.topCvssScore = topCvssScore; }

    public IaMlResult getMl() { return ml; }
    public void setMl(IaMlResult ml) { this.ml = ml; }

    public IaAstInfo getAst() { return ast; }
    public void setAst(IaAstInfo ast) { this.ast = ast; }

    public List<IaCweResult> getCweFindings() { return cweFindings; }
    public void setCweFindings(List<IaCweResult> cweFindings) { this.cweFindings = cweFindings; }

    public List<IaCvssResult> getCvssScores() { return cvssScores; }
    public void setCvssScores(List<IaCvssResult> cvssScores) { this.cvssScores = cvssScores; }

    public static class IaMlResult {
        @JsonProperty("is_vulnerable")
        private boolean isVulnerable;
        private int label;
        @JsonProperty("probability_vulnerable")
        private double probabilityVulnerable;
        @JsonProperty("probability_safe")
        private double probabilitySafe;

        public boolean isVulnerable() { return isVulnerable; }
        public void setVulnerable(boolean vulnerable) { isVulnerable = vulnerable; }
        public int getLabel() { return label; }
        public void setLabel(int label) { this.label = label; }
        public double getProbabilityVulnerable() { return probabilityVulnerable; }
        public void setProbabilityVulnerable(double probabilityVulnerable) { this.probabilityVulnerable = probabilityVulnerable; }
        public double getProbabilitySafe() { return probabilitySafe; }
        public void setProbabilitySafe(double probabilitySafe) { this.probabilitySafe = probabilitySafe; }
    }

    public static class IaAstInfo {
        @JsonProperty("parse_error")
        private boolean parseError;

        public boolean isParseError() { return parseError; }
        public void setParseError(boolean parseError) { this.parseError = parseError; }
    }

    public static class IaCweResult {
        @JsonProperty("cwe_id")
        private String cweId;
        private String name;
        private String description;
        private double confidence;
        private String evidence;

        public String getCweId() { return cweId; }
        public void setCweId(String cweId) { this.cweId = cweId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getEvidence() { return evidence; }
        public void setEvidence(String evidence) { this.evidence = evidence; }
    }

    public static class IaCvssResult {
        @JsonProperty("cwe_id")
        private String cweId;
        @JsonProperty("base_score")
        private double baseScore;
        @JsonProperty("adjusted_score")
        private double adjustedScore;
        private String severity;
        @JsonProperty("vector_string")
        private String vectorString;
        private String rationale;

        public String getCweId() { return cweId; }
        public void setCweId(String cweId) { this.cweId = cweId; }
        public double getBaseScore() { return baseScore; }
        public void setBaseScore(double baseScore) { this.baseScore = baseScore; }
        public double getAdjustedScore() { return adjustedScore; }
        public void setAdjustedScore(double adjustedScore) { this.adjustedScore = adjustedScore; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getVectorString() { return vectorString; }
        public void setVectorString(String vectorString) { this.vectorString = vectorString; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
    }
}
