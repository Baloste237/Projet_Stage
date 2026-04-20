import pytest
from fastapi.testclient import TestClient

import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from app.main import app
from app.services.ml_model import MLModel

# Utilisation d'un client de test. 
# Remarque: avec `with TestClient(app)`, les événements de cycle de vie (Lifespan) 
# sont déclenchés automatiquement (chargement des modèles)
@pytest.fixture(scope="module")
def client():
    with TestClient(app) as c:
        yield c

def test_health_check(client):
    """Vérifie que l'API est saine et que les modèles se chargent bien."""
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["model_loaded"] is True
    assert data["tfidf_loaded"] is True

def test_analyze_safe_code(client, monkeypatch):
    """Test avec un code Java propre et inoffensif."""
    safe_code = """
    public class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello, World!");
        }
    }
    """
    
    # On mock le modèle ML car sur un faux exemple non-représentatif comme "Hello World", 
    # le modèle (entraîné sur une large base de données) peut "halluciner".
    def mock_predict(*args, **kwargs):
        return {
            'label': 0,
            'is_vulnerable': False,
            'probability_safe': 0.99,
            'probability_vulnerable': 0.01
        }
    monkeypatch.setattr(MLModel, "predict", mock_predict)

    response = client.post("/api/v1/analyze", json={"code": safe_code, "filename": "HelloWorld.java"})
    
    assert response.status_code == 200
    data = response.json()
    assert data["is_vulnerable"] is False
    assert data["overall_severity"] == "None"

def test_analyze_hybrid_hardcoded_password(client):
    """Test du moteur de règle (Hybride) : mot de passe en clair."""
    vuln_code = """
    public class DBConnection {
        public void connect() {
            String dbUser = "admin";
            String password = "superSecretPassword123"; // Vulnerability here !
            // connection logic...
        }
    }
    """
    response = client.post("/api/v1/analyze", json={"code": vuln_code, "filename": "DBConnection.java"})
    
    assert response.status_code == 200
    data = response.json()
    # Le moteur de règles a dû forcer is_vulnerable à True
    assert data["is_vulnerable"] is True
    
    # On vérifie que la CWE-798 a bien remarchée à la surface
    cwe_ids = [cwe["cwe_id"] for cwe in data["cwe_findings"]]
    assert "CWE-798" in cwe_ids, "Le RuleEngine Hybride n'a pas détecté le Hardcoded Password"

def test_analyze_hybrid_weak_crypto(client):
    """Test du moteur de règles : utilisation de MD5."""
    vuln_code = """
    import java.security.MessageDigest;

    public class Hasher {
        public byte[] hashIt(String input) throws Exception {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(input.getBytes());
        }
    }
    """
    response = client.post("/api/v1/analyze", json={"code": vuln_code, "filename": "Hasher.java"})
    
    assert response.status_code == 200
    data = response.json()
    assert data["is_vulnerable"] is True
    
    cwe_ids = [cwe["cwe_id"] for cwe in data["cwe_findings"]]
    assert "CWE-327" in cwe_ids, "Le RuleEngine Hybride n'a pas détecté la Cryptographie Faible"
