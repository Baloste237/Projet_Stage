"""
features.py — Étape 2 du pipeline SAST
Extraction des features : AST structurelles (35) + TF-IDF nettoyé (1993).
Reproduit EXACTEMENT la logique du notebook ast_extractor_java_fixed.ipynb.

Vecteur final = hstack([X_tfidf (1993), X_ast (35)]) → 2028 dimensions.

Décompte vérifié sur le modèle pkl :
  model.n_features_in_ = 2028
  AST_FEATURE_ORDER    = 35 colonnes
  TF-IDF (après filtre min_df/max_df) = 1993 colonnes
"""
import re
import pickle
import numpy as np
import scipy.sparse
import javalang

from collections import Counter, defaultdict
from pathlib import Path
from typing import Optional

from app.services.parser import ParseResult


# ── Nœuds AST sensibles à la sécurité (identiques au notebook) ───────────────
SENSITIVE_IMPORTS = {
    'java.io', 'java.net', 'java.sql', 'java.lang.reflect',
    'java.security', 'javax.crypto', 'java.nio', 'java.util.Random',
    'java.lang.Runtime', 'java.lang.ProcessBuilder'
}


# ─────────────────────────────────────────────────────────────────────────────
# 1. Profondeur AST (itérative, identique au notebook)
# ─────────────────────────────────────────────────────────────────────────────
def get_ast_depth(tree) -> int:
    """Calcule la profondeur maximale de l'AST de manière itérative."""
    if not hasattr(tree, 'children'):
        return 0
    max_depth = 0
    stack = [(tree, 0)]
    while stack:
        node, depth = stack.pop()
        if hasattr(node, 'children'):
            for child in node.children:
                if isinstance(child, javalang.tree.Node):
                    stack.append((child, depth + 1))
                    max_depth = max(max_depth, depth + 1)
                elif isinstance(child, list):
                    for item in child:
                        if isinstance(item, javalang.tree.Node):
                            stack.append((item, depth + 1))
                            max_depth = max(max_depth, depth + 1)
    return max_depth


# ─────────────────────────────────────────────────────────────────────────────
# 2. Extraction des 28 features AST (identique au notebook)
# ─────────────────────────────────────────────────────────────────────────────
def extract_ast_features(parse_result: ParseResult) -> dict:
    """
    Extrait ~28 features AST structurelles.
    Si parse_error=True → toutes les features = 0.0, parse_error = 1.0.
    """
    feats = defaultdict(float)

    if parse_result.parse_error or parse_result.tree is None:
        feats['parse_error'] = 1.0
        return dict(feats)

    tree = parse_result.tree

    # ── Comptage de tous les nœuds ────────────────────────────────────────
    node_types = Counter()
    total_nodes = 0
    for path, node in tree:
        node_type = type(node).__name__
        node_types[node_type] += 1
        total_nodes += 1

    feats['total_nodes']       = total_nodes
    feats['unique_node_types'] = len(node_types)

    # ── Déclarations ──────────────────────────────────────────────────────
    feats['n_classes']      = node_types.get('ClassDeclaration', 0)
    feats['n_interfaces']   = node_types.get('InterfaceDeclaration', 0)
    feats['n_methods']      = node_types.get('MethodDeclaration', 0)
    feats['n_constructors'] = node_types.get('ConstructorDeclaration', 0)
    feats['n_fields']       = node_types.get('FieldDeclaration', 0)
    feats['n_local_vars']   = node_types.get('LocalVariableDeclaration', 0)
    feats['n_parameters']   = node_types.get('FormalParameter', 0)

    # ── Flux de contrôle ──────────────────────────────────────────────────
    feats['n_if']       = node_types.get('IfStatement', 0)
    feats['n_for']      = node_types.get('ForStatement', 0)
    feats['n_while']    = node_types.get('WhileStatement', 0)
    feats['n_do_while'] = node_types.get('DoStatement', 0)
    feats['n_switch']   = node_types.get('SwitchStatement', 0)
    feats['n_try']      = node_types.get('TryStatement', 0)
    feats['n_catch']    = node_types.get('CatchClause', 0)
    feats['n_finally']  = node_types.get('FinallyBlock', 0)
    feats['n_throw']    = node_types.get('ThrowStatement', 0)
    feats['n_return']   = node_types.get('ReturnStatement', 0)
    feats['n_break']    = node_types.get('BreakStatement', 0)
    feats['n_continue'] = node_types.get('ContinueStatement', 0)

    # ── Appels et créations ───────────────────────────────────────────────
    feats['n_method_invoc']    = node_types.get('MethodInvocation', 0)
    feats['n_object_creation'] = node_types.get('ClassCreator', 0)
    feats['n_array_creation']  = node_types.get('ArrayCreator', 0)
    feats['n_array_access']    = node_types.get('ArraySelector', 0)
    feats['n_cast']            = node_types.get('Cast', 0)
    feats['n_ternary']         = node_types.get('TernaryExpression', 0)

    # ── Complexité cyclomatique approchée ─────────────────────────────────
    feats['cyclomatic_complexity'] = (
        1 +
        feats['n_if'] + feats['n_for'] + feats['n_while'] +
        feats['n_do_while'] + feats['n_switch'] + feats['n_catch'] +
        feats['n_ternary']
    )

    # ── Profondeur max de l'AST ───────────────────────────────────────────
    try:
        feats['ast_depth'] = get_ast_depth(tree)
    except Exception:
        feats['ast_depth'] = 0

    # ── Imports ───────────────────────────────────────────────────────────
    imports = [imp.path for imp in tree.imports] if tree.imports else []
    feats['n_imports']          = len(imports)
    feats['n_sensitive_imports'] = sum(
        1 for imp in imports
        if any(imp.startswith(s) for s in SENSITIVE_IMPORTS)
    )

    # ── Ratios métriques ──────────────────────────────────────────────────
    if feats['n_methods'] > 0:
        feats['avg_params_per_method'] = feats['n_parameters'] / feats['n_methods']
        feats['try_catch_ratio']       = feats['n_try']          / feats['n_methods']
        feats['method_invoc_ratio']    = feats['n_method_invoc'] / feats['n_methods']
    else:
        feats['avg_params_per_method'] = 0.0
        feats['try_catch_ratio']       = 0.0
        feats['method_invoc_ratio']    = 0.0

    feats['parse_error'] = 0.0
    return dict(feats)


# ─────────────────────────────────────────────────────────────────────────────
# 3. Nettoyage du code source (identique au notebook — PATCH v2 anti-leakage)
# ─────────────────────────────────────────────────────────────────────────────
def clean_code(code: str) -> str:
    """
    Nettoie le code Java pour l'analyse TF-IDF.
    Reproduit exactement clean_code() du notebook (PATCH v2).
    """
    if not isinstance(code, str) or not code.strip():
        return ''

    # Commentaires multi-lignes et Javadoc
    code = re.sub(r'/\*.*?\*/', '', code, flags=re.DOTALL)
    # Commentaires de ligne
    code = re.sub(r'//.*', '', code)
    # Imports et packages
    code = re.sub(r'^\s*import\s+[\w.]+;', '', code, flags=re.MULTILINE)
    code = re.sub(r'^\s*package\s+[\w.]+;', '', code, flags=re.MULTILINE)
    # Annotations
    code = re.sub(r'@\w+', '', code)

    # Suppression des patterns CWE (anti-data leakage)
    code = re.sub(r'CWE\d+[_\-]?\w*', ' ', code, flags=re.IGNORECASE)
    # Suppression des suffixes Juliet
    code = re.sub(r'\b(bad|good|safe|testcase|servlet|helper)\w*\b', ' ', code, flags=re.IGNORECASE)
    code = re.sub(r'_s?\d{2,}\b', '', code)

    # Normalisation des littéraux
    code = re.sub(r'"[^"]*"',  ' STRING_LITERAL ', code)
    code = re.sub(r"'[^']*'",  ' CHAR_LITERAL ',   code)
    code = re.sub(r'\b\d+\b',  ' NUMBER_LITERAL ', code)
    code = re.sub(r'\s+', ' ', code).strip()

    return code if len(code) >= 10 else ''


# ─────────────────────────────────────────────────────────────────────────────
# 4. Ordre des colonnes AST (doit correspondre à celui du notebook)
# ─────────────────────────────────────────────────────────────────────────────
AST_FEATURE_ORDER = [
    'total_nodes', 'unique_node_types',
    'n_classes', 'n_interfaces', 'n_methods', 'n_constructors',
    'n_fields', 'n_local_vars', 'n_parameters',
    'n_if', 'n_for', 'n_while', 'n_do_while', 'n_switch',
    'n_try', 'n_catch', 'n_finally', 'n_throw', 'n_return',
    'n_break', 'n_continue',
    'n_method_invoc', 'n_object_creation', 'n_array_creation',
    'n_array_access', 'n_cast', 'n_ternary',
    'cyclomatic_complexity', 'ast_depth',
    'n_imports', 'n_sensitive_imports',
    'avg_params_per_method', 'try_catch_ratio', 'method_invoc_ratio',
    'parse_error',
]


# ─────────────────────────────────────────────────────────────────────────────
# 5. Construction du vecteur combiné (TF-IDF + AST) → 2028 dims
# ─────────────────────────────────────────────────────────────────────────────
def build_feature_vector(
    parse_result: ParseResult,
    tfidf_vectorizer,
) -> scipy.sparse.csr_matrix:
    """
    Produit le vecteur de features combiné prêt pour le modèle SVM.

    Pipeline (identique à l'entraînement) :
      1. clean_code()           → texte nettoyé
      2. tfidf.transform()      → vecteur TF-IDF sparse (≈2000 dims)
      3. extract_ast_features() → 28 features AST
      4. hstack([tfidf, ast])   → vecteur combiné 2028 dims

    Args:
        parse_result: Résultat du parser.py (AST + source brut).
        tfidf_vectorizer: Instance TfidfVectorizer déjà fitté (tfidf_ast_java.pkl).

    Returns:
        Matrice sparse (1, 2028) prête pour model.predict_proba().
    """
    # — TF-IDF —
    cleaned = clean_code(parse_result.source)
    if not cleaned:
        # Document vide → vecteur TF-IDF nul
        n_tfidf = len(tfidf_vectorizer.get_feature_names_out())
        X_tfidf = scipy.sparse.csr_matrix((1, n_tfidf), dtype=np.float32)
    else:
        X_tfidf = tfidf_vectorizer.transform([cleaned])

    # — AST features —
    ast_feats = extract_ast_features(parse_result)
    ast_row = [ast_feats.get(col, 0.0) for col in AST_FEATURE_ORDER]
    X_ast = scipy.sparse.csr_matrix(
        np.array(ast_row, dtype=np.float32).reshape(1, -1)
    )

    # — Fusion —
    X_combined = scipy.sparse.hstack([X_tfidf, X_ast])
    return X_combined