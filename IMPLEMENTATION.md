# Sécurisation JWT & Dockerisation - Plateforme E-Commerce

## 📋 Résumé des modifications

Implémentation complète de la sécurisation JWT/RSA et dockerisation pour le microservice **Order** basée sur l'architecture réalisée dans le microservice **Product**.

### Modifications apportées

#### 1. ✅ Sécurité JWT (ms-order)

**Fichiers créés:**
- `src/main/java/com/ecommerce/order/security/JwtTokenValidator.java` - Validation des JWT signés RSA
- `src/main/java/com/ecommerce/order/security/JwtAuthentificationFilter.java` - Filtre Spring Security
- `src/main/java/com/ecommerce/order/security/SecurityConfig.java` - Configuration Spring Security
- `src/main/java/com/ecommerce/order/settings/InfraSetting.java` - Chargement des clés RSA
- `src/main/java/com/ecommerce/order/domain/entity/User.java` - Entité User pour JWT claims

**Fichiers modifiés:**
- `pom.xml` - Ajout des dépendances:
  - `com.nimbusds:nimbus-jose-jwt:9.37.3` - Librairie JWT
  - `org.springframework.boot:spring-boot-starter-security` - Spring Security

#### 2. ✅ Configuration (ms-order)

**application.yml enrichi avec:**
- Configuration Actuator complète (health, metrics, prometheus, info)
- Logs au niveau DEBUG pour la sécurité
- Gestion des erreurs et compression
- Support OpenAPI/Swagger

#### 3. ✅ Docker

**Fichiers créés:**
- `docker/build-all.sh` - Script pour compiler et builder les 3 services
- `docker/publish-all.sh` - Script pour publier sur Docker Hub
- `docker/deploy.sh` - Script pour déployer depuis Docker Hub

**Fichiers existants (compatibles):**
- `docker-compose.yml` - Déjà configuré pour ms-order
- `ms-order/Dockerfile` - Build multi-stage (Maven + JRE)

#### 4. ✅ Documentation

**Fichiers créés:**
- `SECURITY.md` - Architecture complète de sécurité JWT/RSA
  - Flux d'authentification
  - Structure du JWT
  - Validation
  - Gestion des clés
  - Configuration Spring Security
  - Cycle de vie du JWT
  - Dépannage

- `DOCKER.md` - Guide complet de dockerisation
  - Génération des clés RSA
  - Build des images
  - Publication Docker Hub
  - Déploiement avec docker-compose
  - Commandes essentielles
  - Configuration des repositories privés
  - Dépannage

#### 5. ✅ Tests Postman

**Fichier créé:**
- `postman/platform-secured.json` - Collection Postman sécurisée avec:
  - Tests de login (génération JWT)
  - Tests d'accès avec token valide (200 OK)
  - Tests de rejet sans token (401 Unauthorized)
  - Tests de rejet avec token invalide (401)
  - Tests des endpoints publics
  - Scénario intégré complet

---

## 🔒 Architecture de Sécurité

### Flux d'authentification

```
1. Client →[POST /auth/login] → Membership
2. Membership →[Signe avec clé privée RSA] → JWT
3. JWT → Client (token + expires_in)
4. Client →[GET /products + Bearer JWT] → Product/Order
5. Product/Order →[Valide avec clé publique RSA] → Authentification OK
```

### Détails techniques

- **Algorithme:** RS256 (RSA-256)
- **Clé privée:** 2048 bits (stockée dans `/app/keys/server.p12`)
- **Clé publique:** Extraite du certificat
- **Format:** PKCS12 (server.p12)
- **Expiration:** 1 heure (3600 secondes)
- **Stateless:** Pas de session serveur

### Claims JWT

```json
{
  "sub": "username",
  "iss": "episen-e-commerce",
  "aud": ["web"],
  "UserId": 1,
  "Email": "user@example.com",
  "Roles": ["USER", "ADMIN"],
  "exp": 1704100000
}
```

---

## 🚀 Démarrage rapide

### 1. Générer les clés RSA (une seule fois)

```bash
# Depuis la racine du projet
mkdir -p keys

# Créer la clé privée
openssl genrsa -out keys/private.key 2048

# Créer le certificat auto-signé
openssl req -new -x509 -key keys/private.key -out keys/certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"

# Convertir en PKCS12
openssl pkcs12 -export -in keys/certificate.crt -inkey keys/private.key \
  -out keys/server.p12 -name jil -password pass:jil

# Vérifier
ls -la keys/server.p12
```

### 2. Builder et lancer les services

```bash
# Option A: Script automatisé
bash docker/build-all.sh
docker-compose up -d

# Option B: Manuel
mvn clean package -DskipTests
docker-compose up -d
```

### 3. Tester avec Postman

1. Importer la collection: `postman/platform-secured.json`
2. Exécuter le scénario: **"8. Scénario Complet Intégré"**
3. Ou exécuter individuellement:
   - 3.1 Créer un utilisateur
   - 3.2 Login (obtenir JWT)
   - 4.1 GET Products (avec token)
   - 4.2 POST Order (avec token)

### 4. Vérifier la sécurité

```bash
# Accès sans token (401)
curl -X GET http://localhost:8082/api/v1/products

# Accès avec token valide (200)
TOKEN="$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}' | jq -r '.token')"

curl -X GET http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📦 Déploiement Docker Hub

### 1. Créer un compte Docker Hub

- Visiter https://hub.docker.com
- S'inscrire (gratuit)

### 2. Créer 3 repositories privés

Nommés:
- `ecommerce-membership`
- `ecommerce-product`
- `ecommerce-order`

### 3. Publier les images

```bash
# Configuration
export DOCKER_HUB_USERNAME="votre-username"

# Login Docker
docker login

# Build et push
bash docker/build-all.sh
bash docker/publish-all.sh
```

### 4. Partager l'accès

Pour chaque repository:
1. Settings → Collaborators
2. Ajouter l'username de l'enseignant
3. Accès: Read-only

---

## 📂 Structure finale du projet

```
ecommerce-platform/
├── docker/
│   ├── build-all.sh              ✨ Nouveau
│   ├── publish-all.sh            ✨ Nouveau
│   └── deploy.sh                 ✨ Nouveau
├── keys/
│   └── server.p12                (À générer)
├── ms-membership/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/java/com/membership/...
│           ├── security/
│           │   └── JwtTokenGenerator.java
│           └── application/
│               └── AuthController.java    (Endpoint /auth/login)
│
├── ms-product/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/java/com/episen/ms_product/
│           ├── security/               ✅ Implémenté
│           │   ├── JwtTokenValidator.java
│           │   ├── JwtAuthentificationFilter.java
│           │   └── SecurityConfig.java
│           ├── settings/
│           │   └── InfraSetting.java
│           └── domain/entity/User.java
│
├── ms-order/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/java/com/ecommerce/order/
│           ├── security/               ✨ Nouveau
│           │   ├── JwtTokenValidator.java
│           │   ├── JwtAuthentificationFilter.java
│           │   └── SecurityConfig.java
│           ├── settings/              ✨ Nouveau
│           │   └── InfraSetting.java
│           └── domain/entity/User.java ✨ Nouveau
│
├── postman/
│   ├── platform-tests.json       (Ancien)
│   └── platform-secured.json     ✨ Nouveau
│
├── docker-compose.yml            (Existant, compatible)
├── prometheus.yml
├── monitoring/
├── SECURITY.md                   ✨ Nouveau
├── DOCKER.md                     ✨ Nouveau
└── README.md
```

---

## 🧪 Scénarios de test

### Scénario 1: Authentication Flow
```
1. POST /api/v1/users          → 201 (créer user)
2. POST /api/v1/auth/login     → 200 (obtenir JWT)
3. GET  /products (+ token)    → 200 (accès autorisé)
```

### Scénario 2: Unauthorized Access
```
1. GET /products (SANS token)  → 401 (rejeté)
2. GET /products (token fake)  → 401 (rejeté)
```

### Scénario 3: Order Workflow Complet
```
1. Login                       → JWT
2. GET /products (+ JWT)       → Product list
3. POST /orders (+ JWT)        → Order créé
4. GET /orders/{id} (+ JWT)    → Détails order
```

---

## 🔧 Commandes essentielles

### Docker

```bash
# Build les images
docker-compose build

# Démarrer les services
docker-compose up -d

# Voir les logs
docker-compose logs -f order

# Arrêter les services
docker-compose down

# Nettoyer
docker-compose down -v
```

### Vérifications

```bash
# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health

# Prometheus
curl http://localhost:9090/metrics

# Grafana
open http://localhost:3000  # admin/admin123
```

---

## 📚 Documentation

- **[SECURITY.md](./SECURITY.md)** - Architecture de sécurité JWT/RSA détaillée
- **[DOCKER.md](./DOCKER.md)** - Guide complet de dockerisation
- **[TP2_SECURITE_DOCKER.md](./TP2_SECURITE_DOCKER.md)** - Énoncé du TP

---

## ✅ Checklist de livraison

- [x] Sécurisation JWT/RSA implémentée sur ms-order
- [x] Filtre JWT et validation des tokens
- [x] Configuration Spring Security
- [x] Chargement des clés RSA PKCS12
- [x] Docker Compose configuré
- [x] Scripts de build et déploiement
- [x] Documentation SECURITY.md complète
- [x] Documentation DOCKER.md complète
- [x] Collection Postman sécurisée avec tests
- [x] Structure de dossiers organisée

---

## 🐛 Dépannage

### "Token cannot be verified"
→ Vérifier que les clés RSA sont identiques sur tous les services

### "401 Unauthorized"
→ Vérifier que le header Authorization contient "Bearer <token>"

### Port déjà utilisé
→ Modifier les ports dans docker-compose.yml ou tuer le processus

### Services ne communiquent pas
→ Vérifier le réseau Docker: `docker network ls`

Plus de détails dans [SECURITY.md](./SECURITY.md#dépannage) et [DOCKER.md](./DOCKER.md#dépannage)

---

## 👤 Auteur

Implémentation pour l'TP2 - Sécurisation JWT & Dockerisation
Date: Janvier 2026

---

**Bonne chance pour votre soutenance ! 🎯**
