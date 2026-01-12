# SECURITY.md - Architecture de Sécurité JWT/RSA

## Vue d'ensemble

La plateforme e-commerce implémente une authentification stateless basée sur les **JWT (JSON Web Tokens)** signés avec un chiffrement **RSA asymétrique (2048 bits)**.

### Flux de sécurité

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /api/v1/auth/login
       │    (email + password)
       ↓
┌────────────────────┐        RSA Private Key
│ Membership Service │ ─────────────────────→ Signe JWT
└────────┬───────────┘
         │
         │ 2. Retourne JWT signé
         │    { token: "eyJhbGc...", expiresIn: 3600 }
         ↓
┌─────────────────────────┐
│ Client stocke le token  │
└──────────┬──────────────┘
           │
           │ 3. Authorization: Bearer <token>
           ├────────────────→ Product Service    (avec filtre JWT)
           ├────────────────→ Order Service      (avec filtre JWT)
           └────────────────→ Membership Service (avec filtre JWT)
                              │
                              ↓
                        RSA Public Key
                        Vérifie signature
                        Extrait claims
                        Authentifie utilisateur
```

---

## Architecture de sécurité

### 1. Génération du JWT (Service Membership)

**Endpoint:** `POST /api/v1/auth/login`

**Requête:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Réponse (200 OK):**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsImlzcyI6ImVwaXNlbi1lLWNvbW1lcmNlIiwiYXVkIjpbIndlYiJdLCJVc2VySWQiOjEsIkVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsIlJvbGVzIjpbIlVTRVIiXSwiZXhwIjoxNzA0MTAwMDAwfQ.signature",
  "expiresIn": 3600
}
```

### 2. Structure du JWT

**Header:**
```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "username",                    // Subject (utilisateur)
  "iss": "episen-e-commerce",          // Issuer
  "aud": ["web"],                      // Audience
  "UserId": 1,                         // ID utilisateur
  "Email": "user@example.com",         // Email
  "Roles": ["USER", "ADMIN"],          // Rôles
  "exp": 1704100000                    // Expiration (Unix timestamp)
}
```

**Signature:** Signée avec la clé privée RSA (RS256)

---

## Validation du JWT (Services Product & Order)

### Filtre de sécurité: `JwtAuthentificationFilter`

Le filtre effectue les opérations suivantes :

1. **Extraction du token**
   - Extrait le header `Authorization: Bearer <token>`
   - Retourne `401 Unauthorized` si absent ou mal formé

2. **Validation de la signature**
   - Vérifie la signature avec la clé publique RSA
   - Retourne `401 Unauthorized` si signature invalide

3. **Validation des claims**
   - Vérifie l'expiration (`exp`)
   - Vérifie l'émetteur (`iss = episen-e-commerce`)
   - Vérifie l'audience (`aud = web`)
   - Retourne `401 Unauthorized` si validation échoue

4. **Authentification de l'utilisateur**
   - Extrait les informations: `UserId`, `Email`, `Roles`
   - Crée un `SecurityContext` avec les rôles
   - Propage le contexte à l'application

### Codes de réponse

| Code | Situation | Exemple |
|------|-----------|---------|
| **200 OK** | Token valide, requête acceptée | GET /products |
| **401 Unauthorized** | Token absent, invalide ou expiré | GET /products (sans token) |
| **403 Forbidden** | Token expiré (optionnel) | Retour du filtre |

---

## Gestion des clés RSA

### 1. Structure des clés

Les clés sont stockées dans un fichier **PKCS12** (`.p12`) :

```
keys/
└── server.p12          # Certificat + clé privée
```

**Spécifications:**
- Format: PKCS12
- Alias: `jil`
- Password: `jil`
- Clé privée: 2048 bits RSA
- Clé publique: Extraite du certificat

### 2. Génération des clés (Une seule fois)

#### Créer une clé privée (RSA 2048 bits)
```bash
openssl genrsa -out private.key 2048
```

#### Créer un certificat auto-signé
```bash
openssl req -new -x509 -key private.key -out certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"
```

#### Convertir au format PKCS12
```bash
openssl pkcs12 -export -in certificate.crt -inkey private.key \
  -out server.p12 -name jil -password pass:jil
```

### 3. Distribution des clés

- **Clé privée** : Stockée dans `/app/keys/server.p12`
  - Accès limité au service Membership (génération JWT)
  - Jamais partagée

- **Clé publique** : Extraite du certificat dans `server.p12`
  - Partagée à tous les services (Product, Order)
  - Utilisée pour validation du JWT

### 4. Volume Docker

Dans `docker-compose.yml` :
```yaml
volumes:
  - ./keys:/app/keys
```

Tous les services accèdent aux clés via le répertoire monté.

---

## Configuration Spring Security

### Membership Service - SecurityConfig

Configuration minimale - Spring Boot expose l'endpoint sans authentification par défaut :

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Note:** Le endpoint `/api/v1/auth/login` est accessible publiquement. Pour une sécurité accrue en production, ajouter `@EnableWebSecurity` et `HttpSecurity.authorizeHttpRequests()`.

### Product & Order Services - SecurityConfig

Protège tous les endpoints sauf les health checks et OpenAPI :

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthentificationFilter jwtAuthentificationFilter;

    public SecurityConfig(JwtAuthentificationFilter jwtAuthentificationFilter) {
        this.jwtAuthentificationFilter = jwtAuthentificationFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // Désactiver CSRF (pas pertinent pour les APIs stateless)
            .csrf(csrf -> csrf.disable())
            
            // Pas de session (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Autorisation des requêtes
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            
            // Ajouter le filtre JWT
            .addFilterBefore(jwtAuthentificationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return httpSecurity.build();
    }
}
```

---

## Communication inter-services

### Propagation du JWT

Quand le service Order appelle le service Product :

```java
// Dans OrderService
@Service
public class OrderService {
    
    @Autowired
    private WebClient webClient;
    
    public Product getProductInfo(Long productId, String token) {
        return webClient.get()
            .uri("http://product:8082/products/{id}", productId)
            .header("Authorization", "Bearer " + token)  // Propager le JWT
            .retrieve()
            .bodyToMono(Product.class)
            .block();
    }
}
```

### Gestion des erreurs 401

```java
public Product getProductInfo(Long productId, String token) {
    try {
        return webClient.get()
            .uri("http://product:8082/products/{id}", productId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .onStatus(
                status -> status.value() == 401,
                response -> Mono.error(new UnauthorizedException("Invalid token"))
            )
            .bodyToMono(Product.class)
            .block();
    } catch (UnauthorizedException e) {
        // Log et retourner erreur 401 au client
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
            "Token invalide pour accéder à Product Service");
    }
}
```

---

## Endpoints publics vs protégés

### Publics (sans authentification)

```
POST   /api/v1/auth/login                          (Membership)
GET    /actuator/**                                (Tous les services)
GET    /actuator/health                            (Tous les services)
GET    /v3/api-docs/**                             (OpenAPI)
GET    /swagger-ui.html                            (Swagger UI)
```

### Protégés (authentification requise)

```
GET    /products                                   (Product)
POST   /products                                   (Product)
GET    /orders                                     (Order)
POST   /orders                                     (Order)
... tous les autres endpoints
```

---

## Cycle de vie du JWT

### 1. Création
- Durée: **1 heure** (3600 secondes)
- Signature: RSA-256 (clé privée)
- Audience: `web`

### 2. Stockage côté client
- LocalStorage ou SessionStorage (navigateur)
- Cookie sécurisé (HttpOnly, Secure, SameSite)

### 3. Validation
- À chaque requête protégée
- Vérification: signature + expiration + claims

### 4. Expiration
- **Automatique** après 1 heure
- Client doit relancer le login
- Aucun refresh token (simple)

---

## Sécurité - Best Practices

✅ **Implémenté**
- RSA asymétrique (clé privée secrète)
- Stateless (pas de session serveur)
- JWT signé (non juste encodé)
- Expiration de 1 heure
- Validation complète des claims
- Endpoints `/actuator` exemptés

⚠️ **Recommandations pour la production**
- HTTPS obligatoire
- Refresh tokens pour renouvellement sans re-login
- Stockage des clés dans un vault (HashiCorp Vault, AWS Secrets Manager)
- Rate limiting sur `/auth/login`
- Audit logging des authentifications
- Token blacklist pour les déconnexions
- Rotation régulière des clés

---

## Dépannage

### Erreur: "Token cannot be verified"
**Cause:** Clé publique invalide ou token signé avec une autre clé
**Solution:** Vérifier que les clés RSA sont identiques sur tous les services

### Erreur: "Invalid Token - Token expired"
**Cause:** Token expiré (plus de 1 heure)
**Solution:** Client doit refaire un login pour obtenir un nouveau token

### Erreur: "401 Unauthorized"
**Cause:** Header `Authorization` absent ou mal formé
**Solution:** Envoyer `Authorization: Bearer <token>`

### Service Product/Order retourne 401 en interne
**Cause:** Token non propagé lors de l'appel inter-service
**Solution:** Vérifier que le header `Authorization` est propagé dans `WebClient`

---

## Fichiers concernés

```
ms-membership/
├── src/main/java/com/membership/users/
│   ├── application/
│   │   └── AuthController.java          (Endpoint /auth/login)
│   ├── security/
│   │   └── JwtTokenGenerator.java       (Génère JWT)
│   └── domain/
│       └── entity/User.java

ms-product/
├── src/main/java/com/episen/ms_product/
│   ├── security/
│   │   ├── JwtTokenValidator.java       (Valide JWT)
│   │   ├── JwtAuthentificationFilter.java (Filtre Spring)
│   │   └── SecurityConfig.java
│   ├── settings/
│   │   └── InfraSetting.java            (Charge clés RSA)
│   └── domain/entity/User.java

ms-order/
├── src/main/java/com/ecommerce/order/
│   ├── security/
│   │   ├── JwtTokenValidator.java       (Valide JWT)
│   │   ├── JwtAuthentificationFilter.java (Filtre Spring)
│   │   └── SecurityConfig.java
│   ├── settings/
│   │   └── InfraSetting.java            (Charge clés RSA)
│   └── domain/entity/User.java

keys/
└── server.p12                           (Clés RSA - PKCS12)
```

---

## Références

- [JWT - jwt.io](https://jwt.io)
- [Spring Security - Official Docs](https://spring.io/projects/spring-security)
- [Nimbus JOSE + JWT Library](https://connect2id.com/products/nimbus-jose-jwt)
- [OpenSSL - RSA Key Generation](https://www.openssl.org/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
