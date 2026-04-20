"""
cvss_calculator.py — Étape 5 du pipeline SAST
Calcul du score CVSS v3 à partir du CWE et de la probabilité ML.

Approche : chaque CWE a des métriques CVSS v3 de base prédéfinies.
Le score final est pondéré par la probabilité ML du modèle.

Formule :
  cvss_adjusted = cvss_base × (0.5 + 0.5 × ml_probability)

Référence : https://www.first.org/cvss/v3.1/specification-document
"""
from dataclasses import dataclass
from typing import Optional, List


@dataclass
class CVSSScore:
    base_score: float           # Score CVSS v3 de base (0.0–10.0)
    adjusted_score: float       # Score ajusté par la proba ML (0.0–10.0)
    severity: str               # None/Low/Medium/High/Critical
    vector_string: str          # Ex: CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N
    cwe_id: str
    rationale: str


# ─────────────────────────────────────────────────────────────────────────────
# Table CVSS v3 par CWE
# Champs : (base_score, vector_string, severity_label)
# Sources : NVD / MITRE CWE top-25
# ─────────────────────────────────────────────────────────────────────────────
_CWE_CVSS_TABLE = {
    'CWE-89': (
        9.8, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H', 'Critical',
        "SQL Injection — accès complet aux données, modification possible."
    ),
    'CWE-22': (
        7.5, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N', 'High',
        "Path Traversal — lecture arbitraire de fichiers système."
    ),
    'CWE-79': (
        6.1, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:C/C:L/I:L/A:N', 'Medium',
        "XSS — vol de session, injection de script dans le navigateur."
    ),
    'CWE-78': (
        9.8, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H', 'Critical',
        "OS Command Injection — exécution arbitraire de commandes système."
    ),
    'CWE-470': (
        8.1, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:H', 'High',
        "Unsafe Reflection — exécution de code arbitraire via chargement dynamique."
    ),
    'CWE-476': (
        7.5, 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H', 'High',
        "Null Pointer Dereference — déni de service, crash applicatif."
    ),
    'CWE-327': (
        7.4, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N', 'High',
        "Broken Crypto — algorithme faible permettant le déchiffrement des données."
    ),
    'CWE-330': (
        5.9, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N', 'Medium',
        "Weak Random — prédictibilité des valeurs aléatoires pour la sécurité."
    ),
    'CWE-693': (
        5.0, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N', 'Medium',
        "Protection Mechanism Failure — vulnérabilité générique détectée par ML."
    ),
}

# Score par défaut si CWE inconnu
_DEFAULT_CVSS = (
    4.0, 'CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N', 'Medium',
    "Vulnérabilité de type inconnu."
)


def _severity_label(score: float) -> str:
    """Retourne le label de sévérité CVSS v3 correspondant au score."""
    if score == 0.0:
        return 'None'
    elif score < 4.0:
        return 'Low'
    elif score < 7.0:
        return 'Medium'
    elif score < 9.0:
        return 'High'
    else:
        return 'Critical'


def calculate_cvss(
    cwe_id: str,
    ml_probability: float,
) -> CVSSScore:
    """
    Calcule le score CVSS v3 pour un CWE donné, ajusté par la probabilité ML.

    Args:
        cwe_id: Identifiant CWE (ex: "CWE-89").
        ml_probability: Probabilité de vulnérabilité du SVM (0.0–1.0).

    Returns:
        CVSSScore avec score de base, score ajusté, sévérité et vecteur.
    """
    base_score, vector, _, rationale = _CWE_CVSS_TABLE.get(cwe_id, _DEFAULT_CVSS)

    # Ajustement par la confiance ML
    # Formule : score_ajusté = base × (0.5 + 0.5 × proba_ml)
    # → Si proba=1.0 : score_ajusté = base (plein score)
    # → Si proba=0.5 : score_ajusté = base × 0.75 (atténué)
    adjusted = round(base_score * (0.5 + 0.5 * ml_probability), 1)
    adjusted = min(10.0, max(0.0, adjusted))

    return CVSSScore(
        base_score=base_score,
        adjusted_score=adjusted,
        severity=_severity_label(adjusted),
        vector_string=vector,
        cwe_id=cwe_id,
        rationale=rationale,
    )


def calculate_all_cvss(
    cwe_matches: list,
    ml_probability: float,
) -> List[CVSSScore]:
    """
    Calcule les scores CVSS pour tous les CWE détectés.
    Retourne la liste triée par score ajusté décroissant.

    Args:
        cwe_matches: Liste de CWEMatch (depuis cwe_mapper.py).
        ml_probability: Probabilité ML globale de vulnérabilité.

    Returns:
        Liste de CVSSScore triée par criticité.
    """
    if not cwe_matches:
        return []

    scores = [
        calculate_cvss(match.cwe_id, ml_probability)
        for match in cwe_matches
    ]
    scores.sort(key=lambda s: s.adjusted_score, reverse=True)
    return scores