# Microservice Product (ms-product)

## Description
Microservice de gestion des produits pour la plateforme e-commerce. Ce service permet de créer, consulter, modifier et supprimer des produits.

## Technologies utilisées
- Java 21
- Spring Boot 3.5.8
- Spring Data JPA
- Spring Web
- H2 Database (en mémoire)
- Lombok
- OpenAPI/Swagger
- Micrometer/Prometheus

## Port
Le service tourne sur le port **8082**

## Prérequis
- Java 17+
- Maven 3.6+

## Démarrage

```bash
cd ms-product
mvn spring-boot:run
```

## Endpoints REST

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/products` | Liste tous les produits |
| GET | `/api/v1/products/{id}` | Détail d'un produit |
| POST | `/api/v1/products` | Créer un produit |
| PUT | `/api/v1/products/{id}` | Modifier un produit |
| PATCH | `/api/v1/products/{id}/stock` | Mettre à jour le stock |
| DELETE | `/api/v1/products/{id}` | Supprimer un produit |
| GET | `/api/v1/products/category/{category}` | Filtrer par catégorie |
| GET | `/api/v1/products/available` | Produits en stock |
| GET | `/api/v1/products/search?name=...` | Rechercher par nom |

## Exemple de création de produit

```json
POST /api/v1/products
{
    "name": "Laptop X",
    "description": "Ultrabook 13 pouces",
    "price": 1299.99,
    "stock": 10,
    "category": "ELECTRONICS"
}
```

## Exemple de mise à jour du stock

```json
PATCH /api/v1/products/1/stock
{
    "stock": 8
}
```

## URLs utiles
- Swagger UI : http://localhost:8082/swagger-ui.html
- API Docs : http://localhost:8082/api-docs
- H2 Console : http://localhost:8082/h2-console
- Health : http://localhost:8082/actuator/health
- Metrics : http://localhost:8082/actuator/prometheus

## Tests

```bash
mvn test
```

## Architecture

```
ms-product/
├── src/main/java/com/ecommerce/product/
│   ├── ProductApplication.java
│   ├── application/
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── service/
│   ├── domain/
│   │   ├── entity/
│   │   ├── enumerate/
│   │   └── repository/
│   └── infrastructure/
│       ├── exception/
│       └── web/controller/
└── src/main/resources/
    ├── application.yml
    └── data.sql
```
