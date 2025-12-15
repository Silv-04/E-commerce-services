# 📊 Guide de Monitoring - Plateforme E-Commerce

## Vue d'ensemble

Ce guide explique comment utiliser le monitoring de la plateforme E-Commerce avec **Prometheus** et **Grafana**.

## Architecture du Monitoring

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  MS-Membership  │     │   MS-Product    │     │    MS-Order     │
│     :8081       │     │     :8082       │     │     :8083       │
│  /actuator/     │     │  /actuator/     │     │  /actuator/     │
│   prometheus    │     │   prometheus    │     │   prometheus    │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                        ┌────────▼────────┐
                        │   Prometheus    │
                        │     :9090       │
                        │  (Scraping)     │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │    Grafana      │
                        │     :3000       │
                        │ (Dashboards)    │
                        └─────────────────┘
```

## Prérequis

- **Docker** et **Docker Compose** installés
- Les 3 microservices démarrés sur les ports 8081, 8082, 8083

## Démarrage Rapide

### 1. Démarrer les microservices

```bash
# Terminal 1 - MS-Membership
cd ms-membership
mvn spring-boot:run

# Terminal 2 - MS-Product  
cd ms-product
mvn spring-boot:run

# Terminal 3 - MS-Order
cd ms-order
mvn spring-boot:run
```

### 2. Démarrer le monitoring

```bash
# À la racine du projet
docker-compose up -d
```

### 3. Accéder aux interfaces

| Service | URL | Identifiants |
|---------|-----|--------------|
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin123 |

## Utilisation de Prometheus

### Accès à l'interface

Ouvrez http://localhost:9090 dans votre navigateur.

### Vérifier les targets

1. Allez dans **Status** → **Targets**
2. Vous devriez voir 3 targets avec le statut **UP** :
   - `ms-membership` (host.docker.internal:8081)
   - `ms-product` (host.docker.internal:8082)
   - `ms-order` (host.docker.internal:8083)

### Requêtes utiles

```promql
# Vérifier que les services sont UP
up

# Nombre de requêtes HTTP par service
http_server_requests_seconds_count

# Temps de réponse moyen
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Mémoire JVM utilisée
jvm_memory_used_bytes{area="heap"}

# Connexions DB actives
hikaricp_connections_active
```

## Utilisation de Grafana

### Première connexion

1. Ouvrez http://localhost:3000
2. Connectez-vous avec **admin** / **admin123**
3. (Optionnel) Changez le mot de passe

### Dashboards disponibles

Deux dashboards sont préconfigurés :

#### 1. E-Commerce Platform - Overview

Ce dashboard affiche :
- ✅ Statut des 3 microservices (UP/DOWN)
- 📊 Requêtes HTTP par seconde
- ⏱️ Temps de réponse moyen
- 💾 Utilisation mémoire JVM
- 🔗 Connexions base de données actives

#### 2. E-Commerce Platform - Business KPIs

Ce dashboard affiche :
- 👥 Nombre total d'utilisateurs
- 📦 Nombre total de produits
- 🛒 Nombre total de commandes
- ⚠️ Produits en stock bas
- 📊 Répartition des commandes par statut
- 📦 Répartition des produits par catégorie
- 💰 Montant des commandes du jour

### Accéder aux dashboards

1. Dans le menu latéral, cliquez sur **Dashboards**
2. Naviguez vers le dossier **E-Commerce**
3. Sélectionnez le dashboard souhaité

## Métriques Personnalisées

### MS-Membership (8081)

| Métrique | Description |
|----------|-------------|
| `users_total` | Nombre total d'utilisateurs |
| `users_active` | Nombre d'utilisateurs actifs |

### MS-Product (8082)

| Métrique | Description |
|----------|-------------|
| `products_total` | Nombre total de produits |
| `products_by_category` | Produits par catégorie |
| `products_low_stock` | Produits avec stock < 5 |
| `products_created_total` | Compteur de produits créés |

### MS-Order (8083)

| Métrique | Description |
|----------|-------------|
| `orders_total` | Nombre total de commandes |
| `orders_by_status` | Commandes par statut |
| `orders_today_total_amount` | Montant total des commandes du jour |
| `orders_created_total` | Compteur de commandes créées |

## Arrêt du Monitoring

```bash
# Arrêter les containers
docker-compose down

# Arrêter et supprimer les volumes (données)
docker-compose down -v
```

## Troubleshooting

### Les targets sont DOWN dans Prometheus

**Problème** : Les microservices ne sont pas accessibles depuis Docker.

**Solutions** :
1. Vérifiez que les microservices sont bien démarrés
2. Vérifiez que les endpoints `/actuator/prometheus` sont accessibles :
   ```bash
   curl http://localhost:8081/actuator/prometheus
   curl http://localhost:8082/actuator/prometheus
   curl http://localhost:8083/actuator/prometheus
   ```
3. Sur Windows/Mac, `host.docker.internal` devrait fonctionner automatiquement

### Grafana ne montre pas de données

**Problème** : Les graphiques sont vides.

**Solutions** :
1. Vérifiez que Prometheus collecte les données (Status → Targets)
2. Vérifiez la datasource dans Grafana (Configuration → Data Sources)
3. Élargissez la plage de temps dans le dashboard

### Erreur de connexion à Prometheus depuis Grafana

**Problème** : Datasource non fonctionnelle.

**Solution** : L'URL doit être `http://prometheus:9090` (nom du container Docker).

## Configuration Avancée

### Modifier l'intervalle de scraping

Éditez `prometheus.yml` :
```yaml
global:
  scrape_interval: 10s  # Défaut: 15s
```

### Ajouter des alertes

Créez un fichier `alerts.yml` et référencez-le dans `prometheus.yml` :
```yaml
rule_files:
  - "alerts.yml"
```

### Persister les données

Les données sont déjà persistées via les volumes Docker :
- `prometheus_data` pour Prometheus
- `grafana_data` pour Grafana

## Ressources

- [Documentation Prometheus](https://prometheus.io/docs/)
- [Documentation Grafana](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)
