# Guide Configuration OAuth2

## 🔧 Configuration des Applications OAuth2

### 1. Google OAuth2

1. **Allez sur** : [Google Cloud Console](https://console.cloud.google.com/)
2. **Créez un projet** ou sélectionnez-en un existant
3. **Activez l'API Google+** : APIs & Services > Library > Google+ API
4. **Créez des identifiants** :
   - APIs & Services > Credentials > Create Credentials > OAuth 2.0 Client IDs
   - Application type: Web application
   - Authorized redirect URIs: `http://localhost:8081/login/oauth2/code/google`
5. **Notez** : Client ID et Client Secret

### 2. GitHub OAuth2

1. **Allez sur** : [GitHub Developer Settings](https://github.com/settings/developers)
2. **Cliquez** : "New OAuth App"
3. **Remplissez** :
   - Application name: Votre App Name
   - Homepage URL: `http://localhost:3000`
   - Authorization callback URL: `http://localhost:8081/login/oauth2/code/github`
4. **Notez** : Client ID et Client Secret

### 3. GitLab OAuth2

1. **Allez sur** : [GitLab Applications](https://gitlab.com/-/profile/applications)
2. **Cliquez** : "New application"
3. **Remplissez** :
   - Name: Votre App Name
   - Redirect URI: `http://localhost:8081/login/oauth2/code/gitlab`
   - Scopes: `read_user`, `read_api`
4. **Notez** : Application ID (Client ID) et Secret

## 🔑 Remplacement des Placeholders

Remplacez dans `src/main/resources/application.properties` :

```properties
# Google
spring.security.oauth2.client.registration.google.client-id=VOTRE_CLIENT_ID_GOOGLE
spring.security.oauth2.client.registration.google.client-secret=VOTRE_CLIENT_SECRET_GOOGLE

# GitHub
spring.security.oauth2.client.registration.github.client-id=VOTRE_CLIENT_ID_GITHUB
spring.security.oauth2.client.registration.github.client-secret=VOTRE_CLIENT_SECRET_GITHUB

# GitLab
spring.security.oauth2.client.registration.gitlab.client-id=VOTRE_CLIENT_ID_GITLAB
spring.security.oauth2.client.registration.gitlab.client-secret=VOTRE_CLIENT_SECRET_GITLAB
```

## 🧪 Test des Flux OAuth2

### Depuis le Frontend

1. **Boutons de connexion** :
   ```javascript
   // Redirection vers OAuth2
   const handleGoogleLogin = () => {
     window.location.href = 'http://localhost:8081/oauth2/authorization/google';
   };
   ```

2. **Gestion du callback** :
   ```javascript
   // Dans votre composant de redirection
   useEffect(() => {
     const urlParams = new URLSearchParams(window.location.search);
     const token = urlParams.get('token');
     const provider = urlParams.get('provider');

     if (token) {
       localStorage.setItem('jwt_token', token);
       // Rediriger vers dashboard
     }
   }, []);
   ```

### Test Manuel

1. **Démarrez l'application** : `./mvnw spring-boot:run`
2. **Allez sur** : `http://localhost:8081/oauth2/authorization/google`
3. **Authentifiez-vous** et vérifiez la redirection

## 🚨 Gestion d'Erreurs

### Erreurs Gérées

- **Échec d'authentification** : Redirection vers `/oauth2/error`
- **Attributs manquants** : Gestion graceful avec fallbacks
- **Exceptions pendant le traitement** : Logging et redirection d'erreur

### Codes d'Erreur Possibles

- `access_denied` : Utilisateur a refusé l'accès
- `invalid_request` : Requête malformée
- `unauthorized_client` : Client non autorisé
- `unsupported_response_type` : Type de réponse non supporté

## 🔄 Refresh Tokens (Optionnel)

Pour implémenter les refresh tokens :

### 1. Étendez le JWT Service

```java
// Dans JWTService
public String generateRefreshToken(String username) {
    return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 jours
            .signWith(getKey())
            .compact();
}

public boolean validateRefreshToken(String token) {
    // Validation similaire au JWT normal
}
```

### 2. Ajoutez un Endpoint Refresh

```java
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    if (jwtService.validateRefreshToken(refreshToken)) {
        String username = jwtService.extractUsername(refreshToken);
        String newAccessToken = jwtService.generateToken(username);
        return ResponseEntity.ok(new JwtResponse(newAccessToken, refreshToken));
    }
    return ResponseEntity.status(401).body("Invalid refresh token");
}
```

### 3. Stockage des Refresh Tokens

Ajoutez une table pour stocker les refresh tokens de manière sécurisée.

## 📋 Checklist Déploiement

- [ ] Clés API configurées en production
- [ ] URLs de redirection mises à jour pour prod
- [ ] HTTPS activé pour OAuth2
- [ ] Gestion des sessions stateless vérifiée
- [ ] Logs d'erreur monitorés
- [ ] Tests d'intégration OAuth2 ajoutés