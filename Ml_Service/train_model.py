"""
train_model.py
----------------
Trains a RandomForestClassifier to predict a student's performance category
from 5 features: attendance, assignment_score, midterm_score, participation, prev_gpa.

Usage:
    python train_model.py --data students_data.csv
    python train_model.py --synthetic 1200        # generate synthetic data instead

Outputs (written to ./artifacts/):
    - performance_model.pkl        (trained RandomForestClassifier, wrapped with scaler + label encoder)
    - feature_importance.png
    - confusion_matrix.png
    - metrics.json                 (accuracy, precision, recall, f1, best hyperparameters)
"""

import argparse
import json
import os

import joblib
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.model_selection import GridSearchCV, train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler

FEATURES = ["attendance", "assignment_score", "midterm_score", "participation", "prev_gpa"]

CATEGORY_ORDER = ["At Risk", "Needs Improvement", "Average", "Good", "Excellent"]

ARTIFACT_DIR = os.path.join(os.path.dirname(__file__), "artifacts")
os.makedirs(ARTIFACT_DIR, exist_ok=True)


def composite_score(row: pd.Series) -> float:
    """Weighted composite (0-100) used to derive the ground-truth label.

    Weights: attendance 20%, assignment 30%, midterm 30%, participation 10%,
    prev_gpa 10% (prev_gpa assumed on a 0-4 scale, rescaled to 0-100).
    """
    gpa_pct = (row["prev_gpa"] / 4.0) * 100
    return (
            0.20 * row["attendance"]
            + 0.30 * row["assignment_score"]
            + 0.30 * row["midterm_score"]
            + 0.10 * row["participation"]
            + 0.10 * gpa_pct
    )


def score_to_category(score: float) -> str:
    if score >= 90:
        return "Excellent"
    if score >= 70:
        return "Good"
    if score >= 50:
        return "Average"
    if score >= 30:
        return "Needs Improvement"
    return "At Risk"


def rule_based_quick_category(attendance: float, assignment_score: float) -> str:
    """Lightweight A-D quick-categorization (attendance + assignment only),
    used for the sidebar's 4 category cards without calling the model."""
    if attendance >= 80 and assignment_score >= 80:
        return "High Performer"
    if 60 <= attendance <= 79 and 50 <= assignment_score <= 79:
        return "Average Performer"
    if 40 <= attendance <= 59 and 30 <= assignment_score <= 49:
        return "Needs Attention"
    if attendance < 40 or assignment_score < 30:
        return "Critical Intervention"
    # Anything falling outside these exact bands: fall back to nearest bucket.
    if attendance >= 60 and assignment_score >= 50:
        return "Average Performer"
    return "Needs Attention"


def generate_synthetic_data(n: int, seed: int = 42) -> pd.DataFrame:
    """Generates class-balanced synthetic data by sampling features within
    per-category target bands, so every category has enough rows for
    stratified train/test splitting and cross-validation."""
    rng = np.random.default_rng(seed)
    # score bands roughly matching score_to_category thresholds
    bands = {
        "Excellent": (90, 100),
        "Good": (70, 89),
        "Average": (50, 69),
        "Needs Improvement": (30, 49),
        "At Risk": (0, 29),
    }
    per_class = n // len(bands)
    rows = []
    for label, (lo, hi) in bands.items():
        for _ in range(per_class):
            target = rng.uniform(lo, hi)
            # jitter each feature around the target score, clipped to valid ranges
            attendance = np.clip(target + rng.normal(0, 8), 0, 100)
            assignment_score = np.clip(target + rng.normal(0, 8), 0, 100)
            midterm_score = np.clip(target + rng.normal(0, 8), 0, 100)
            participation = np.clip(target + rng.normal(0, 12), 0, 100)
            prev_gpa = np.clip((target / 100) * 4 + rng.normal(0, 0.4), 0.5, 4.0)
            rows.append(
                {
                    "attendance": round(attendance, 1),
                    "assignment_score": round(assignment_score, 1),
                    "midterm_score": round(midterm_score, 1),
                    "participation": round(participation, 1),
                    "prev_gpa": round(prev_gpa, 2),
                }
            )
    df = pd.DataFrame(rows)
    df["category"] = df.apply(lambda r: score_to_category(composite_score(r)), axis=1)
    return df.sample(frac=1, random_state=seed).reset_index(drop=True)


def load_data(args) -> pd.DataFrame:
    if args.data:
        df = pd.read_csv(args.data)
        missing = [c for c in FEATURES if c not in df.columns]
        if missing:
            raise ValueError(f"Input CSV is missing required columns: {missing}")
        if "category" not in df.columns:
            df["category"] = df.apply(lambda r: score_to_category(composite_score(r)), axis=1)
        return df
    return generate_synthetic_data(args.synthetic)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--data", type=str, default=None, help="Path to CSV with student data")
    parser.add_argument("--synthetic", type=int, default=1000, help="Rows of synthetic data if --data not given")
    args = parser.parse_args()

    df = load_data(args)
    print(f"Loaded {len(df)} rows. Category distribution:\n{df['category'].value_counts()}\n")

    X = df[FEATURES]
    y = df["category"]

    label_encoder = LabelEncoder()
    label_encoder.fit(CATEGORY_ORDER)  # fix a stable, meaningful order
    y_enc = label_encoder.transform(y)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y_enc, test_size=0.20, random_state=42, stratify=y_enc
    )

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # --- Hyperparameter tuning ---
    param_grid = {
        "n_estimators": [100, 200, 300],
        "max_depth": [None, 8, 12, 16],
        "min_samples_split": [2, 5, 10],
        "min_samples_leaf": [1, 2, 4],
    }
    base_clf = RandomForestClassifier(random_state=42, class_weight="balanced")
    grid = GridSearchCV(base_clf, param_grid, cv=5, scoring="f1_weighted", n_jobs=-1, verbose=1)
    grid.fit(X_train_scaled, y_train)

    best_model = grid.best_estimator_
    print(f"\nBest hyperparameters: {grid.best_params_}")

    # --- Evaluation ---
    y_pred = best_model.predict(X_test_scaled)

    accuracy = accuracy_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average="weighted", zero_division=0)
    recall = recall_score(y_test, y_pred, average="weighted", zero_division=0)
    f1 = f1_score(y_test, y_pred, average="weighted", zero_division=0)

    print(f"\nAccuracy:  {accuracy:.4f}")
    print(f"Precision: {precision:.4f}")
    print(f"Recall:    {recall:.4f}")
    print(f"F1-score:  {f1:.4f}")
    report = classification_report(
        y_test, y_pred, target_names=label_encoder.classes_, zero_division=0
    )
    print("\nClassification report:\n", report)

    # --- Confusion matrix plot ---
    cm = confusion_matrix(y_test, y_pred)
    plt.figure(figsize=(7, 6))
    sns.heatmap(
        cm, annot=True, fmt="d", cmap="Blues",
        xticklabels=label_encoder.classes_, yticklabels=label_encoder.classes_,
    )
    plt.xlabel("Predicted")
    plt.ylabel("Actual")
    plt.title("Confusion Matrix - Student Performance Prediction")
    plt.tight_layout()
    plt.savefig(os.path.join(ARTIFACT_DIR, "confusion_matrix.png"), dpi=150)
    plt.close()

    # --- Feature importance plot ---
    importances = best_model.feature_importances_
    order = np.argsort(importances)[::-1]
    plt.figure(figsize=(7, 5))
    sns.barplot(
        x=importances[order], y=np.array(FEATURES)[order],
        hue=np.array(FEATURES)[order], palette="viridis", legend=False,
    )
    plt.xlabel("Importance")
    plt.title("Feature Importance - Random Forest")
    plt.tight_layout()
    plt.savefig(os.path.join(ARTIFACT_DIR, "feature_importance.png"), dpi=150)
    plt.close()

    # --- Persist model bundle ---
    bundle = {
        "model": best_model,
        "scaler": scaler,
        "label_encoder": label_encoder,
        "features": FEATURES,
        "category_order": CATEGORY_ORDER,
    }
    model_path = os.path.join(ARTIFACT_DIR, "performance_model.pkl")
    joblib.dump(bundle, model_path)
    print(f"\nSaved model bundle to {model_path}")

    metrics = {
        "accuracy": accuracy,
        "precision": precision,
        "recall": recall,
        "f1_score": f1,
        "best_params": grid.best_params_,
        "n_train": len(X_train),
        "n_test": len(X_test),
        "feature_importance": dict(zip(FEATURES, importances.tolist())),
    }
    with open(os.path.join(ARTIFACT_DIR, "metrics.json"), "w") as f:
        json.dump(metrics, f, indent=2)
    print(f"Saved metrics to {os.path.join(ARTIFACT_DIR, 'metrics.json')}")


if __name__ == "__main__":
    main()