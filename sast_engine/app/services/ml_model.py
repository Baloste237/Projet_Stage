"""
ml_model.py — Étape 3 du pipeline SAST
Chargement du modèle SVM et inférence.

Modèle : CalibratedClassifierCV(LinearSVC, method='sigmoid', cv=3)
Classes : [0 = safe, 1 = vulnerable]
"""
import pickle
import logging
from pathlib import Path

import numpy as np
import scipy.sparse

logger = logging.getLogger(__name__)


class MLModel:
    """
    Wrapper autour du CalibratedClassifierCV sauvegardé.
    Chargement unique au démarrage de l'app (singleton via lifespan).
    """

    def __init__(self, model_path: str, tfidf_path: str):
        self.model_path = Path(model_path)
        self.tfidf_path = Path(tfidf_path)
        self.model = None
        self.tfidf = None

    def load(self):
        """Charge le modèle SVM et le vectorizer TF-IDF depuis le disque."""
        logger.info(f"Chargement du modèle : {self.model_path}")
        if not self.model_path.exists():
            raise FileNotFoundError(
                f"Modèle introuvable : {self.model_path}\n"
                "Placez svm_best_model.pkl dans app/ml/"
            )
        with open(self.model_path, 'rb') as f:
            self.model = pickle.load(f)

        logger.info(f"Chargement du vectorizer TF-IDF : {self.tfidf_path}")
        if not self.tfidf_path.exists():
            raise FileNotFoundError(
                f"Vectorizer introuvable : {self.tfidf_path}\n"
                "Placez tfidf_ast_java.pkl dans app/ml/"
            )
        with open(self.tfidf_path, 'rb') as f:
            self.tfidf = pickle.load(f)

        # Validation : le vecteur doit avoir 2028 features
        expected = self.model.n_features_in_
        logger.info(f"Modèle prêt — features attendues : {expected}")

    def predict(self, X: scipy.sparse.csr_matrix) -> dict:
        """
        Prédit la probabilité de vulnérabilité.

        Args:
            X: Matrice sparse (1, 2028).

        Returns:
            {
                'label': 0 ou 1,
                'is_vulnerable': bool,
                'probability_vulnerable': float (0.0–1.0),
                'probability_safe': float (0.0–1.0),
            }
        """
        if self.model is None or self.tfidf is None:
            raise RuntimeError("Le modèle n'est pas chargé. Appelez load() d'abord.")

        # Validation dimension
        expected = self.model.n_features_in_
        actual = X.shape[1]
        if actual != expected:
            raise ValueError(
                f"Dimension incorrecte : reçu {actual}, attendu {expected}. "
                "Vérifiez la cohérence du vectorizer TF-IDF."
            )

        proba = self.model.predict_proba(X)[0]   # [p_safe, p_vulnerable]
        label = int(np.argmax(proba))

        return {
            'label': label,
            'is_vulnerable': bool(label == 1),
            'probability_safe':        float(proba[0]),
            'probability_vulnerable':  float(proba[1]),
        }