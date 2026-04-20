"""
cwe_mapper.py — Étape 4 du pipeline SAST
Mapping pattern AST → identifiants CWE (Common Weakness Enumeration).

Stratégie hybride :
  - Le ML détecte qu'il y a une vulnérabilité (binaire).
  - Le CWE mapper identifie QUEL type de vulnérabilité (basé sur patterns AST).
  - Si aucun pattern ne matche → CWE générique basé sur la probabilité ML.
"""
import re
import javalang
from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class CWEMatch:
    cwe_id: str           # Ex: "CWE-89"
    name: str             # Ex: "SQL Injection"
    description: str
    confidence: float     # 0.0 – 1.0
    evidence: str         # Ce qui a déclenché le match


# ─────────────────────────────────────────────────────────────────────────────
# Base de règles : pattern AST → CWE
# ─────────────────────────────────────────────────────────────────────────────
# Chaque règle est une fonction (tree, source) → Optional[CWEMatch]

def _check_sql_injection(tree, source: str) -> Optional[CWEMatch]:
    """CWE-89 : SQL Injection — concaténation de chaîne dans une requête SQL."""
    sql_methods = {'executeQuery', 'executeUpdate', 'execute', 'prepareStatement'}
    suspicious_concat = False

    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.MethodInvocation):
                if node.member in sql_methods:
                    # Vérifie si un argument est une BinaryOperation (concaténation +)
                    if node.arguments:
                        for arg in node.arguments:
                            if isinstance(arg, javalang.tree.BinaryOperation) and arg.operator == '+':
                                suspicious_concat = True
                                break
    except Exception:
        pass

    # Complément regex sur le source nettoyé
    has_sql_kw = bool(re.search(
        r'\b(executeQuery|executeUpdate|prepareStatement)\b.*\+',
        source, re.IGNORECASE
    ))

    if suspicious_concat or has_sql_kw:
        return CWEMatch(
            cwe_id='CWE-89',
            name='SQL Injection',
            description="Concaténation de données non validées dans une requête SQL.",
            confidence=0.85 if suspicious_concat else 0.60,
            evidence="Appel SQL avec concaténation de chaîne détecté."
        )
    return None


def _check_path_traversal(tree, source: str) -> Optional[CWEMatch]:
    """CWE-22 : Path Traversal — construction de chemin avec entrée utilisateur."""
    file_classes = {'File', 'FileInputStream', 'FileOutputStream', 'FileReader',
                    'FileWriter', 'Path', 'Paths'}
    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.ClassCreator):
                if hasattr(node.type, 'name') and node.type.name in file_classes:
                    if node.arguments:
                        for arg in node.arguments:
                            if isinstance(arg, javalang.tree.BinaryOperation):
                                return CWEMatch(
                                    cwe_id='CWE-22',
                                    name='Path Traversal',
                                    description="Construction de chemin de fichier avec données potentiellement non validées.",
                                    confidence=0.75,
                                    evidence=f"new {node.type.name}() avec concaténation détecté."
                                )
    except Exception:
        pass

    if re.search(r'new\s+(File|FileInputStream|FileOutputStream)\s*\(.*\+', source):
        return CWEMatch(
            cwe_id='CWE-22',
            name='Path Traversal',
            description="Construction de chemin de fichier avec données potentiellement non validées.",
            confidence=0.55,
            evidence="Pattern new File(... +) détecté dans le source."
        )
    return None


def _check_xss(tree, source: str) -> Optional[CWEMatch]:
    """CWE-79 : Cross-Site Scripting — écriture de données dans la réponse HTTP."""
    output_methods = {'println', 'print', 'write', 'getWriter'}
    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.MethodInvocation):
                if node.member in output_methods:
                    if node.arguments:
                        for arg in node.arguments:
                            if isinstance(arg, javalang.tree.BinaryOperation):
                                return CWEMatch(
                                    cwe_id='CWE-79',
                                    name='Cross-Site Scripting (XSS)',
                                    description="Écriture de données non échappées dans la réponse HTTP.",
                                    confidence=0.70,
                                    evidence=f"Appel {node.member}() avec concaténation détecté."
                                )
    except Exception:
        pass
    return None


def _check_command_injection(tree, source: str) -> Optional[CWEMatch]:
    """CWE-78 : OS Command Injection — Runtime.exec() ou ProcessBuilder."""
    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.MethodInvocation):
                if node.member == 'exec':
                    return CWEMatch(
                        cwe_id='CWE-78',
                        name='OS Command Injection',
                        description="Exécution d'une commande système avec des données potentiellement contrôlées.",
                        confidence=0.80,
                        evidence="Appel Runtime.exec() détecté."
                    )
            if isinstance(node, javalang.tree.ClassCreator):
                if hasattr(node.type, 'name') and node.type.name == 'ProcessBuilder':
                    return CWEMatch(
                        cwe_id='CWE-78',
                        name='OS Command Injection',
                        description="Utilisation de ProcessBuilder avec des arguments potentiellement non validés.",
                        confidence=0.75,
                        evidence="new ProcessBuilder() détecté."
                    )
    except Exception:
        pass
    return None


def _check_unsafe_reflection(tree, source: str) -> Optional[CWEMatch]:
    """CWE-470 : Unsafe Reflection — Class.forName() avec entrée utilisateur."""
    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.MethodInvocation):
                if node.member == 'forName':
                    return CWEMatch(
                        cwe_id='CWE-470',
                        name='Unsafe Reflection',
                        description="Chargement dynamique de classe via réflexion avec données non validées.",
                        confidence=0.72,
                        evidence="Appel Class.forName() détecté."
                    )
    except Exception:
        pass
    return None


def _check_null_dereference(tree, source: str) -> Optional[CWEMatch]:
    """CWE-476 : Null Pointer Dereference — accès sans vérification null."""
    try:
        for _, node in tree:
            if isinstance(node, javalang.tree.MethodInvocation):
                if node.qualifier and node.qualifier == 'null':
                    return CWEMatch(
                        cwe_id='CWE-476',
                        name='Null Pointer Dereference',
                        description="Déréférencement potentiel d'une référence nulle.",
                        confidence=0.60,
                        evidence="Accès à une référence potentiellement nulle."
                    )
    except Exception:
        pass
    return None


def _check_weak_crypto(tree, source: str) -> Optional[CWEMatch]:
    """CWE-327 : Use of a Broken Crypto Algorithm."""
    weak_algos = ['MD5', 'SHA1', 'SHA-1', 'DES', 'RC4', 'RC2']
    for algo in weak_algos:
        if algo in source:
            return CWEMatch(
                cwe_id='CWE-327',
                name='Use of a Broken Cryptographic Algorithm',
                description=f"Utilisation de l'algorithme cryptographique faible : {algo}.",
                confidence=0.80,
                evidence=f"Algorithme '{algo}' trouvé dans le code source."
            )
    return None


def _check_random(tree, source: str) -> Optional[CWEMatch]:
    """CWE-330 : Use of Insufficiently Random Values."""
    if re.search(r'\bnew\s+Random\b', source):
        return CWEMatch(
            cwe_id='CWE-330',
            name='Use of Insufficiently Random Values',
            description="Utilisation de java.util.Random (non cryptographique) pour des opérations de sécurité.",
            confidence=0.65,
            evidence="new Random() détecté."
        )
    return None


# Liste ordonnée de toutes les règles (ordre = priorité)
_RULES = [
    _check_sql_injection,
    _check_path_traversal,
    _check_command_injection,
    _check_unsafe_reflection,
    _check_xss,
    _check_weak_crypto,
    _check_random,
    _check_null_dereference,
]


# ─────────────────────────────────────────────────────────────────────────────
# Interface publique
# ─────────────────────────────────────────────────────────────────────────────
def map_cwe(
    parse_result,
    ml_probability: float,
) -> List[CWEMatch]:
    """
    Applique toutes les règles et retourne la liste des CWE détectés.

    Si aucun pattern ne matche mais que le ML est confiant (proba > 0.6),
    on retourne un CWE générique CWE-693 (Protection Mechanism Failure).

    Args:
        parse_result: Résultat du parser (tree + source).
        ml_probability: Probabilité de vulnérabilité du modèle ML (0–1).

    Returns:
        Liste (potentiellement vide) de CWEMatch triée par confiance décroissante.
    """
    matches: List[CWEMatch] = []

    if parse_result.tree is not None and not parse_result.parse_error:
        for rule in _RULES:
            try:
                result = rule(parse_result.tree, parse_result.source)
                if result is not None:
                    matches.append(result)
            except Exception:
                continue

    # Fallback si ML est confiant mais aucun pattern détecté
    if not matches and ml_probability >= 0.60:
        matches.append(CWEMatch(
            cwe_id='CWE-693',
            name='Protection Mechanism Failure',
            description="Vulnérabilité détectée par le modèle ML sans pattern structurel identifié.",
            confidence=ml_probability * 0.7,
            evidence=f"Score ML de vulnérabilité : {ml_probability:.2f}."
        ))

    # Tri par confiance décroissante
    matches.sort(key=lambda m: m.confidence, reverse=True)
    return matches