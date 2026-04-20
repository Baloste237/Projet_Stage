"""
api/routes.py — Orchestrateur du pipeline SAST complet
POST /analyze : parse → règles → features → ML → CWE → CVSS → réponse JSON
"""
import logging
from fastapi import APIRouter, HTTPException, Request

from app.schemas.response import (
    AnalysisRequest, AnalysisResponse,
    MLResult, ASTInfo, CWEResult, CVSSResult,
    HealthResponse,
)
from app.services.parser          import parse_java
from app.services.features        import build_feature_vector, extract_ast_features, AST_FEATURE_ORDER
from app.services.cwe_mapper      import map_cwe
from app.services.cvss_calculator import calculate_all_cvss
from app.services.rules_engine    import RulesEngine

logger = logging.getLogger(__name__)
router = APIRouter()


# ─────────────────────────────────────────────────────────────────────────────
# Health check
# ─────────────────────────────────────────────────────────────────────────────
@router.get("/health", response_model=HealthResponse, tags=["Monitoring"])
def health(request: Request):
    """Vérifie que le modèle et le vectorizer sont chargés."""
    ml: "MLModel" = request.app.state.ml_model
    return HealthResponse(
        status="ok" if ml.model else "degraded",
        model_loaded=ml.model is not None,
        tfidf_loaded=ml.tfidf is not None,
        n_features=ml.model.n_features_in_ if ml.model else 0,
    )


# ─────────────────────────────────────────────────────────────────────────────
# Pipeline principal
# ─────────────────────────────────────────────────────────────────────────────
@router.post("/analyze", response_model=AnalysisResponse, tags=["SAST"])
def analyze(request: Request, body: AnalysisRequest):
    """
    Pipeline SAST complet (Hybride) :
      1. Parse AST (javalang)
      2. Moteur de Règles (Détection heuristique rapide)
      3. Extraction features (TF-IDF + AST → 2028 dims)
      4. Inférence ML (CalibratedClassifierCV / LinearSVC)
      5. Mapping CWE (Fusion ML + Règles)
      6. Calcul CVSS v3 (score ajusté)
    """
    ml = request.app.state.ml_model

    # ── Étape 1 : Parser ─────────────────────────────────────────────────────
    logger.info(f"[SAST] Parsing — fichier : {body.filename or '<anonymous>'}")
    parse_result = parse_java(body.code)

    # ── Étape 2 : Moteur de Règles (Hybride) ─────────────────────────────────
    rule_matches = RulesEngine.analyze(body.code, parse_result)
    
    if rule_matches:
        logger.info(f"[SAST] Règles Heuristiques : {len(rule_matches)} vulnérabilité(s) trouvée(s)")

    # ── Étape 3 : Features ───────────────────────────────────────────────────
    try:
        X = build_feature_vector(parse_result, ml.tfidf)
    except Exception as e:
        logger.error(f"[SAST] Erreur feature extraction : {e}")
        raise HTTPException(status_code=422, detail=f"Erreur extraction features : {e}")

    # ── Étape 4 : ML ─────────────────────────────────────────────────────────
    try:
        pred = ml.predict(X)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))

    ml_result = MLResult(
        is_vulnerable=pred['is_vulnerable'],
        label=pred['label'],
        probability_vulnerable=pred['probability_vulnerable'],
        probability_safe=pred['probability_safe'],
    )

    # ── Étape 5 : CWE Mapper ─────────────────────────────────────────────────
    ml_cwe_matches = map_cwe(parse_result, pred['probability_vulnerable'])
    
    # FUSION des trouvailles (Hybride)
    cwe_matches = rule_matches + ml_cwe_matches
    is_finally_vulnerable = pred['is_vulnerable'] or (len(rule_matches) > 0)

    # ── Étape 6 : CVSS ───────────────────────────────────────────────────────
    # On ajuste la probabilité à 1.0 si le moteur de règle a détecté une faille certaine
    adjusted_probability = 1.0 if rule_matches else pred['probability_vulnerable']
    cvss_scores = calculate_all_cvss(cwe_matches, adjusted_probability)

    # ── Construction de la réponse ────────────────────────────────────────────
    if not is_finally_vulnerable:
        overall_severity = 'None'
        top_score = None
    elif cvss_scores:
        top_score = cvss_scores[0].adjusted_score
        overall_severity = cvss_scores[0].severity
    else:
        top_score = None
        overall_severity = 'Unknown'

    # Info AST résumée
    ast_feats = extract_ast_features(parse_result)
    ast_info = ASTInfo(
        parse_error=parse_result.parse_error,
        total_nodes=int(ast_feats.get('total_nodes', 0)) or None,
        n_methods=int(ast_feats.get('n_methods', 0)) or None,
        n_sensitive_imports=int(ast_feats.get('n_sensitive_imports', 0)),
        cyclomatic_complexity=ast_feats.get('cyclomatic_complexity'),
        ast_depth=int(ast_feats.get('ast_depth', 0)) or None,
    )

    cwe_results = [
        CWEResult(
            cwe_id=m.cwe_id,
            name=m.name,
            description=m.description,
            confidence=m.confidence,
            evidence=m.evidence,
        )
        for m in cwe_matches
    ]

    cvss_results = [
        CVSSResult(
            cwe_id=s.cwe_id,
            base_score=s.base_score,
            adjusted_score=s.adjusted_score,
            severity=s.severity,
            vector_string=s.vector_string,
            rationale=s.rationale,
        )
        for s in cvss_scores
    ]

    logger.info(
        f"[SAST] Résultat final : {'VULNERABLE' if is_finally_vulnerable else 'SAFE'} | "
        f"proba_ML={pred['probability_vulnerable']:.3f} | "
        f"CWEs={[m.cwe_id for m in cwe_matches]} | "
        f"CVSS={top_score}"
    )

    return AnalysisResponse(
        is_vulnerable=is_finally_vulnerable,
        overall_severity=overall_severity,
        top_cvss_score=top_score,
        ml=ml_result,
        ast=ast_info,
        cwe_findings=cwe_results,
        cvss_scores=cvss_results,
    )