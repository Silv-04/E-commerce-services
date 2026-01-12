# 🛍️ E-Commerce Platform - Sécurisation JWT & Dockerisation

[![Status](https://img.shields.io/badge/Status-✅%20Complete-brightgreen)](LIVRAISON.md)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://docs.docker.com/compose/)

Une plateforme e-commerce **sécurisée** avec authentification JWT/RSA et **dockerisée** pour un déploiement production-ready.

## 🚀 Démarrage rapide

```bash
# 1. Générer les clés RSA
mkdir -p keys
openssl genrsa -out keys/private.key 2048
openssl req -new -x509 -key keys/private.key -out keys/certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"
openssl pkcs12 -export -in keys/certificate.crt -inkey keys/private.key \
  -out keys/server.p12 -name jil -password pass:jil

# 2. Lancer les services
docker-compose up -d

# 3. Vérifier
docker-compose ps
curl http://localhost:8082/actuator/health
```

👉 **[Guide complet →](QUICKSTART.md)**

---

## 📚 Documentation

| Document | Contenu | Durée |
|----------|---------|-------|
| **[QUICKSTART.md](QUICKSTART.md)** | 🚀 Démarrage en 15 min | 15 min |
| **[SECURITY.md](SECURITY.md)** | 🔒 Architecture JWT/RSA | 30 min |
| **[DOCKER.md](DOCKER.md)** | 🐳 Dockerisation & déploiement | 45 min |
| **[INDEX.md](INDEX.md)** | 📖 Navigation documentation | 5 min |
| **[VERIFICATION.md](VERIFICATION.md)** | ✅ Tests & vérification | 45 min |
| **[IMPLEMENTATION.md](IMPLEMENTATION.md)** | 📋 Résumé implémentation | 10 min |
| **[RESUME.md](RESUME.md)** | 📊 Synthèse complète | 20 min |
| **[LIVRAISON.md](LIVRAISON.md)** | 📦 Rapport de livraison | 5 min |

---

## 🎯 Fonctionnalités

### 🔐 Sécurité JWT/RSA

- ✅ **Authentification JWT** - Tokens signés RS256
- ✅ **RSA asymétrique** - Clés 2048 bits (privée/publique)
- ✅ **Spring Security** - Filtrage stateless
- ✅ **Validation tokens** - Signature + expiration + claims
- ✅ **Inter-services** - Propagation JWT sécurisée
- ✅ **Endpoints publics** - /actuator, /v3/api-docs

### 🐳 Dockerisation

- ✅ **Docker Compose** - Orchestration 5 conteneurs
- ✅ **Multi-stage builds** - Images légères (~150 MB)
- ✅ **Java 21 JRE** - Eclipse Temurin optimisé
- ✅ **Réseau bridge** - Communication inter-services
- ✅ **Volumes partagés** - Clés RSA centralisées
- ✅ **Docker Hub** - Déploiement image privée

### 📊 Monitoring

- ✅ **Prometheus** - Scrape des métriques
- ✅ **Grafana** - Dashboards en temps réel
- ✅ **Health checks** - Liveness/readiness probes
- ✅ **Metrics** - JVM, HTTP, Tomcat
- ✅ **Logs** - DEBUG pour sécurité

### 🧪 Tests

- ✅ **Postman** - 23 tests automatisés
- ✅ **Health checks** - Endpoints publics
- ✅ **Authentification** - Login JWT
- ✅ **Autorisation** - Accès avec/sans token
- ✅ **Sécurité** - Token invalide, absent
- ✅ **Workflow complet** - Scénarios intégrés

---

## 📦 Architecture

```
Client
  │
  ├─ POST /auth/login (email, password)
  │   └─ Membership Service
  │       └─ JWT (RS256 signé) ↓
  │
  └─ GET /products + Authorization: Bearer JWT
      ├─ Product Service
      │   └─ Valide JWT avec RSA public key
      │       └─ 200 OK ou 401
      │
      └─ Order Service
          └─ Valide JWT avec RSA public key
              └─ 200 OK ou 401
```

### Services

| Service | Port | Rôle |
|---------|------|------|
| **Membership** | 8081 | Authentification & JWT |
| **Product** | 8082 | Gestion produits (sécurisé) |
| **Order** | 8083 | Gestion commandes (sécurisé) ✨ |
| **Prometheus** | 9090 | Collecte métriques |
| **Grafana** | 3000 | Visualisation dashboards |

---

## 🔐 Sécurité

### JWT Format
```json
{
  "header": {"alg": "RS256", "typ": "JWT"},
  "payload": {
    "sub": "username",
    "iss": "episen-e-commerce",
    "aud": ["web"],
    "UserId": 1,
    "Email": "user@example.com",
    "Roles": ["USER"],
    "exp": 1704100000
  },
  "signature": "signed_with_private_RSA_key"
}
```

### Garanties

- 🔒 **Intégrité** - Signature RSA (non falsifiable)
- 🔐 **Confidentialité** - Clé privée jamais partagée
- ⏱️ **Expiration** - 1 heure par défaut
- 🔄 **Stateless** - Pas de session serveur
- ✅ **Validation** - Signature + Claims + Expiration

---

## 🐳 Containers

```yaml
version: '3.8'

services:
  membership:
    ports: ["8081:8081"]
    volumes:
      - ./keys:/app/keys          # Clés RSA

  product:
    ports: ["8082:8082"]
    volumes:
      - ./keys:/app/keys

  order:
    ports: ["8083:8083"]
    volumes:
      - ./keys:/app/keys
    depends_on:
      - membership
      - product

  prometheus:
    ports: ["9090:9090"]

  grafana:
    ports: ["3000:3000"]
```

---

## 📊 Métriques

| Métrique | Valeur |
|----------|--------|
| **Fichiers créés** | 11 |
| **Lignes de code Java** | ~220 |
| **Lignes de documentation** | ~2650 |
| **Scripts shell** | 3 (305 lignes) |
| **Tests Postman** | 23 |
| **Images Docker** | 5 |
| **Taille images** | 145-343 MB |
| **Démarrage** | ~30s |

---

## 🎓 Concepts

### Sécurité
- **JWT** - JSON Web Tokens
- **RSA** - Asymétrique (2048 bits)
- **RS256** - Signature RSA-SHA256
- **Spring Security** - Framework d'authentification

### DevOps
- **Docker** - Containerisation
- **Docker Compose** - Orchestration
- **Multi-stage builds** - Optimization images
- **Docker Hub** - Registry privé

### Microservices
- **Communication sécurisée** - JWT propagé
- **Authentification centralisée** - Membership service
- **Stateless** - Pas de session
- **Observable** - Prometheus + Grafana

---

## 🚀 Déploiement

### Local

```bash
# Setup complet
bash setup.sh

# Ou pas à pas
docker-compose up -d
```

### Docker Hub privé

```bash
# Configuration
export DOCKER_HUB_USERNAME="your-username"

# Build et publish
bash docker/build-all.sh
bash docker/publish-all.sh

# Déployer depuis Docker Hub
bash docker/deploy.sh
```

---

## ✅ Checklist

- [ ] Clés RSA générées (`keys/server.p12`)
- [ ] Services compilés (`mvn package`)
- [ ] Images Docker créées (`docker build`)
- [ ] Services lancés (`docker-compose up -d`)
- [ ] Health checks OK (`curl /actuator/health`)
- [ ] User créé et login effectué
- [ ] Postman tests importés et exécutés
- [ ] Tous les tests réussis

---

## 🔧 Commandes essentielles

### Docker Compose

```bash
# Démarrer
docker-compose up -d

# Arrêter
docker-compose down

# Logs
docker-compose logs -f [service]

# Redémarrer
docker-compose restart

# Statut
docker-compose ps
```

### Tests

```bash
# Health check
curl http://localhost:8082/actuator/health

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'

# Accès avec token
TOKEN="..."
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/v1/products
```

---

## 📞 Support

### Questions?

- **Démarrage** → [QUICKSTART.md](QUICKSTART.md)
- **Sécurité** → [SECURITY.md](SECURITY.md)
- **Docker** → [DOCKER.md](DOCKER.md)
- **Tests** → [VERIFICATION.md](VERIFICATION.md)
- **Tout** → [INDEX.md](INDEX.md)

### Erreurs?

- **Port déjà utilisé** → Modifier docker-compose.yml
- **Token invalide** → Régénérer les clés
- **401 Unauthorized** → Vérifier le header Authorization
- **Docker ne démarre pas** → Vérifier les logs

---

## 📋 Structure du projet

```
ecommerce-platform/
├── docker/                    # Scripts de déploiement
├── keys/                      # Clés RSA (à générer)
├── ms-membership/             # Service d'authentification
├── ms-product/                # Service produits
├── ms-order/                  # Service commandes ✨
├── postman/                   # Tests Postman
├── monitoring/                # Grafana provisioning
├── docker-compose.yml         # Orchestration
├── QUICKSTART.md              # Démarrage rapide
├── SECURITY.md                # Architecture JWT
├── DOCKER.md                  # Documentation Docker
└── ... (documentation complète)
```

---

## 🎯 Prochaines étapes

1. **Lire** [QUICKSTART.md](QUICKSTART.md) (15 min)
2. **Lancer** les services (`docker-compose up`)
3. **Tester** avec Postman (23 tests)
4. **Consulter** [SECURITY.md](SECURITY.md) pour approfondir
5. **Déployer** sur Docker Hub (optionnel)

---

## 📅 Feuille de route

| Jour | Activité |
|------|----------|
| **1** | Lecture documentation |
| **2** | Lancement et tests locaux |
| **3** | Déploiement Docker Hub |
| **4** | Soutenance et démo |

---

## ✨ Highlights

✅ **Production-ready** - Meilleur pratiques appliquées
✅ **Sécurisée** - RSA asymétrique (non falsifiable)
✅ **Documentée** - >2650 lignes de documentation
✅ **Testée** - 23 scénarios Postman
✅ **Scalable** - Stateless microservices
✅ **Observable** - Prometheus + Grafana
✅ **Reproductible** - Scripts automatisés

---

## 📜 Licence

MIT License - Libre d'utilisation

---

## 🤝 Contribution

Les améliorations sont bienvenues! Consultez les [bonnes pratiques](SECURITY.md#sécurité---best-practices).

---

## 👤 Auteur

Implémentation pour le **TP2 - Sécurisation JWT & Dockerisation**
**Date:** Janvier 2026
**Status:** ✅ Complète et testée

---

## 🎓 Apprentissages

- ✅ JWT et authentification moderne
- ✅ RSA asymétrique pour la sécurité
- ✅ Spring Security stateless
- ✅ Docker et containers
- ✅ Microservices sécurisés
- ✅ Monitoring et observabilité
- ✅ DevOps et déploiement

---

**Documentation rapide:** [INDEX.md](INDEX.md)
**Démarrage immédiat:** [QUICKSTART.md](QUICKSTART.md)
**Rapport final:** [LIVRAISON.md](LIVRAISON.md)

**Bonne chance! 🚀**
