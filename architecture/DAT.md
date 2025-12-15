# Document d'Architecture Technique (DAT) - Plateforme E-Commerce

## 1. Vue d'ensemble

- Plateforme composée de trois microservices : ms-membership (utilisateurs), ms-product (catalogue), ms-order (commandes).
- Exposition REST sécurisée par validation applicative (pas d'auth obligatoire dans le périmètre actuel).
- Observabilité : métriques Prometheus (port 9090) et health checks Spring Boot Actuator.
- Outils de test/recette : collection Postman pour orchestrer les scénarios.

## 2. Schéma d'architecture

- Diagramme à placer dans architecture/diagram.png (fourni) et référencé ci-dessous.
- Vue logique : [architecture/diagram.png](architecture/diagram.png)
- Description textuelle : Postman appelle directement les trois microservices exposés. Chaque microservice possède sa propre base H2 locale (dev). Prometheus scrute les endpoints /actuator/prometheus de chaque service.

## 3. Description des microservices

### 3.1 ms-membership (port 8081)

- Rôle : gestion des utilisateurs (CRUD, validation, exceptions, métriques, health checks).
- Données : base H2 locale (dev). Table users chargée via data.sql.
- Interfaces : REST /api/v1/users/\* ; expose Actuator et Prometheus.

### 3.2 ms-product (port 8082)

- Rôle : gestion du catalogue produits (validation prix/stock/catégorie, règles métier sur suppression et stock, métriques par catégorie).
- Données : base H2 locale (dev) avec jeux de données initiaux (5-10 produits recommandés).
- Interfaces : REST /api/v1/products/\* ; health check personnalisé pour stock bas ; compteur de créations par catégorie.

### 3.3 ms-order (port 8083)

- Rôle : gestion des commandes et lignes (validation user et produit, calcul total, règles d’état).
- Données : base H2 locale (dev).
- Interfaces : REST /api/v1/orders/\* ; health check de disponibilité des services membership et product ; métriques compteurs par statut et gauge du montant du jour.

## 4. Choix technologiques

- Langage : Java, framework Spring Boot (starter web, validation, actuator, data JPA, H2).
- Build : Maven (wrappers présents selon services).
- Persistance : H2 pour le développement/TP ; recommandation : PostgreSQL/MySQL en prod, une base par microservice.
- API : REST + OpenAPI/Swagger pour documentation des endpoints.
- Observabilité : Spring Boot Actuator + Micrometer Prometheus ; dashboards Grafana optionnels (monitoring/).
- Déploiement local : docker-compose (Prometheus, Grafana) ; microservices démarrés via Maven/IDE.

## 5. Communication inter-services

- Mode : appels REST synchrones. L’ordre appelle membership pour vérifier l’utilisateur et product pour vérifier stock/prix, via RestTemplate ou WebClient.
- Contrats : endpoints publics des services ; erreurs propagées avec codes HTTP 4xx/5xx explicites.
- Résilience recommandée : timeouts courts, retries limités, gestion des erreurs réseau, circuit breaker (ex. Resilience4j) si besoin.

## 6. Gestion des données

- Principe : base de données dédiée par microservice (pattern Database per Service). Pas de partage de schéma.
- Modélisation clé :
  - membership : users
  - product : products (category, price, stock, active)
  - order : orders + order_items (statut, totalAmount, quantités)
- Intégrité : validations applicatives ; règle métier pour empêcher la suppression d’un produit présent dans une commande.
- Migrations : H2 + data.sql pour dev ; envisager Flyway/Liquibase en environnements supérieurs.

## 7. Gestion des erreurs et résilience

- Validation Bean Validation sur DTOs (prix > 0, stock >= 0, catégories autorisées, quantités > 0, adresses obligatoires, etc.).
- Gestion centralisée des erreurs via @ControllerAdvice + mapping HTTP cohérent (400 pour validation, 404 pour ressources, 409 pour conflits métier, 500 pour erreurs internes).
- Contrainte métier : commande DELIVERED ou CANCELLED non modifiable ; stock jamais négatif ; suppression produit interdite s’il est référencé.
- Observabilité : health checks personnalisés (stock bas, dépendances externes), métriques métiers (compteurs produits par catégorie, commandes par statut, gauge montant du jour).
- Journaux : logs structurés recommandés pour corrélation (traceId/spanId si ajout de Spring Cloud Sleuth ou équivalent).

## 8. Sécurité et configuration

- Sécurité : non requise pour le TP ; prévoir plus tard l’ajout d’authN/authZ (JWT) et d’un gateway si besoin.
- Configuration : ports fixes 8081/8082/8083 ; Prometheus 9090 ; variables d’environnement pour URLs inter-services, DSNs bases, timeouts HTTP.

## 9. Déploiement et exploitation

- Démarrage local :
  1. Builder chaque service avec Maven.
  2. Lancer ms-membership, ms-product, ms-order (ordre libre, mais membership/product doivent être disponibles avant la création de commandes).
  3. Lancer Prometheus/Grafana via docker-compose pour le monitoring.
- Supervision : Prometheus scrape /actuator/prometheus ; dashboards Grafana (monitoring/dashboards/\*) utilisables en bonus.
- Tests : collection Postman (postman/platform-tests.json) couvrant scénarios complets et erreurs.

## 10. Évolutions envisagées

- Passage H2 → base relationnelle managée (PostgreSQL), externalisation des secrets, migration avec Flyway.
- Ajout d’un API Gateway et d’un service discovery si le nombre de services augmente.
- Sécurisation (JWT/OAuth2) et quota/ratelimiting.
- Durcissement résilience (circuit breakers, bulkheads), mise en cache catalogue pour alléger la charge sur product.
