## Guide de Déploiement – Plateforme E-Commerce Microservices

---

## 1. Présentation générale

Ce document décrit les étapes nécessaires pour **compiler, lancer et vérifier** le bon fonctionnement de la plateforme **E-Commerce Microservices**, composée des services suivants :

| Microservice | Rôle | Port |
|--------------|------|------|
| ms-membership | Gestion des utilisateurs | 8081 |
| ms-product | Gestion du catalogue produit | 8082 |
| ms-order | Gestion des commandes | 8083 |

Chaque microservice est une application **Spring Boot**, communiquant via des **API REST**.

---

## 2. Prérequis

### Logiciels requis
- **Java JDK 21**
- **Maven 3.9+**
- **Git**
- **Docker & Docker Compose**

### Configuration Java (pom.xml)
Tous les microservices utilisent Java 21 :

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

---

## Instructions de démarrage pas-à-pas

### Cloner le repository

```bash
git clone https://github.com/Silv-04/E-commerce-services.git
cd E-commerce-services
```

---

### Compiler chaque service

Chaque microservice doit être compilé séparément.

```bash
cd ms-membership
mvn clean compile

cd ../ms-product
mvn clean compile

cd ../ms-order
mvn clean compile
```

---

### Lancer dans le bon ordre

L’ordre de lancement est important à cause des dépendances inter‑services.

#### 1️⃣ Service Membership

```bash
cd ms-membership
mvn spring-boot:run
```

Port : **8081**

---

#### 2️⃣ Service Product

```bash
cd ms-product
mvn spring-boot:run
```

Port : **8082**

---

#### 3️⃣ Service Order

```bash
cd ms-order
mvn spring-boot:run
```

Port : **8083**

---

### Vérifier que tout fonctionne

#### Health checks

- Membership  
  `http://localhost:8081/actuator/health`

- Product  
  `http://localhost:8082/actuator/health`

- Order  
  `http://localhost:8083/actuator/health`

Résultat attendu :
```json
{ "status": "UP" }
```

---

#### Documentation OpenAPI / Swagger (Product)

```text
http://localhost:8082/v3/api-docs
```

---

#### Endpoints Actuator (Product)

- Health
```text
http://localhost:8082/actuator/health
```

- Info
```text
http://localhost:8082/actuator/info
```

- Métrique personnalisée (produits créés par catégorie)
```text
http://localhost:8082/actuator/metrics/product.created?tag=type:FOOD
```

---
