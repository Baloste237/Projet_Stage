# Authentification OAuth2 - Guide Complet

## 🎯 Vue d'ensemble

Votre application supporte maintenant l'authentification hybride :
- **JWT traditionnel** pour les comptes locaux
- **OAuth2** pour Google, GitHub et GitLab

## 🔧 Configuration Requise

### 1. Applications OAuth2

Suivez le guide dans `OAUTH2_SETUP_GUIDE.md` pour créer vos applications OAuth2.

### 2. Variables d'environnement

Remplacez les placeholders dans `application.properties` :

```properties
# Production - Remplacez par vos vraies clés
spring.security.oauth2.client.registration.google.client-id=VOTRE_CLIENT_ID_GOOGLE
spring.security.oauth2.client.registration.google.client-secret=VOTRE_CLIENT_SECRET_GOOGLE

spring.security.oauth2.client.registration.github.client-id=VOTRE_CLIENT_ID_GITHUB
spring.security.oauth2.client.registration.github.client-secret=VOTRE_CLIENT_SECRET_GITHUB

spring.security.oauth2.client.registration.gitlab.client-id=VOTRE_CLIENT_ID_GITLAB
spring.security.oauth2.client.registration.gitlab.client-secret=VOTRE_CLIENT_SECRET_GITLAB
```

## 🌐 Endpoints Disponibles

### Authentification JWT (existante)
- `POST /api/auth/register` - Inscription utilisateur local
- `POST /api/auth/login` - Connexion utilisateur local

### Authentification OAuth2 (nouveau)
- `GET /oauth2/authorization/google` - Démarrer OAuth2 Google
- `GET /oauth2/authorization/github` - Démarrer OAuth2 GitHub
- `GET /oauth2/authorization/gitlab` - Démarrer OAuth2 GitLab
- `GET /api/auth/oauth2/user` - Récupérer token utilisateur OAuth2
- `GET /api/auth/oauth2/error` - Gestion des erreurs OAuth2

## 🔄 Flux OAuth2

### 1. Initiation
```javascript
// Depuis le frontend
window.location.href = 'http://localhost:8081/oauth2/authorization/google';
```

### 2. Authentification Provider
- Utilisateur redirigé vers Google/GitHub/GitLab
- Consentement demandé
- Retour vers votre app avec code d'autorisation

### 3. Callback et Token
- Backend échange le code contre un token d'accès
- Récupère les informations utilisateur
- Crée/utilise l'utilisateur en base
- Génère un JWT
- Redirige vers : `http://localhost:3000/oauth2/redirect?token={jwt}&provider={provider}`

### 4. Traitement Frontend
```javascript
// Dans OAuth2Callback.tsx
const token = urlParams.get('token');
if (token) {
  localStorage.setItem('jwt_token', token);
  navigate('/dashboard');
}
```

## 🚨 Gestion d'Erreurs

### Types d'Erreurs
- **Access Denied** : Utilisateur refuse l'accès
- **Invalid Request** : Configuration incorrecte
- **Network Error** : Problème de connectivité
- **Token Expired** : Token OAuth2 expiré

### Gestion Frontend
```javascript
const error = urlParams.get('error');
if (error) {
  // Afficher message d'erreur
  setError(`Erreur OAuth2: ${error}`);
}
```

## 🔒 Sécurité

### Tokens JWT
- **Expiration** : 24 heures par défaut
- **Stockage** : localStorage (à remplacer par httpOnly cookies en prod)
- **Refresh** : Non implémenté (optionnel)

### OAuth2
- **Scopes** : Limité au minimum nécessaire
- **State** : Non utilisé (à ajouter pour sécurité)
- **PKCE** : Non utilisé (recommandé pour SPAs)

## 🧪 Tests

### Tests Backend
```bash
./mvnw test
```

### Tests Frontend
```javascript
// Simuler OAuth2 callback
window.location.href = 'http://localhost:3000/oauth2/redirect?token=fake_jwt&provider=google';
```

## 📋 Checklist Déploiement

- [ ] Clés API configurées
- [ ] URLs de redirection mises à jour
- [ ] HTTPS activé
- [ ] CORS configuré
- [ ] Gestion d'état ajoutée (PKCE)
- [ ] Cookies sécurisés pour tokens
- [ ] Monitoring des erreurs OAuth2

## 🔄 Améliorations Futures

### Refresh Tokens
```java
// Dans JWTService
public String generateRefreshToken(String username) {
    return Jwts.builder()
            .subject(username)
            .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
            .signWith(getKey())
            .compact();
}
```

### Gestion d'État (PKCE)
- Générer un code challenge
- Valider lors du callback

### Multi-tenancy
- Support de plusieurs configurations OAuth2
- Configuration par environnement

## 🆘 Dépannage

### Erreur "redirect_uri_mismatch"
- Vérifiez l'URL de redirection dans la config OAuth2
- Assurez-vous qu'elle correspond exactement

### Erreur "invalid_client"
- Vérifiez le Client ID et Secret
- Assurez-vous que l'application est publiée

### Token manquant
- Vérifiez les logs du backend
- Assurez-vous que l'utilisateur a consenti

---

## 📞 Support

Pour des problèmes spécifiques :
1. Vérifiez les logs du backend (`logs/`)
2. Consultez la console développeur du navigateur
3. Testez les endpoints avec Postman