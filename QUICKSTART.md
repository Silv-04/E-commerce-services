# 🚀 GUIDE DE DÉMARRAGE RAPIDE

## Bienvenue! 👋

Ce guide vous permettra de lancer la plateforme e-commerce sécurisée en 15 minutes.

---

## 📋 Prérequis

Vérifiez que vous avez installé:

```bash
# Docker & Docker Compose
docker --version
docker-compose --version

# OpenSSL
openssl version

# Optionnel: Git
git --version
```

**Windows:** Installer [Docker Desktop](https://www.docker.com/products/docker-desktop)
**Mac:** Installer [Docker Desktop](https://www.docker.com/products/docker-desktop) ou `brew install docker`
**Linux:** `sudo apt-get install docker.io docker-compose openssl`

---

## ⚡ Démarrage en 5 étapes

### Étape 1: Générer les clés RSA (2 min)

```bash
# Depuis le répertoire racine du projet
mkdir -p keys

# Créer la clé privée RSA 2048 bits
openssl genrsa -out keys/private.key 2048

# Créer le certificat auto-signé
openssl req -new -x509 -key keys/private.key -out keys/certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"

# Convertir en PKCS12 (format utilisé par les services)
openssl pkcs12 -export -in keys/certificate.crt -inkey keys/private.key \
  -out keys/server.p12 -name jil -password pass:jil

# Vérifier (optionnel)
ls -la keys/server.p12
echo "✓ Clés RSA créées"
```

**Résultat attendu:** Fichier `keys/server.p12` (~4 KB)

---

### Étape 2: Compiler les services (3 min)

```bash
# Compiler les 3 microservices
cd ms-membership && mvn clean package -DskipTests && cd ..
cd ms-product && mvn clean package -DskipTests && cd ..
cd ms-order && mvn clean package -DskipTests && cd ..

echo "✓ Services compilés"
```

**Alternative avec script:**
```bash
bash docker/build-all.sh
```

---

### Étape 3: Builder les images Docker (4 min)

```bash
# Option A: Script automatisé (recommandé)
bash docker/build-all.sh

# Option B: Manuel
docker build -t ecommerce-membership:1.0 ms-membership/
docker build -t ecommerce-product:1.0 ms-product/
docker build -t ecommerce-order:1.0 ms-order/

# Vérifier
docker images | grep ecommerce
echo "✓ Images Docker créées"
```

---

### Étape 4: Lancer avec Docker Compose (2 min)

```bash
# Démarrer tous les services
docker-compose up -d

# Attendre quelques secondes
sleep 5

# Vérifier que tout est lancé
docker-compose ps
```

**Résultat attendu:**

```
NAME                COMMAND                  SERVICE      STATUS
membership_service  java -jar app.jar        membership   Up 5s
product_service     java -jar app.jar        product      Up 5s
order_service       java -jar app.jar        order        Up 5s
prometheus          /bin/prometheus ...      prometheus   Up 5s
grafana             /run.sh                  grafana      Up 5s
```

---

### Étape 5: Tester (4 min)

#### Test 1: Health checks (public, sans auth)

```bash
# Vérifier que les services répondent
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health

# Résultat attendu: {"status":"UP"}
echo "✓ Health checks OK"
```

#### Test 2: Créer un utilisateur

```bash
# Créer un nouvel utilisateur
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "Test123!"
  }'

# Résultat attendu: {"id": 1, "email": "john@example.com", ...}
echo "✓ Utilisateur créé"
```

#### Test 3: Se connecter (obtenir JWT)

```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Test123!"
  }'

# Résultat attendu: {"token": "eyJhbGc...", "expiresIn": 3600}
echo "✓ Token JWT obtenu"
```

#### Test 4: Accès avec token

```bash
# D'abord, stocker le token
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Test123!"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"

# Ensuite, utiliser le token pour accéder aux services
curl -X GET http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer $TOKEN"

# Résultat attendu: Liste de produits en JSON
echo "✓ Accès avec token autorisé"
```

#### Test 5: Rejet sans token

```bash
# Essayer sans le token
curl -X GET http://localhost:8082/api/v1/products

# Résultat attendu: 401 Unauthorized (avec corps vide)
echo "✓ Accès sans token rejeté"
```

---

## 🧪 Tests complets avec Postman

### Option 1: Collection Postman

1. Ouvrir **Postman**
2. **File → Import**
3. Sélectionner: `postman/platform-secured.json`
4. Cliquer **Import**

**Exécuter les tests:**
- Section **3.2 Login** → Copier le token dans les tests suivants
- Section **4** → Tests d'accès autorisé (200 OK)
- Section **5** → Tests sans token (401)
- Section **8** → Scénario complet intégré

### Option 2: Manuellement

Voir **Test 1-5** ci-dessus

---

## 🌐 Accéder aux services

| Service | URL |
|---------|-----|
| **Membership API** | http://localhost:8081 |
| **Product API** | http://localhost:8082 |
| **Order API** | http://localhost:8083 |
| **Swagger/OpenAPI** | http://localhost:8082/swagger-ui.html |
| **Health** | http://localhost:8082/actuator/health |
| **Metrics** | http://localhost:8082/actuator/metrics |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3000 |

**Grafana login:** admin / admin123

---

## 🛑 Arrêter les services

```bash
# Arrêter (garder les données)
docker-compose down

# Arrêter et supprimer les données
docker-compose down -v

# Voir les logs avant d'arrêter
docker-compose logs -f
```

---

## 🔧 Commandes utiles

### Logs et débogage

```bash
# Voir tous les logs
docker-compose logs -f

# Voir les logs d'un seul service
docker-compose logs -f order
docker-compose logs -f product

# Dernières 50 lignes
docker-compose logs --tail=50 order

# Avec timestamps
docker-compose logs -f --timestamps order
```

### Vérifications

```bash
# État des services
docker-compose ps

# Utilisation des ressources
docker stats

# Réseau Docker
docker network ls

# Volumes
docker volume ls
```

### Redémarrer un service

```bash
# Redémarrer tous les services
docker-compose restart

# Redémarrer un service spécifique
docker-compose restart order
```

---

## ⚠️ Problèmes courants

### "Docker daemon not running"

```bash
# Redémarrer Docker
sudo systemctl restart docker
# Ou redémarrer Docker Desktop (Windows/Mac)
```

### "Port already in use"

```bash
# Identifier le processus
lsof -i :8082  # macOS/Linux
netstat -ano | findstr :8082  # Windows

# Libérer le port
kill -9 <PID>

# Ou utiliser un autre port dans docker-compose.yml
# ports:
#   - "18082:8082"  (nouveau port)
```

### "Connection refused"

```bash
# Vérifier que Docker Compose est lancé
docker-compose ps

# Sinon lancer
docker-compose up -d

# Attendre quelques secondes
sleep 5
```

### "401 Unauthorized"

- Vérifier que le token est inclus: `Authorization: Bearer <token>`
- Vérifier que le token n'est pas expiré
- Vérifier que le format du header est correct

### "Token cannot be verified"

- Vérifier que `keys/server.p12` existe
- Vérifier que les clés RSA n'ont pas été régénérées (sinon relancer les services)
- Vérifier que le fichier est dans le bon format PKCS12

---

## 📚 Documentation détaillée

Pour plus d'informations:

- **Sécurité JWT/RSA:** Consulter [SECURITY.md](SECURITY.md)
- **Docker et déploiement:** Consulter [DOCKER.md](DOCKER.md)
- **Implémentation complète:** Consulter [IMPLEMENTATION.md](IMPLEMENTATION.md)
- **Vérification et tests:** Consulter [VERIFICATION.md](VERIFICATION.md)
- **Résumé:** Consulter [RESUME.md](RESUME.md)

---

## ✅ Checklist rapide

- [ ] Clés RSA générées (`keys/server.p12`)
- [ ] Services compilés (Maven)
- [ ] Images Docker créées
- [ ] Services lancés (`docker-compose up -d`)
- [ ] Health checks OK (curl /actuator/health)
- [ ] User créé via POST /api/v1/users
- [ ] Login fonctionnant (JWT obtenu)
- [ ] Accès avec token OK (200 OK)
- [ ] Accès sans token rejeté (401)
- [ ] Collection Postman importée (optionnel)

---

## 🎓 Explications rapides

### JWT (JSON Web Token)

Token contenant: `header.payload.signature`

- **Header:** Type de token + algorithme (RS256)
- **Payload:** Données (userId, email, roles, expiration)
- **Signature:** Signée avec la clé privée RSA

### RSA asymétrique

- **Clé privée:** Signe les tokens (gardée secrète)
- **Clé publique:** Valide les tokens (partagée)
- **Avantage:** Plus sûr (clé privée jamais exposée)

### Docker Compose

Orchestre 5 conteneurs:
- 3 microservices (ports 8081, 8082, 8083)
- 1 Prometheus (port 9090)
- 1 Grafana (port 3000)

Tous sur un réseau interne pour communication.

---

## 🆘 Support

### Erreurs classiques

| Erreur | Solution |
|--------|----------|
| Port already in use | `docker-compose down` ou changer le port |
| Connection refused | Lancer `docker-compose up -d` |
| 401 Unauthorized | Vérifier le header `Authorization: Bearer <token>` |
| Token cannot be verified | Régénérer les clés et redémarrer |

### Ressources

- [JWT.io](https://jwt.io) - Debugger JWT
- [Docker Docs](https://docs.docker.com) - Documentation Docker
- [Spring Security Docs](https://spring.io/projects/spring-security) - Security configs

---

## 🎯 Prochaines étapes

### Maintenant que tout fonctionne:

1. **Importer la collection Postman** pour tester automatiquement
2. **Consulter SECURITY.md** pour comprendre l'architecture
3. **Consulter DOCKER.md** pour le déploiement production
4. **Modifier les services** si besoin (ajouter des endpoints)
5. **Publier sur Docker Hub** pour partage/production

---

## 💡 Conseils

✅ **Gardez les logs à côté** - `docker-compose logs -f`
✅ **Utilisez Postman** pour tester rapidement
✅ **Consultez la documentation** en cas de problème
✅ **Redémarrez tout** si changements: `docker-compose restart`
✅ **Sauvegardez les clés RSA** - Important pour la production

---

**Vous êtes prêt! 🚀**

**Commande finale:**
```bash
docker-compose up -d && docker-compose ps
```

**Bonne chance! 🎯**
