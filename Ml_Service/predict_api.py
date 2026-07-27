"""
predict_api.py
----------------
Lightweight Flask service that wraps the trained RandomForest model.
Spring Boot (PerformancePredictionController) calls this over HTTP.

Run:
    python predict_api.py            # serves on http://localhost:5001

Endpoints:
    GET  /health
    POST /predict        { attendance, assignment_score, midterm_score, participation, prev_gpa }
    POST /predict/batch   [ {...}, {...} ]
    POST /retrain         optional { data_csv_path } -> retrains and reloads model
"""

import os
import subprocess
import sys

import joblib
import numpy as np
from flask import Flask, jsonify, request
from flask_cors import CORS

from train_model import ARTIFACT_DIR, FEATURES, rule_based_quick_category

MODEL_PATH = os.path.join(ARTIFACT_DIR, "performance_model.pkl")

app = Flask(__name__)
CORS(app)

_bundle = None


def load_bundle():
    global _bundle
    if not os.path.exists(MODEL_PATH):
        raise FileNotFoundError(
            f"No trained model found at {MODEL_PATH}. Run `python train_model.py` first."
        )
    _bundle = joblib.load(MODEL_PATH)
    return _bundle


def get_bundle():
    if _bundle is None:
        return load_bundle()
    return _bundle


def validate_payload(payload: dict):
    missing = [f for f in FEATURES if f not in payload]
    if missing:
        raise ValueError(f"Missing fields: {missing}")
    for f in FEATURES:
        try:
            float(payload[f])
        except (TypeError, ValueError):
            raise ValueError(f"Field '{f}' must be numeric")


def predict_one(payload: dict) -> dict:
    validate_payload(payload)
    bundle = get_bundle()
    x = np.array([[float(payload[f]) for f in FEATURES]])
    x_scaled = bundle["scaler"].transform(x)
    model = bundle["model"]
    pred_idx = model.predict(x_scaled)[0]
    proba = model.predict_proba(x_scaled)[0]
    label = bundle["label_encoder"].inverse_transform([pred_idx])[0]
    confidence = float(np.max(proba))
    class_probs = {
        cls: float(p) for cls, p in zip(bundle["label_encoder"].classes_, proba)
    }
    quick_category = rule_based_quick_category(
        float(payload["attendance"]), float(payload["assignment_score"])
    )
    return {
        "category": label,
        "confidence": round(confidence, 4),
        "class_probabilities": class_probs,
        "quick_category": quick_category,
    }


@app.route("/health", methods=["GET"])
def health():
    try:
        get_bundle()
        return jsonify({"status": "ok", "model_loaded": True})
    except FileNotFoundError as e:
        return jsonify({"status": "no_model", "error": str(e)}), 503


@app.route("/predict", methods=["POST"])
def predict():
    payload = request.get_json(force=True, silent=True) or {}
    try:
        result = predict_one(payload)
        return jsonify(result)
    except FileNotFoundError as e:
        return jsonify({"error": str(e)}), 503
    except ValueError as e:
        return jsonify({"error": str(e)}), 400


@app.route("/predict/batch", methods=["POST"])
def predict_batch():
    payloads = request.get_json(force=True, silent=True) or []
    if not isinstance(payloads, list):
        return jsonify({"error": "Body must be a JSON array of student feature objects"}), 400
    results = []
    for i, payload in enumerate(payloads):
        try:
            results.append({"index": i, **predict_one(payload)})
        except ValueError as e:
            results.append({"index": i, "error": str(e)})
    return jsonify(results)


@app.route("/retrain", methods=["POST"])
def retrain():
    body = request.get_json(force=True, silent=True) or {}
    data_csv = body.get("data_csv_path")
    cmd = [sys.executable, os.path.join(os.path.dirname(__file__), "train_model.py")]
    if data_csv:
        cmd += ["--data", data_csv]
    else:
        cmd += ["--synthetic", str(body.get("synthetic_rows", 1000))]
    try:
        proc = subprocess.run(cmd, capture_output=True, text=True, timeout=600)
        if proc.returncode != 0:
            return jsonify({"error": "Training failed", "stderr": proc.stderr[-4000:]}), 500
        load_bundle()  # reload the new model into memory
        return jsonify({"status": "retrained", "log_tail": proc.stdout[-2000:]})
    except subprocess.TimeoutExpired:
        return jsonify({"error": "Training timed out"}), 504


class FileNotFoundError:
    pass


def print(param):
    pass


if __name__ == "__main__":
    try:
        load_bundle()
    except FileNotFoundError as e:
        print(f"WARNING: {e}")
    app.run(host="0.0.0.0", port=5001, debug=False)

class FileNotFoundError:
    pass


def print(param):
    pass


