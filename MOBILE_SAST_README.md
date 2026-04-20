# Documentation : Configuration et Utilisation du Scan SAST Mobile (MobSF)

Cette documentation explique étape par étape comment démarrer, configurer et tester le scan de vulnérabilités pour les applications mobiles (fichiers `.apk`) via le moteur **MobSF** intégré à notre backend Spring Boot.

---

## 📋 Prérequis
- **Docker** et **Docker Desktop** installés et en cours d'exécution.
- Avoir un fichier application mobile de test (ex: `inSecureBankv2.apk`).
- Frontend React et Backend Spring Boot fonctionnels.

---

## 🚀 ÉTAPE 1 : Démarrer le moteur MobSF (Docker)

MobSF est une plateforme puissante qui tourne de manière conteneurisée. Ouvrez un terminal (Powershell, CMD, ou Bash) et exécutez la commande suivante :

```bash
docker run -it --rm -p 8008:8000 opensecurity/mobile-security-framework-mobsf
```

*(Nous redirigeons le port 8000 du conteneur vers le port **8008** de votre machine locale pour éviter les conflits).*

Laissez le terminal ouvert. Dès que vous verrez un message similaire à `[INFO] Listening at: http://0.0.0.0:8000`, MobSF est prêt !

---

## 🔑 ÉTAPE 2 : Générer & Récupérer la clé API (API KEY)

L'API de MobSF requiert une authentification obligatoire par Header.
1. Ouvrez votre navigateur internet et allez sur : **[http://localhost:8008/api_docs](http://localhost:8008/api_docs)**
2. En haut de la page Swagger, MobSF affichera en clair votre **REST API Key**.
3. **Copiez cette clé** dans votre presse-papiers.

---

## ⚙️ ÉTAPE 3 : Configuration de Spring Boot

Avant de démarrer l'API, vous devez modifier très légèrement le fichier de propriétés de Spring Boot pour y injecter la clé et un temps d'attente cohérent vis-à-vis des requêtes lourdes.

Dans le fichier `backend/src/main/resources/application.properties`, repérez la section `# MobSF` et adaptez les valeurs ainsi :

```properties
# Configuration de MobSF pour le SAST Mobile
mobsf.url=http://localhost:8008
mobsf.api.key=VOUS_COLLEREZ_LA_CLE_ICI
mobsf.connect-timeout=30
mobsf.read-timeout=600   # IMPORTANT: Mis à 600s (10 min) car une analyse MobSF peut être longue.
```

Sauvegardez le fichier.

---

## ▶️ ÉTAPE 4 : Démarrer le projet et Tester de bout-en-bout

Maintenant que tout est branché, vous pouvez passer au test grandeur nature.

1. **Lancez votre Backend Spring Boot** (`mvn spring-boot:run` ou via votre IDE).
2. Vérifiez dans votre console que l'application démarre fièrement sur le port (ex: `8081`).
3. **Lancez votre Frontend (React)** et naviguez vers l'onglet du Scan Mobile.

### Via l'Interface Graphique (Frontend App-Shield-Pro)
- Uploadez votre fichier `.apk`.
- Cliquez sur "Démarrer le scan".
- Attendez que l'analyse se termine (Gardez vos yeux sur la console Spring Boot !).

### Via Postman (Alerte Optionnelle pour développeur)
Si vous voulez tester le backend indépendamment du frontend :
- **Requête** : `POST http://localhost:8081/api/v1/scans/mobile` (Adaptez la route selon votre controller).
- **Body** : Format `form-data`.
- **Clé** : `file` (Type "File", pointez vers votre fichier apk).
- **Envoyer** la requête. Le résultat sera retourné une fois le parsing MobSF achevé.

---

## 🧩 Ce passe-t-il sous le capot ? (Le flux d'exécution)

1. **L'Upload Sécurisé :**
   Le `MobSFClient` de l'API intercepte le fichier et le charge obligatoirement en RAM (via un `ByteArrayResource`) pour éviter les timeouts et forcer l'envoi multipart (`multipart/form-data`).
2. **Scan Statique Profilé :**
   Une fois réceptionné, MobSF décompile et inspecte la globalité du code (Manifeste, Bytecode, Dépendances, etc.). La fonction `lancerAnalyse` de `MobSFClient` se gèle (timeout jusqu'à 10 mins).
3. **Dépouillement des Vulnérabilités (Regex et Sécurité) :**
   Lorsque le scan revient au `AppScanServiceImpl`, il est découpé avec une précision chirurgicale. Les "Findings" et les "Dangerous Permissions" (Permissions Android Insécures de type CWE-276) sont convertis en entités `Vulnerabilite`.
4. **Persistance des Données :**
   Le scan original et l'intégralité de ses failles sont tracés dans la table PostgreSQL correspondante (`TotalVulnerabilities`, `CriticalCount`, etc.) et sont injectés dans l'entité globale `Historique`.
