#!/bin/bash
set -e

echo "Building all microservices..."
(cd ../ms-product && mvn clean package -DskipTests)
(cd ../ms-order && mvn clean package -DskipTests)
(cd ../ms-user && mvn clean package -DskipTests)

echo "Building Docker images..."
docker build -t e-commerce-ms-product ../ms-product
docker build -t e-commerce-ms-order ../ms-order
docker build -t e-commerce-ms-user ../ms-user

echo "Finished"