# DOCKER.md - Dockerisation et Déploiement

## Vue d'ensemble

Guide complet pour containeriser la plateforme e-commerce et la déployer avec Docker et Docker Compose.

---

## Prérequis

- **Docker** >= 24.0
- **Docker Compose** >= 2.20
- **Maven** >= 3.9.0 (pour build local)
- **Java** >= 21 (pour build local)

### Installation

**Windows (avec Docker Desktop):**
```bash
# Télécharger Docker Desktop depuis https://www.docker.com/products/docker-desktop
# L'installation inclut Docker CLI et Docker Compose
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install docker.io docker-compose

# Vérifier l'installation
docker --version
docker-compose --version
```

---

## Architecture Docker

### Images Docker

Chaque microservice a sa propre image basée sur **Eclipse Temurin 21 JRE**:

| Service | Image | Port | Dockerfile |
|---------|-------|------|-----------|
| Membership | `username/ecommerce-membership:1.0` | 8081 | [Dockerfile](ms-membership/Dockerfile) |
| Product | `username/ecommerce-product:1.0` | 8082 | [Dockerfile](ms-product/Dockerfile) |
| Order | `username/ecommerce-order:1.0` | 8083 | [Dockerfile](ms-order/Dockerfile) |
| Prometheus | `prom/prometheus:v2.47.0` | 9090 | (officielle) |
| Grafana | `grafana/grafana:10.1.0` | 3000 | (officielle) |

### Dockerfile multi-stage

Chaque service utilise une build **multi-stage** pour optimiser la taille:

```dockerfile
# Stage 1: Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (image finale légère)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ms-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Avantages:**
- Image finale petite (~150 MB au lieu de ~600 MB)
- Maven et sources sont supprimés dans l'image finale
- Sécurité accrue (moins de vulnérabilités potentielles)

---

## Docker Compose

### Structure

```yaml
version: '3.8'

services:
  # Services métier
  membership:
    build:
      context: ./ms-membership
      dockerfile: Dockerfile
    ports:
      - "8081:8081"
    volumes:
      - ./keys:/app/keys          # Clés RSA partagées
    networks:
      - monitoring

  product:
    build:
      context: ./ms-product
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    volumes:
      - ./keys:/app/keys
    networks:
      - monitoring

  order:
    build:
      context: ./ms-order
      dockerfile: Dockerfile
    ports:
      - "8083:8083"
    volumes:
      - ./keys:/app/keys
    environment:
      USER_SERVICE_URL: "http://membership:8081"
      PRODUCT_SERVICE_URL: "http://product:8082"
    networks:
      - monitoring
    depends_on:
      - membership
      - product

  # Observabilité
  prometheus:
    image: prom/prometheus:v2.47.0
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    networks:
      - monitoring

  grafana:
    image: grafana/grafana:10.1.0
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/provisioning:/etc/grafana/provisioning:ro
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin123
    networks:
      - monitoring
    depends_on:
      - prometheus

volumes:
  prometheus_data:
  grafana_data:

networks:
  monitoring:
    driver: bridge
```

### Configuration réseau

- **Driver:** Bridge
- **DNS interne:** Services accessibles par leur nom (ex: `http://membership:8081`)
- **Isolation:** Chaque conteneur peut communiquer avec les autres

---

## Commandes Docker essentielles

### Génération des clés RSA

```bash
# 1. Créer la clé privée (2048 bits)
openssl genrsa -out private.key 2048

# 2. Créer le certificat auto-signé (365 jours)
openssl req -new -x509 -key private.key -out certificate.crt -days 365 \
  -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce"

# 3. Créer le répertoire des clés
mkdir -p keys

# 4. Convertir en PKCS12
openssl pkcs12 -export -in certificate.crt -inkey private.key \
  -out keys/server.p12 -name jil -password pass:jil

# 5. Vérifier
ls -la keys/server.p12
```

**Résultat:** Fichier `keys/server.p12` (clé privée + certificat)

### Build des images

#### Build local (depuis le répertoire racine)

```bash
# Option 1: Script automatisé
bash docker/build-all.sh

# Option 2: Manuel - Membership
cd ms-membership
mvn clean package -DskipTests
docker build -t myusername/ecommerce-membership:1.0 .

# Option 2: Manuel - Product
cd ms-product
mvn clean package -DskipTests
docker build -t myusername/ecommerce-product:1.0 .

# Option 2: Manuel - Order
cd ms-order
mvn clean package -DskipTests
docker build -t myusername/ecommerce-order:1.0 .
```

#### Vérifier les images
```bash
docker images | grep ecommerce
```

**Sortie attendue:**
```
REPOSITORY                        TAG    IMAGE ID    SIZE
myusername/ecommerce-membership   1.0    abc123...   145MB
myusername/ecommerce-product      1.0    def456...   148MB
myusername/ecommerce-order        1.0    ghi789...   150MB
```

### Publication sur Docker Hub

#### 1. Créer un compte Docker Hub

- Se rendre sur https://hub.docker.com
- S'inscrire (gratuit)
- Créer 3 repositorys **privés**:
  - `ecommerce-membership`
  - `ecommerce-product`
  - `ecommerce-order`

#### 2. Login Docker CLI

```bash
docker login
# Username: votre-username
# Password: votre-token-docker-hub
```

#### 3. Tag et Push (automatisé)

```bash
# Configuration
export DOCKER_HUB_USERNAME="your-username"

# Script automatisé
bash docker/publish-all.sh

# Ou manuel
docker tag myusername/ecommerce-membership:1.0 myusername/ecommerce-membership:latest
docker push myusername/ecommerce-membership:1.0
docker push myusername/ecommerce-membership:latest

docker tag myusername/ecommerce-product:1.0 myusername/ecommerce-product:latest
docker push myusername/ecommerce-product:1.0
docker push myusername/ecommerce-product:latest

docker tag myusername/ecommerce-order:1.0 myusername/ecommerce-order:latest
docker push myusername/ecommerce-order:1.0
docker push myusername/ecommerce-order:latest
```

#### 4. Vérifier sur Docker Hub

Visitez https://hub.docker.com/u/your-username et vérifiez que les images sont présentes.

---

## Déploiement avec Docker Compose

### Démarrage de la plateforme complète

```bash
# Depuis le répertoire racine

# Option 1: Script automatisé
bash docker/deploy.sh

# Option 2: Manuel
docker-compose up -d

# Vérifier le statut
docker-compose ps
```

**Sortie attendue:**
```
NAME                COMMAND                  SERVICE      STATUS      PORTS
membership_service  "java -jar app.jar"      membership   Up 2s       0.0.0.0:8081->8081/tcp
product_service     "java -jar app.jar"      product      Up 2s       0.0.0.0:8082->8082/tcp
order_service       "java -jar app.jar"      order        Up 2s       0.0.0.0:8083->8083/tcp
prometheus          "/bin/prometheus ..."    prometheus   Up 2s       0.0.0.0:9090->9090/tcp
grafana             "/run.sh"                grafana      Up 2s       0.0.0.0:3000->3000/tcp
```

### Consultation des logs

```bash
# Tous les services
docker-compose logs -f

# Un service spécifique
docker-compose logs -f membership
docker-compose logs -f product
docker-compose logs -f order

# Dernières 100 lignes
docker-compose logs --tail=100 order
```

### Accès aux services

| Service | URL |
|---------|-----|
| **Membership API** | http://localhost:8081 |
| **Product API** | http://localhost:8082 |
| **Order API** | http://localhost:8083 |
| **OpenAPI Swagger** | http://localhost:8082/swagger-ui.html |
| **Health Check** | http://localhost:8082/actuator/health |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3000 (admin/admin123) |

### Arrêt des services

```bash
# Arrêter les services (garder les volumes)
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Redémarrer rapidement
docker-compose restart
```

---

## Déploiement depuis Docker Hub

### Récupérer la plateforme depuis Docker Hub

```bash
# Configuration
export DOCKER_HUB_USERNAME="your-username"

# Option 1: Script automatisé
bash docker/deploy.sh

# Option 2: Manuel
docker pull $DOCKER_HUB_USERNAME/ecommerce-membership:latest
docker pull $DOCKER_HUB_USERNAME/ecommerce-product:latest
docker pull $DOCKER_HUB_USERNAME/ecommerce-order:latest

# Démarrer avec docker-compose
docker-compose up -d
```

### Recréer la plateforme de zéro

```bash
# 1. Cloner le repository
git clone https://github.com/your-org/ecommerce-platform.git
cd ecommerce-platform

# 2. Générer les clés RSA
bash docker/generate-keys.sh
# ou manuellement (voir section "Génération des clés RSA")

# 3. Configurer Docker Hub (optionnel)
export DOCKER_HUB_USERNAME="your-username"

# 4. Démarrer la plateforme
docker-compose up -d

# 5. Vérifier
docker-compose ps
curl http://localhost:8082/actuator/health
```

---

## Gestion des données

### Volumes persistants

```yaml
volumes:
  prometheus_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /var/lib/docker/volumes/prometheus_data

  grafana_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /var/lib/docker/volumes/grafana_data
```

### Données H2 (en mémoire)

Les services Membership, Product et Order utilisent une base de données **H2 en mémoire** (`jdbc:h2:mem:*`).

- **Avantage:** Pas besoin de conteneur MySQL/PostgreSQL
- **Inconvénient:** Données perdues au redémarrage

**Pour la production:** Utiliser une base de données externe:

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_PASSWORD: password
      POSTGRES_DB: ecommerce
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## Configuration Docker Hub (Repositorys privés)

### Créer un repository privé

1. Accédez à https://hub.docker.com
2. Cliquez sur **"Repositories"**
3. Cliquez sur **"Create Repository"**
4. Remplissez:
   - **Name:** `ecommerce-membership` (ou `-product`, `-order`)
   - **Visibility:** **Private** ✓
   - **Description:** "Microservice de gestion des utilisateurs"
5. Cliquez **"Create"**

### Partager l'accès avec l'enseignant

1. Allez dans **Settings** du repository
2. Onglet **Collaborators**
3. Cliquez **"Add Collaborator"**
4. Entrez le username de l'enseignant
5. Sélectionnez le rôle: **Read-only**
6. Cliquez **"Add"**

---

## Dépannage

### Erreur: "Cannot connect to Docker daemon"

```bash
# Vérifier que Docker est lancé
docker ps

# Redémarrer le service Docker
sudo systemctl restart docker

# (Windows) Redémarrer Docker Desktop
```

### Erreur: "port 8081 is already allocated"

```bash
# Identifier le processus utilisant le port
lsof -i :8081  # (macOS/Linux)
netstat -ano | findstr :8081  # (Windows)

# Libérer le port ou utiliser un autre
# Modifier le port dans docker-compose.yml
# ports:
#   - "18081:8081"  (nouveau port externe)
```

### Les services ne communiquent pas

```bash
# Vérifier le réseau Docker Compose
docker-compose ps

# Vérifier la connectivité inter-services
docker-compose exec order ping membership
docker-compose exec order ping product

# Vérifier les logs
docker-compose logs order | grep -i "connection\|timeout"
```

### Image trop grande (>500 MB)

```bash
# Utiliser une image JRE au lieu de JDK
FROM eclipse-temurin:21-jre-alpine  # ~145 MB (bon)
# Au lieu de:
FROM eclipse-temurin:21-jdk-alpine  # ~370 MB (mauvais)

# Supprimer les dépendances test du JAR
mvn clean package -DskipTests

# Vérifier la taille
docker image ls | grep ecommerce
```

---

## Optimisations recommandées

### 1. Caching des couches Docker

```dockerfile
# Placer les dépendances avant le code
COPY pom.xml .
RUN mvn dependency:resolve  # Cache les dépendances

COPY src ./src
RUN mvn compile             # Cache le code compilé
```

### 2. Health Checks

```yaml
services:
  membership:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s
```

### 3. Limites de ressources

```yaml
services:
  membership:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

### 4. Auto-restart

```yaml
services:
  membership:
    restart: unless-stopped  # Redémarrer si crash (sauf arrêt manuel)
```

---

## Fichiers et dossiers

```
ecommerce-platform/
├── docker/
│   ├── build-all.sh              # Compiler et build images
│   ├── publish-all.sh            # Push vers Docker Hub
│   └── deploy.sh                 # Pull et démarrer
├── docker-compose.yml            # Orchestration des services
├── keys/
│   └── server.p12                # Clés RSA (généré)
├── ms-membership/
│   ├── Dockerfile                # Build Membership
│   └── ...
├── ms-product/
│   ├── Dockerfile                # Build Product
│   └── ...
├── ms-order/
│   ├── Dockerfile                # Build Order
│   └── ...
├── prometheus.yml                # Configuration Prometheus
├── monitoring/
│   └── provisioning/             # Config Grafana
└── DOCKER.md                     # Ce fichier
```

---

## Références

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Specification](https://docs.docker.com/compose/compose-file/)
- [Docker Hub](https://hub.docker.com)
- [OpenSSL Documentation](https://www.openssl.org/docs/)
- [Best Practices for Java Container Images](https://www.docker.com/blog/best-practices-for-java-container-images/)
