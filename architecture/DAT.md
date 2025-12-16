# Document d'Architecture Technique (DAT) - Plateforme E-Commerce

## 1. Vue d'ensemble

- Plateforme composée de trois microservices : **ms-membership** (utilisateurs), **ms-product** (catalogue), **ms-order** (commandes).
- Observabilité : métriques **Prometheus** (port 9090) et health checks **Spring Boot Actuator**.
- Outils de test/recette : collection **Postman** pour orchestrer les scénarios.

## 2. Schéma d'architecture


- Vue logique : [architecture/diagram.png](architecture/diagram.png)
- Description textuelle : **Postman** appelle directement les trois microservices exposés. 
Chaque microservice possède sa propre base **H2** locale (dev). **Prometheus** scrute les endpoints /actuator/prometheus de chaque service.

## 3. Description des microservices

### 3.1 ms-membership (port 8081)

- Rôle : gestion des utilisateurs (CRUD, validation, exceptions, métriques, health checks).
- Données : base **H2** locale (dev). Table users chargée via data.sql.
- Interfaces : REST /api/v1/users/\* ; expose **Actuator** et **Prometheus**.

### 3.2 ms-product (port 8082)

- Rôle : gestion du catalogue produits (validation prix/stock/catégorie, règles métier sur suppression et stock, métriques par catégorie).
- Données : base **H2** locale avec jeux de données initiaux (5-10 produits recommandés).
- Interfaces : REST /api/v1/products/\* ; health check personnalisé pour stock bas ; compteur de créations par catégorie.

### 3.3 ms-order (port 8083)

- Rôle : gestion des commandes et lignes (validation user et produit, calcul total, règles d’état).
- Données : base H2 locale (dev).
- Interfaces : REST /api/v1/orders/\* ; health check de disponibilité des services membership et product ; métriques compteurs par statut et gauge du montant du jour.

## 4. Choix technologiques

- Langage : Java, framework Spring Boot (starter web, validation, actuator, data JPA, H2).
- Build : Maven (wrappers présents selon services).
- Persistance : H2 pour le développement/TP.
- API : REST + OpenAPI/Swagger pour documentation des endpoints.
- Observabilité : Spring Boot Actuator + Micrometer Prometheus ; dashboards Grafana(monitoring).

## 5. Communication inter-services

- Mode : appels REST synchrones. L’ordre appelle membership pour vérifier l’utilisateur et product pour vérifier stock/prix, via RestTemplate ou WebClient.
- Contrats : endpoints publics des services ; erreurs propagées avec codes HTTP 4xx/5xx explicites.

## 6. Gestion des données

- Principe : base de données dédiée par microservice.
- Modélisation clé :
  - membership : users
  - product : products (category, price, stock, active)
  - order : orders + order_items (statut, totalAmount, quantités)
- Intégrité : validations applicatives ; règle métier pour empêcher la suppression d’un produit présent dans une commande.
- Migrations : H2 + data.sql pour dev.

## 7. Gestion des erreurs et résilience

- Validation Bean Validation sur DTOs (prix > 0, stock >= 0, catégories autorisées, quantités > 0, adresses obligatoires, etc.).
- Gestion centralisée des erreurs via @ControllerAdvice + mapping HTTP cohérent (400 pour validation, 404 pour ressources, 409 pour conflits métier, 500 pour erreurs internes).
- Contrainte métier : commande DELIVERED ou CANCELLED non modifiable ; stock jamais négatif ; suppression produit interdite s’il est référencé.
- Observabilité : health checks personnalisés (stock bas, dépendances externes), métriques métiers (compteurs produits par catégorie, commandes par statut, gauge montant du jour).

## 8. Sécurité et configuration


- Configuration : ports fixes 8081/8082/8083 ; Prometheus 9090.

## 9. Déploiement et exploitation

- Démarrage local :
  1. Builder chaque service avec Maven.
  2. Lancer **ms-membership** et **ms-product** avant **ms-order** (membership/product doivent être disponibles avant la création de commandes).
  3. Lancer **Prometheus**/**Grafana** via docker-compose pour le monitoring.
- Supervision : **Prometheus** scrape /actuator/prometheus ; dashboards **Grafana** (monitoring/dashboards/\*).
- Tests : collection Postman (postman/platform-tests.json) couvrant scénarios complets et erreurs.