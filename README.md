# 🛡️ App Shield Pro — Guide de Démarrage

Ce projet est une plateforme complète d'analyse de vulnérabilités (SAST mobile & web) composée de plusieurs microservices.  
Ce guide vous explique **exactement** comment démarrer le projet, que ce soit manuellement (développement local) ou via Docker.

---

## 🏗️ Architecture du Projet

| Composant | Technologie | Port | Rôle |
|---|---|---|---|
| **Frontend** (`/app-shield-pro`) | Vite + React | `5173` | Interface utilisateur web |
| **Backend API** (`/backend`) | Spring Boot (Java) | `8081` | Logique métier, BDD, orchestration des scans |
| **Moteur SAST** (`/sast_engine`) | FastAPI (Python) | `8000` | Analyse statique de code source (IA + règles) |
| **Base de données** | PostgreSQL | `5433` | Stockage des données |
| **MobSF** | Docker | `8008` | Scanner d'APK Android/iOS |

---

## 💻 Option 1 : Démarrage Manuel (Développement Local)

Recommandé quand vous modifiez du code et voulez voir les changements en direct.

### Pré-requis
- **Java 17+** installé
- **Python 3.10+** installé
- **Bun** installé (`npm install -g bun` ou via [bun.sh](https://bun.sh))
- **Docker Desktop** en cours d'exécution (uniquement pour PostgreSQL et MobSF)

---

### Étape 1 — Démarrer PostgreSQL & MobSF via Docker

Ces deux services sont lancés via Docker car leur installation manuelle est fastidieuse.

```bash
# À la racine du projet
docker-compose up -d postgres mobsf
```

> ✅ **MobSF est maintenant configuré avec une clé API fixe automatiquement.**  
> La clé `mobsf_fixed_api_key_vulnscan_2024` est définie dans le fichier `.env`  
> et injectée automatiquement dans MobSF **et** dans le backend. Pas besoin de la copier-coller.

Vérifiez que les deux conteneurs sont bien démarrés :
```bash
docker-compose ps
```
Attendez que MobSF soit `healthy` avant de continuer (cela peut prendre ~60 secondes).

---

### Étape 2 — Démarrer le Moteur SAST Python

Ouvrez un **nouveau terminal** :

```bash
cd sast_engine

# Activer l'environnement virtuel
.venv\Scripts\activate        # Windows
# source .venv/bin/activate   # Linux / macOS

# Installer les dépendances (seulement la première fois)
pip install -r requirements.txt

# Lancer le serveur FastAPI
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

> ⚠️ Lancez toujours `uvicorn` **depuis le répertoire `sast_engine`** pour que les chemins vers les modèles ML (`.pkl`) soient corrects.

✅ L'API SAST est accessible sur : `http://localhost:8000/docs`

---

### Étape 3 — Démarrer le Backend Spring Boot

Ouvrez un **nouveau terminal** :

```bash
cd backend
./mvnw spring-boot:run
```

> Le backend lira automatiquement la clé MobSF depuis la variable d'environnement  
> `MOBSF_API_KEY` définie dans `.env` (valeur par défaut : `mobsf_fixed_api_key_vulnscan_2024`).

✅ Le Backend est accessible sur : `http://localhost:8081`

---

### Étape 4 — Démarrer le Frontend

Ouvrez un **nouveau terminal** :

```bash
cd app-shield-pro

# Installer les dépendances (seulement la première fois)
bun install

# Lancer le serveur de développement
npm run dev
```

✅ Le Frontend est accessible sur : `http://localhost:5173`

---

### Récapitulatif des ports (Mode Manuel)

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8081 |
| Moteur SAST Python | http://localhost:8000/docs |
| MobSF (interface web) | http://localhost:8008 |
| PostgreSQL | localhost:5433 |

---

## 🐳 Option 2 : Démarrage Via Docker (Backend + BDD + MobSF)

> **Note :** Le `docker-compose.yml` couvre le **Backend**, **PostgreSQL** et **MobSF**.  
> Le moteur Python (`sast_engine`) et le frontend (`app-shield-pro`) doivent être lancés manuellement (voir Étapes 2 et 4 ci-dessus).

### Pré-requis
- **Docker Desktop** en cours d'exécution
- **Bun** installé (pour le frontend)
- **Python 3.11+** (pour le moteur SAST)

---

### Étape 1 — Lancer les conteneurs Docker

```bash
# À la racine du projet (où se trouve docker-compose.yml)
docker-compose up -d
```

La clé API MobSF est **automatiquement partagée** entre MobSF et le backend via le fichier `.env`. Aucune configuration manuelle n'est nécessaire.

---

### Étape 2 — Vérifier que tout est démarré

```bash
docker-compose ps
```

Vous devez voir ces 3 services avec le statut `running` / `healthy` :

| Conteneur | Port | Statut attendu |
|---|---|---|
| `vuln_scanner_db` | `5433` | ✅ healthy |
| `mobsf` | `8008` | ✅ healthy (après ~60s) |
| `vuln_scanner_backend` | `8081` | ✅ healthy |

---

### Étape 3 — Lancer le Moteur SAST Python (manuel)

```bash
cd sast_engine
.venv\Scripts\activate
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

---

### Étape 4 — Lancer le Frontend (manuel)

```bash
cd app-shield-pro
bun install
npm run dev
```

---

### Gestion des conteneurs Docker

```bash
# Voir les logs du backend en temps réel
docker-compose logs -f backend

# Voir les logs de MobSF
docker-compose logs -f mobsf

# Arrêter les conteneurs sans supprimer les données
docker-compose stop

# Redémarrer les conteneurs
docker-compose start

# Reconstruire le backend après modification du code Java
docker-compose up -d --build backend

# Tout arrêter ET supprimer les volumes (reset complet)
docker-compose down -v
```

---

## ⚙️ Configuration — Fichier `.env`

Le fichier `.env` à la racine du projet centralise toute la configuration :

```env
# Base de données
DB_NAME=vuln_scanner
DB_USER=postgres
DB_PASSWORD=admin
DB_PORT=5433

# Backend
BACKEND_PORT=8081

# MobSF — clé API fixe partagée automatiquement entre MobSF et le backend
MOBSF_URL=http://mobsf:8000
MOBSF_API_KEY=mobsf_fixed_api_key_vulnscan_2024
```

> 🔑 **Plus besoin de copier-coller la clé API MobSF.**  
> Elle est définie une seule fois dans `.env` et injectée automatiquement dans tous les services.

---

## 🐛 Troubleshooting

| Problème | Solution |
|---|---|
| Port `8000`, `8081` ou `5433` déjà utilisé | Modifiez les ports dans `.env` ou arrêtez le processus qui les occupe |
| Le moteur SAST ne trouve pas les fichiers `.pkl` | Lancez `uvicorn` **depuis le répertoire `sast_engine`**, pas depuis la racine |
| MobSF retourne `401 Unauthorized` | Vérifiez que `MOBSF_API_KEY` dans `.env` correspond à celle dans `application.properties` |
| MobSF non `healthy` au démarrage | Normal, il prend ~60 secondes. Attendez et relancez `docker-compose ps` |
| Modification du code Java avec Docker | Relancez avec `docker-compose up -d --build backend` pour recompiler |
| Reset complet (données effacées) | `docker-compose down -v` puis `docker-compose up -d` |
