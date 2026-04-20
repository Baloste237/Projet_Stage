"""
parser.py — Étape 1 du pipeline SAST
Parse le code Java en AST avec javalang.
"""
import javalang
from typing import Optional


class ParseResult:
    def __init__(self, tree, source: str, parse_error: bool = False):
        self.tree = tree
        self.source = source
        self.parse_error = parse_error


def parse_java(source_code: str) -> ParseResult:
    """
    Parse du code Java brut vers un AST javalang.

    Args:
        source_code: Code Java sous forme de chaîne de caractères.

    Returns:
        ParseResult avec l'arbre AST ou parse_error=True si échec.
    """
    if not source_code or not source_code.strip():
        return ParseResult(tree=None, source=source_code, parse_error=True)

    try:
        tree = javalang.parse.parse(source_code)
        return ParseResult(tree=tree, source=source_code, parse_error=False)
    except Exception:
        # Code non parsable (syntaxe invalide, fragment incomplet…)
        return ParseResult(tree=None, source=source_code, parse_error=True)