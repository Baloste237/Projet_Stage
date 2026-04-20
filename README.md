# 🛡️ App Shield Pro - Guide de Démarrage Global

Ce projet est une plateforme complète d'analyse de vulnérabilités (SAST/DAST) composée de plusieurs microservices de nouvelle génération. 
Ce guide vous guidera étape par étape pour démarrer le projet entier, soit manuellement (pour le développement), soit via Docker.

---

## 🏗️ Architecture du Projet

Votre projet est divisé en **3 composants principaux** (plus les outils tiers) :

1. **Frontend (`/app-shield-pro`)** : L'interface utilisateur Web propulsée par Vite et Bun.
2. **Backend API (`/backend`)** : L'application principale en Java **Spring Boot** (Port `8081`). Gère la logique des utilisateurs, le stockage en base de données PostgreSQL, et délègue les tâches de scan.
3. **Moteur SAST (`/sast_engine`)** : Microservice Python **FastAPI** (Port `8000`). Utilise notre architecture Hybride (Moteur de Règles Heuristiques + Intelligence Artificielle SVM) pour scanner le code source.
4. **Services Tiers** : Base de données (PostgreSQL) et moteur de scan mobile (MobSF).

---

## 💻 Option 1 : Démarrage Manuel (Recommandé pour coder en Local)

C'est la technique utilisée lorsque vous voulez modifier du code et voir les conséquences en direct.

### Étape 1 : Démarrer la base de données PostgreSQL & MobSF
Puisque installer la base de données à la main sur Windows est contraignant, utilisez Docker juste pour la base de données :
```bash
# À la racine du projet
docker-compose up -d postgres mobsf
```

### Étape 2 : Démarrer le Moteur SAST (Python / IA)
Ouvrez un **nouveau terminal**, et lancez l'API FastAPI :
```bash
cd sast_engine

# Activation de l'environnement virtuel (Si pas déjà fait)
.venv\Scripts\activate

# Installation des dépendances
pip install -r requirements.txt

# Lancement du serveur FastAPI
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```
*L'API SAST IA sera accessible sur `http://localhost:8000/docs`*

### Étape 3 : Démarrer le Backend (Spring Boot)
Ouvrez un **nouveau terminal**, et lancez le backend Java :
```bash
cd backend
./mvnw spring-boot:run
```
*Le Backend sera accessible sur `http://localhost:8081`*

### Étape 4 : Démarrer le Frontend (Dashboard Web)
Ouvrez un **dernier terminal**, lancez Bun pour servir le front :
```bash
cd app-shield-pro
bun install
npm run dev
```
*(Regardez dans le terminal, il vous donnera l'URL locale, souvent `http://localhost:5173` ou `3000`)*

---

## 🐳 Option 2 : Démarrage Via Docker (Déploiement Complet)

> **Note :** Actuellement, le `docker-compose.yml` ne contient que le Backend, PostgreSQL et MobSF. Le moteur Python `sast_engine` et le front `app-shield-pro` n'y sont pas encore intégrés, il faudra donc les lancer manuellement à côté (comme montré dans l'Option 1).

Si vous souhaitez allumer toute la partie couverte par Docker, voici la démarche :

### 1. Démarrer les conteneurs (Backend + BDD + MobSF)
```bash
cd e:\Projet_stage\Projet_Stage
docker-compose up -d
```

### 2. Vérifier que tout est "UP"
```bash
docker-compose ps
```
Vous devriez voir :
- ✅ `vuln_scanner_db` (Base de données PostgreSQL sur le port `5433`)
- ✅ `mobsf` (Scanner mobile externe sur le port `8008`)
- ✅ `vuln_scanner_backend` (API Backend sur le port `8081`)

### 3. Gérer les conteneurs
Si vous voulez consulter les logs (erreurs) du backend :
```bash
docker-compose logs -f backend
```

Pour tout éteindre de manière propre sans détruire les bases de données :
```bash
docker-compose stop
```

---

## 🐛 En cas de Problème (Troubleshooting)

1. **Port déjà utilisé** :
   *Le port 8000 (SAST Python) ou 8081 (Backend Java) ou 5433 (BDD Docker) est déjà pris.*
   - Allez les changer dans le code ou modifiez le fichier `.env` si vous l'utilisez.
2. **Le Python FastAPI ne trouve pas les `.pkl`** :
   - Vous devez obligatoirement lancer `uvicorn` en étant DANS le répertoire `sast_engine` pour que les chemins relatifs vers `app/ml/` se créent au bon endroit.
3. **Vous touchez au code du Backend (Option 2)** :
   - Si vous lancez le backend via docker, il faut forcer Docker à recompiler le Java à chaque changement avec `docker-compose up -d --build backend`.
