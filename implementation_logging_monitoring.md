# 📝 Rapport d'Implémentation : Logging & Monitoring

Ce document détaille l'architecture et les composants mis en place pour le système de monitoring et d'audit logging de la plateforme VulnScan.

---

## 1. Architecture Backend (Spring Boot)

Le système backend est conçu pour être non-intrusif, utilisant la Programmation Orientée Aspect (AOP) pour capturer les événements système sans modifier le code métier.

### Composants Clés
*   **Audit Engine (`LoggingAspect.java`)** : Utilise Spring AOP pour intercepter les méthodes critiques (Login, Scan, Rapports).
*   **Persistance (`AuditLog.java`)** : Entité JPA stockant les détails de chaque action (Utilisateur, Action, Endpoint, Status, Timestamp).
*   **Service de Diffusion (`AuditLogService.java`)** : Gère l'enregistrement asynchrone (`@Async`) et la diffusion en temps réel.
*   **Infrastructure WebSocket (`WebSocketConfig.java`)** : Configure l'endpoint `/api/ws-admin` avec support SockJS pour les mises à jour en direct.

---

## 2. Architecture Frontend (React + Vite)

L'interface d'administration offre une visibilité complète sur l'état du système via des tableaux de bord dynamiques.

### Composants Clés
*   **Admin Dashboard (`AdminDashboard.tsx`)** : Point d'entrée pour la gestion des utilisateurs et des configurations.
*   **Audit Logs (`AdminLogs.tsx`)** : Vue historique avec pagination, filtres et export CSV.
*   **Live Monitor (`AdminLive.tsx`)** : Flux d'activité en temps réel connecté via `@stomp/stompjs`.
*   **Executive Metrics (`AdminMonitoringDashboard.tsx`)** : Visualisation des indicateurs clés (erreurs, activité).

---

## 3. Sécurité & Optimisations

*   **Contrôle d'Accès** : Tous les endpoints de monitoring sont restreints aux utilisateurs possédant le rôle `ROLE_ADMIN`.
*   **Traitement Asynchrone** : L'utilisation de `@Async` garantit que le logging n'impacte jamais les performances des scans.
*   **Compatibilité Navigateur** : Configuration spécifique dans `vite.config.ts` (polyfill `global`) pour assurer le bon fonctionnement des WebSockets sur tous les navigateurs modernes.

---

## 4. Flux de Données

1.  **Action** : Un utilisateur effectue une action (ex: lance un scan).
2.  **Interception** : L'AOP capture l'action et ses paramètres.
3.  **Traitement** : Le service de log enregistre l'action en base de données de manière asynchrone.
4.  **Diffusion** : L'événement est envoyé via WebSocket vers le topic `/topic/logs`.
5.  **Mise à jour** : Les navigateurs des administrateurs connectés se mettent à jour instantanément.
