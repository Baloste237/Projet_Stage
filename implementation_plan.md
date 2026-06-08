# Architecture de Scan Asynchrone

Ce document détaille le plan de migration pour passer d'un système de scan synchrone à une architecture asynchrone professionnelle avec suivi en temps réel.

## 1. Modifications Backend (Spring Boot)

### 1.1 Configuration Asynchrone
- Ajout de l'annotation `@EnableAsync` dans la configuration Spring Boot (`BackendApplication.java` ou une nouvelle classe de configuration).
- Création d'un service dédié `AsyncScanProcessor` avec des méthodes annotées `@Async` pour exécuter le scan en arrière-plan et ne pas bloquer le thread HTTP principal.

### 1.2 Entité `AbstractScan` et DB
Ajout de nouveaux champs pour suivre précisément l'état d'un scan :
- `progress` (int) : pourcentage de progression (0-100).
- `currentStep` (String) : étape actuelle du scan (ex: "Extraction", "Analyse IA", "Génération du rapport").
- `logs` (String / TEXT) : historique des logs simplifiés du scan.
- `startedAt` (LocalDateTime) : date de début réel du scan.
- `executionTime` (Long) : temps d'exécution en millisecondes.

**Mise à jour de `ScanStatus` :**
L'énumération actuelle (`PENDING`, `PROCESSING`, `DONE`, `FAILED`) sera mise à jour pour correspondre aux standards demandés :
`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`.
> [!WARNING]
> Nous exécuterons une requête SQL de migration au démarrage ou manuellement pour mettre à jour les enregistrements existants de la base de données (`PROCESSING` -> `RUNNING`, `DONE` -> `COMPLETED`) afin d'éviter des erreurs JPA.

### 1.3 Nouveaux Endpoints REST
Création dans `AppScanController` de :
- `GET /api/v1/analyze/{id}/status` : retourne uniquement le statut.
- `GET /api/v1/analyze/{id}/progress` : retourne un DTO contenant `progress`, `currentStep`, `status`, et `logs`.
- `GET /api/v1/analyze/running` : retourne la liste des scans en cours d'exécution.

Le endpoint `POST /api/v1/analyze/{appType}` retournera immédiatement un code 202 Accepted avec l'ID du scan, pendant que le traitement continuera en arrière-plan.

## 2. Modifications Frontend (React)

### 2.1 Pages de Scan (Web & Mobile)
- **Soumission :** Le bouton "Lancer le scan" fera une simple requête POST, recevra l'ID, et ne bloquera plus le navigateur.
- **Polling (Suivi) :** Mise en place d'un hook (`setInterval` ou `useQuery` court) qui appellera `GET /api/v1/analyze/{id}/progress` toutes les 3 secondes si un scan est en cours.
- **Interface UI :** Ajout d'une barre de progression réelle basée sur les données du backend, de l'étape en cours, et d'une petite console de logs simplifiés.
- **Persistance :** Au chargement de la page, un appel à `/api/v1/analyze/running` permettra de reprendre le suivi (polling) des scans non terminés, même si l'utilisateur a fermé l'onglet et est revenu.

### 2.2 Dashboard Admin
- Mise à jour de `Dashboard.tsx` pour afficher un bloc spécifique : "Scans en cours".
- Ajout de statistiques différenciant les scans terminés, échoués, et en cours.

## 3. Plan de Vérification

### Tests Automatisés et Manuels
1. Lancer un scan Web volumineux et naviguer vers le Dashboard. Revenir sur "Scan Web" et vérifier que la progression continue de s'afficher.
2. Vérifier que la table `users` et `abstract_scans` n'ont aucune corruption de contrainte suite à la mise à jour des enums.
3. Simuler une erreur de scan (ex: fichier invalide ou timeout FastAPI) et vérifier que le statut passe bien en `FAILED` et s'affiche proprement côté frontend.

> [!IMPORTANT]
> Avez-vous des préférences concernant la conservation de l'énumération exacte en base de données ? Voulez-vous que je crée un script SQL pour mettre à jour les données existantes de `DONE` vers `COMPLETED` ?
