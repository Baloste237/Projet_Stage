"""
schemas/response.py — Schémas Pydantic de la réponse API
"""
from pydantic import BaseModel, Field
from typing import List, Optional


class CWEResult(BaseModel):
    cwe_id: str = Field(examples=["CWE-89"])
    name: str = Field(examples=["SQL Injection"])
    description: str
    confidence: float = Field(ge=0.0, le=1.0, examples=[0.85])
    evidence: str


class CVSSResult(BaseModel):
    cwe_id: str
    base_score: float = Field(ge=0.0, le=10.0, examples=[9.8])
    adjusted_score: float = Field(ge=0.0, le=10.0, examples=[8.3])
    severity: str = Field(examples=["Critical"])
    vector_string: str = Field(examples=["CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H"])
    rationale: str


class MLResult(BaseModel):
    is_vulnerable: bool
    label: int = Field(description="0 = safe, 1 = vulnerable")
    probability_vulnerable: float = Field(ge=0.0, le=1.0)
    probability_safe: float = Field(ge=0.0, le=1.0)


class ASTInfo(BaseModel):
    parse_error: bool
    total_nodes: Optional[int] = None
    n_methods: Optional[int] = None
    n_sensitive_imports: Optional[int] = None
    cyclomatic_complexity: Optional[float] = None
    ast_depth: Optional[int] = None


class AnalysisResponse(BaseModel):
    """Réponse complète du pipeline SAST."""

    # Résumé
    is_vulnerable: bool
    overall_severity: str = Field(
        description="None / Low / Medium / High / Critical",
        examples=["Critical"]
    )
    top_cvss_score: Optional[float] = Field(
        default=None,
        description="Score CVSS ajusté le plus élevé parmi les vulnérabilités détectées."
    )

    # Résultats par étape
    ml: MLResult
    ast: ASTInfo
    cwe_findings: List[CWEResult]
    cvss_scores: List[CVSSResult]

    # Métadonnées
    pipeline_version: str = "1.0.0"


class AnalysisRequest(BaseModel):
    """Corps de la requête POST /analyze."""
    code: str = Field(
        description="Code source Java à analyser.",
        min_length=1,
        examples=['String query = "SELECT * FROM users WHERE id=" + userId;']
    )
    filename: Optional[str] = Field(
        default=None,
        description="Nom du fichier (optionnel, pour contexte).",
        examples=["UserDAO.java"]
    )


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    tfidf_loaded: bool
    n_features: int