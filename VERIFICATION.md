# ✅ Vérification de l'implémentation JWT & Dockerisation

## Partie 1: Vérification de la sécurité JWT sur ms-order

### 1.1 Fichiers de sécurité créés

```bash
# Vérifier l'existence des fichiers
ls -la ms-order/src/main/java/com/ecommerce/order/security/
ls -la ms-order/src/main/java/com/ecommerce/order/settings/
ls -la ms-order/src/main/java/com/ecommerce/order/domain/entity/User.java
```

**Attendu:**
```
JwtTokenValidator.java
JwtAuthentificationFilter.java
SecurityConfig.java
InfraSetting.java
User.java
```

### 1.2 Dépendances JWT dans pom.xml

```bash
# Vérifier les dépendances
grep -A 2 "nimbus-jose-jwt" ms-order/pom.xml
grep "spring-boot-starter-security" ms-order/pom.xml
```

**Attendu:**
```xml
<groupId>com.nimbusds</groupId>
<artifactId>nimbus-jose-jwt</artifactId>
<version>9.37.3</version>

<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-security</artifactId>
```

### 1.3 Configuration application.yml

```bash
# Vérifier les configurations
grep -A 5 "security:" ms-order/src/main/resources/application.yml
grep "APP_PORT" ms-order/src/main/resources/application.yml
```

**Attendu:**
- Configuration Actuator (health, metrics, prometheus, env, loggers)
- Support OpenAPI/Swagger
- Logs au niveau DEBUG pour sécurité
- Compression HTTP activée

---

## Partie 2: Vérification des scripts et documentation

### 2.1 Scripts Docker

```bash
# Vérifier l'existence des scripts
ls -la docker/build-all.sh
ls -la docker/publish-all.sh
ls -la docker/deploy.sh
```

**Attendu:** Les 3 scripts existent et sont exécutables

### 2.2 Documentation

```bash
# Vérifier les fichiers de documentation
ls -la SECURITY.md
ls -la DOCKER.md
ls -la IMPLEMENTATION.md
```

**Attendu:** Les 3 fichiers existent

**Contenu SECURITY.md:**
- Vue d'ensemble
- Architecture de sécurité
- Génération du JWT
- Validation du JWT
- Gestion des clés RSA
- Configuration Spring Security
- Communication inter-services
- Endpoints publics vs protégés
- Cycle de vie du JWT
- Best practices

**Contenu DOCKER.md:**
- Architecture Docker
- Dockerfile multi-stage
- Docker Compose
- Commandes essentielles
- Génération des clés RSA
- Build et publication
- Déploiement
- Gestion des données
- Configuration Docker Hub
- Dépannage

### 2.3 Collection Postman

```bash
# Vérifier la collection Postman
ls -la postman/platform-secured.json
```

**Attendu:** Le fichier existe et contient:
- Configuration et variables (membersip, product, order URLs)
- Health checks
- Authentification JWT (créer user, login)
- Tests d'accès avec token valide (200 OK)
- Tests de rejet sans token (401)
- Tests avec token invalide (401)
- Tests des endpoints publics (sans auth)
- Scénario complet intégré

---

## Partie 3: Tests fonctionnels

### 3.1 Générer les clés RSA

```bash
# Depuis la racine
mkdir -p keys

openssl genrsa -out keys/private.key 2048
openssl req -new -x509 -key keys/private.key -out keys/certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"
openssl pkcs12 -export -in keys/certificate.crt -inkey keys/private.key \
  -out keys/server.p12 -name jil -password pass:jil

# Vérifier
ls -la keys/server.p12
```

**Attendu:** Fichier `keys/server.p12` créé (environ 3-5 KB)

### 3.2 Builder les services

```bash
# Option 1: Script automatisé
bash docker/build-all.sh

# Option 2: Manuel
cd ms-order
mvn clean package -DskipTests
docker build -t ecommerce-order:1.0 .
```

**Attendu:** 
- Build sans erreur
- Image Docker créée: `ecommerce-order:1.0` (~150 MB)

### 3.3 Lancer docker-compose

```bash
# Vérifier docker-compose.yml
cat docker-compose.yml | grep -A 10 "order:"

# Lancer
docker-compose up -d

# Vérifier les services
docker-compose ps
```

**Attendu:**
```
NAME                COMMAND                  SERVICE   STATUS
membership_service  java -jar app.jar        membership  Up
product_service     java -jar app.jar        product     Up
order_service       java -jar app.jar        order       Up
prometheus          /bin/prometheus          prometheus  Up
grafana             /run.sh                  grafana     Up
```

### 3.4 Tests health check

```bash
# Health checks (endpoints publics)
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

**Attendu:** Réponse 200 OK avec `{"status":"UP"}`

### 3.5 Tests de sécurité

#### Test 1: Accès sans token (doit être rejeté 401)

```bash
curl -X GET http://localhost:8082/api/v1/products
```

**Attendu:** `401 Unauthorized`

#### Test 2: Créer un utilisateur et login

```bash
# Créer un user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com","password":"Test123!"}'

# Login (obtenir JWT)
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' | jq -r '.token')

echo "Token: $TOKEN"
```

**Attendu:** 
- User créé avec id
- Token JWT obtenu (commence par `eyJ`)

#### Test 3: Accès avec token valide (doit être accepté 200)

```bash
# Remplacer TOKEN par le token obtenu ci-dessus
curl -X GET http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer $TOKEN"
```

**Attendu:** `200 OK` avec la liste des produits

#### Test 4: Accès avec token invalide (doit être rejeté 401)

```bash
curl -X GET http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer invalid.token.here"
```

**Attendu:** `401 Unauthorized`

#### Test 5: Endpoints publics (sans auth)

```bash
curl -X GET http://localhost:8082/actuator/health
curl -X GET http://localhost:8082/v3/api-docs
```

**Attendu:** `200 OK` (pas besoin de token)

---

## Partie 4: Tests avec Postman

### 4.1 Importer la collection

1. Ouvrir Postman
2. File → Import
3. Sélectionner `postman/platform-secured.json`
4. Cliquer "Import"

**Attendu:** Collection "E-Commerce Platform - Tests Sécurisation JWT" importée

### 4.2 Configurer les variables (optionnel)

Les variables doivent déjà être configurées:
- `membershipBaseUrl`: http://localhost:8081
- `productBaseUrl`: http://localhost:8082
- `orderBaseUrl`: http://localhost:8083

### 4.3 Exécuter les tests

1. **Scénario de base:**
   - 3.1 Créer un utilisateur → 201 Created
   - 3.2 Login → 200 OK + JWT token

2. **Tests d'accès autorisé:**
   - 4.1 GET Products (avec token) → 200 OK
   - 4.2 POST Order (avec token) → 201 Created

3. **Tests de sécurité:**
   - 5.1 GET Products (sans token) → 401 Unauthorized
   - 5.2 POST Order (sans token) → 401 Unauthorized
   - 6.1 GET Products (token invalide) → 401 Unauthorized
   - 6.2 GET Products (header malformé) → 401 Unauthorized

4. **Tests endpoints publics:**
   - 7.1 GET /actuator/health → 200 OK
   - 7.2 GET /v3/api-docs → 200 OK

5. **Scénario complet:**
   - 8. Scénario Complet Intégré (exécute 8.1 → 8.2 → 8.3 → 8.4)

**Attendu:** Tous les tests doivent passer (status 200, 201, ou 401 selon le cas)

---

## Partie 5: Vérifications visuelles

### 5.1 Prometheus

```bash
# Accéder à Prometheus
open http://localhost:9090
# ou
curl http://localhost:9090/api/v1/targets
```

**Attendu:** 
- Services répertoriés comme "UP"
- Métriques disponibles (chercher "jvm_memory")

### 5.2 Grafana

```bash
# Accéder à Grafana
open http://localhost:3000
# Login: admin / admin123
```

**Attendu:**
- Connexion réussie
- Dashboard "ecommerce-overview" visible
- Métriques affichées

### 5.3 Docker Hub (optionnel)

```bash
# Publier les images
export DOCKER_HUB_USERNAME="votre-username"
docker login
bash docker/publish-all.sh
```

**Attendu:** Images publiées sur Docker Hub dans le compte personnel

---

## Checklist finale

### Code source
- [ ] `ms-order/src/main/java/com/ecommerce/order/security/JwtTokenValidator.java` créé
- [ ] `ms-order/src/main/java/com/ecommerce/order/security/JwtAuthentificationFilter.java` créé
- [ ] `ms-order/src/main/java/com/ecommerce/order/security/SecurityConfig.java` créé
- [ ] `ms-order/src/main/java/com/ecommerce/order/settings/InfraSetting.java` créé
- [ ] `ms-order/src/main/java/com/ecommerce/order/domain/entity/User.java` créé
- [ ] `ms-order/pom.xml` contient les dépendances JWT et Security
- [ ] `ms-order/src/main/resources/application.yml` configuré complètement

### Docker
- [ ] `docker/build-all.sh` créé et exécutable
- [ ] `docker/publish-all.sh` créé et exécutable
- [ ] `docker/deploy.sh` créé et exécutable
- [ ] `docker-compose.yml` configuré pour ms-order
- [ ] Clés RSA générées dans `keys/server.p12`

### Documentation
- [ ] `SECURITY.md` créé et complét
- [ ] `DOCKER.md` créé et complet
- [ ] `IMPLEMENTATION.md` créé et complet

### Tests Postman
- [ ] `postman/platform-secured.json` créé
- [ ] Contient les 8 groupes de tests
- [ ] Tous les tests peuvent s'exécuter

### Tests fonctionnels
- [ ] Les 3 services démarrent sans erreur
- [ ] Health checks répondent (8 endpoints)
- [ ] Login retourne un JWT valide
- [ ] Accès autorisé avec token (200)
- [ ] Accès rejeté sans token (401)
- [ ] Accès rejeté avec token invalide (401)
- [ ] Endpoints publics accessibles sans auth

---

## Commandes de nettoyage

```bash
# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Supprimer les images
docker rmi ecommerce-membership:1.0 ecommerce-product:1.0 ecommerce-order:1.0

# Supprimer les conteneurs stoppés
docker container prune -f

# Supprimer les images non utilisées
docker image prune -f

# Afficher tout
docker system df
```

---

## Fichiers d'artefact pour la livraison

```
ecommerce-platform/
├── docker/
│   ├── build-all.sh
│   ├── publish-all.sh
│   └── deploy.sh
├── keys/
│   └── server.p12
├── ms-membership/ (existant + auth controller)
├── ms-product/ (existant + security configuré)
├── ms-order/ (NOUVEAU - sécurité ajoutée)
├── postman/
│   ├── platform-tests.json (ancien)
│   └── platform-secured.json (NOUVEAU)
├── docker-compose.yml (existant)
├── prometheus.yml (existant)
├── SECURITY.md (NOUVEAU)
├── DOCKER.md (NOUVEAU)
├── IMPLEMENTATION.md (NOUVEAU)
├── VERIFICATION.md (ce fichier)
└── setup.sh (optionnel)
```

---

## Contacts et support

**Questions sur la sécurité JWT?**
→ Consulter `SECURITY.md` (section Dépannage)

**Questions sur Docker?**
→ Consulter `DOCKER.md` (section Dépannage)

**Questions sur l'implémentation?**
→ Consulter `IMPLEMENTATION.md`

---

**Status:** ✅ Implémentation complète
**Date:** Janvier 2026
**Durée estimée de vérification:** 30-45 minutes
