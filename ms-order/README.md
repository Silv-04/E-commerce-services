# Microservice Order (ms-order)

## Description
Microservice de gestion des commandes pour la plateforme e-commerce. Ce service permet de créer, consulter, modifier et annuler des commandes.

## Technologies utilisées
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring WebFlux (WebClient pour les appels inter-services)
- H2 Database (en mémoire)
- Lombok
- OpenAPI/Swagger
- Micrometer/Prometheus

## Port
Le service tourne sur le port **8083**

## Prérequis
- Java 17+
- Maven 3.6+
- Les services `ms-membership` (port 8081) et `ms-product` (port 8082) doivent être démarrés

## Démarrage

```bash
cd ms-order
mvn spring-boot:run
```

## Endpoints REST

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/orders` | Liste toutes les commandes |
| GET | `/api/v1/orders/{id}` | Détail d'une commande |
| POST | `/api/v1/orders` | Créer une commande |
| PUT | `/api/v1/orders/{id}/status` | Changer le statut |
| DELETE | `/api/v1/orders/{id}` | Annuler une commande |
| GET | `/api/v1/orders/user/{userId}` | Commandes d'un utilisateur |
| GET | `/api/v1/orders/status/{status}` | Filtrer par statut |

## Exemple de création de commande

```json
POST /api/v1/orders
{
    "userId": 1,
    "shippingAddress": "123 Rue de Paris, 75001 Paris",
    "items": [
        {
            "productId": 1,
            "quantity": 2
        },
        {
            "productId": 2,
            "quantity": 1
        }
    ]
}
```

## Exemple de mise à jour du statut

```json
PUT /api/v1/orders/1/status
{
    "status": "CONFIRMED"
}
```

## Statuts possibles
- `PENDING` : En attente de confirmation
- `CONFIRMED` : Confirmée
- `SHIPPED` : Expédiée
- `DELIVERED` : Livrée
- `CANCELLED` : Annulée

## URLs utiles
- Swagger UI : http://localhost:8083/swagger-ui.html
- API Docs : http://localhost:8083/api-docs
- H2 Console : http://localhost:8083/h2-console
- Health : http://localhost:8083/actuator/health
- Metrics : http://localhost:8083/actuator/prometheus

## Tests

```bash
mvn test
```

## Architecture

```
ms-order/
├── src/main/java/com/ecommerce/order/
│   ├── OrderApplication.java
│   ├── application/
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── service/
│   ├── domain/
│   │   ├── entity/
│   │   ├── enumerate/
│   │   └── repository/
│   └── infrastructure/
│       ├── client/
│       ├── exception/
│       ├── health/
│       └── web/controller/
└── src/main/resources/
    ├── application.yml
    └── data.sql
```
