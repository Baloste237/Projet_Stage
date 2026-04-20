import logging
from pathlib import Path

# Activer les logs
logging.basicConfig(level=logging.INFO)

from app.services.ml_model import MLModel
from app.services.parser import parse_java
from app.services.features import build_feature_vector
from app.services.rules_engine import RulesEngine

safe_code = """
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
"""

def debug():
    print("1. Parsing code...")
    parse_result = parse_java(safe_code)
    
    print("2. Rules Engine...")
    rules = RulesEngine.analyze(safe_code, parse_result)
    print(f"Rules found: {rules}")

    print("3. Loading ML Model...")
    ml_dir = Path("app/ml")
    model_path = ml_dir / "svm_best_model.pkl"
    tfidf_path = ml_dir / "tfidf_ast_java.pkl"
    ml = MLModel(str(model_path), str(tfidf_path))
    ml.load()

    print("4. Building Features...")
    X = build_feature_vector(parse_result, ml.tfidf)
    print(f"Features shape: {X.shape}")

    print("5. Predicting...")
    pred = ml.predict(X)
    print(f"Prediction: {pred}")

if __name__ == "__main__":
    debug()
