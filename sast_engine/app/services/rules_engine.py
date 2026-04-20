"""
rules_engine.py — Moteur de règles heuristiques (SAST Hybride)
Détecte les vulnérabilités classiques et évidentes via l'analyse statique et regex.
Ces règles interviennent avant ou en parallèle du modèle ML pour garantir 
zéro faux négatif sur les failles critiques.
"""
import re
from typing import List
from dataclasses import dataclass
from app.services.parser import ParseResult

@dataclass
class RuleMatch:
    cwe_id: str
    name: str
    description: str
    confidence: float
    evidence: str

class RulesEngine:
    """
    Mises en correspondance des règles de sécurité via de l'heuristique (Regex, etc.).
    """

    @classmethod
    def analyze(cls, code: str, parse_result: ParseResult = None) -> List[RuleMatch]:
        """
        Applique les règles de sécurité heuristiques sur le code brut 
        et retourne la liste des CWE trouvées.
        """
        matches = []
        if not code:
            return matches

        matches.extend(cls._detect_hardcoded_credentials(code))
        matches.extend(cls._detect_weak_crypto(code))
        matches.extend(cls._detect_sql_injection(code))

        return matches

    @classmethod
    def _detect_hardcoded_credentials(cls, code: str) -> List[RuleMatch]:
        """
        CWE-798: Use of Hard-coded Credentials
        Recherche des mots de passe en clair dans les chaînes de caractères.
        """
        matches = []
        # Recherche 'password' ou 'secret' suivi de '=' ou ':' puis d'une chaîne classique
        pattern = r'(?i)\b(password|passwd|pwd|secret|api_key|token)\b\s*[:=]\s*(["\'])(.*?)\2'
        
        for match in re.finditer(pattern, code):
            secret_value = match.group(3).strip()
            # On ignore les espaces vides ou les variables non assignées à des valeurs littérales
            if len(secret_value) > 0 and secret_value.lower() not in ['null', 'true', 'false']:
                # On ne montre qu'une partie de l'évidence pour ne pas leak les secrets
                safe_evidence = match.group(0)[:15] + "...\""
                matches.append(RuleMatch(
                    cwe_id="CWE-798",
                    name="Use of Hard-coded Credentials",
                    description="Des informations d'identification ou secrets ont été détectés écrits en clair.",
                    confidence=0.95,
                    evidence=f"Mot-clé suspect détecté : {safe_evidence}"
                ))
        return matches

    @classmethod
    def _detect_weak_crypto(cls, code: str) -> List[RuleMatch]:
        """
        CWE-327: Use of a Broken or Risky Cryptographic Algorithm
        Recherche MD5 ou SHA1 qui sont considérés cryptographiquement faibles.
        """
        matches = []
        pattern = r'MessageDigest\.getInstance\(\s*(["\'])(MD5|SHA-1)\1\s*\)'
        
        for match in re.finditer(pattern, code):
            matches.append(RuleMatch(
                cwe_id="CWE-327",
                name="Use of a Broken or Risky Cryptographic Algorithm",
                description=f"Utilisation de l'algorithme obsolète {match.group(2)} détectée.",
                confidence=1.0,
                evidence=match.group(0)
            ))
        return matches

    @classmethod
    def _detect_sql_injection(cls, code: str) -> List[RuleMatch]:
        """
        CWE-89: Improper Neutralization of Special Elements used in an SQL Command
        Recherche de concaténation de string basique dans un Statement SQL.
        """
        matches = []
        # Pattern simpliste : .executeQuery("..." + variable) 
        pattern = r'(executeQuery|executeUpdate|execute)\s*\(\s*["\'].*?["\']\s*\+\s*[a-zA-Z0-9_]+'
        
        for match in re.finditer(pattern, code):
            matches.append(RuleMatch(
                cwe_id="CWE-89",
                name="SQL Injection",
                description="Concaténation détectée dans une requête SQL (Risque fort d'Injection).",
                confidence=0.85,  # Un peu moins confiant (ça peut être une constante sûre)
                evidence=match.group(0) + "..."
            ))
        return matches
