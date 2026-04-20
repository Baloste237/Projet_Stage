"""
main.py — Point d'entrée FastAPI
Gère le cycle de vie : chargement modèle au démarrage, nettoyage à l'arrêt.
"""
import logging
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes    import router
from app.services.ml_model import MLModel

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s — %(message)s",
)
logger = logging.getLogger(__name__)

# ── Chemins des artefacts ML ──────────────────────────────────────────────────
ML_DIR        = Path(__file__).parent / "ml"
MODEL_PATH    = ML_DIR / "svm_best_model.pkl"
TFIDF_PATH    = ML_DIR / "tfidf_ast_java.pkl"


# ── Lifespan : chargement unique au démarrage ─────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("=== Démarrage du serveur SAST ===")
    ml = MLModel(model_path=str(MODEL_PATH), tfidf_path=str(TFIDF_PATH))
    try:
        ml.load()
        logger.info(f"Modèle chargé : {ml.model.n_features_in_} features attendues.")
    except FileNotFoundError as e:
        logger.error(str(e))
        logger.error(
            "IMPORTANT : placez les fichiers suivants dans app/ml/ :\n"
            "  - svm_best_model.pkl\n"
            "  - tfidf_ast_java.pkl  (généré par le notebook)"
        )
    app.state.ml_model = ml
    yield
    logger.info("=== Arrêt du serveur SAST ===")


# ── Application FastAPI ───────────────────────────────────────────────────────
app = FastAPI(
    title="SAST API — Analyse de vulnérabilités Java",
    description=(
        "Pipeline d'analyse statique de sécurité (SAST) pour code Java.\n\n"
        "**Pipeline** : `parse (AST)` → `features (TF-IDF + AST)` → "
        "`ML (SVM)` → `CWE mapping` → `CVSS v3 scoring`"
    ),
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router, prefix="/api/v1")


@app.get("/", tags=["Root"])
async def root():
    return {
        "service": "SAST API",
        "version": "1.0.0",
        "docs": "/docs",
        "analyze_endpoint": "POST /api/v1/analyze",
    }