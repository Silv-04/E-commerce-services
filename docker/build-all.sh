#!/bin/bash

# Build All Services - E-commerce Platform
# Script pour compiler tous les microservices et construire les images Docker

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_HUB_USERNAME="${DOCKER_HUB_USERNAME:-your-username}"

echo "=========================================="
echo "Building E-Commerce Platform"
echo "=========================================="

# Build Membership Service
echo ""
echo "[1/3] Building ms-membership..."
cd "$PROJECT_DIR/ms-membership"
mvn clean package -DskipTests
docker build -t "$DOCKER_HUB_USERNAME/ecommerce-membership:1.0" -t "$DOCKER_HUB_USERNAME/ecommerce-membership:latest" .
echo "[✓] ms-membership built successfully"

# Build Product Service
echo ""
echo "[2/3] Building ms-product..."
cd "$PROJECT_DIR/ms-product"
mvn clean package -DskipTests
docker build -t "$DOCKER_HUB_USERNAME/ecommerce-product:1.0" -t "$DOCKER_HUB_USERNAME/ecommerce-product:latest" .
echo "[✓] ms-product built successfully"

# Build Order Service
echo ""
echo "[3/3] Building ms-order..."
cd "$PROJECT_DIR/ms-order"
mvn clean package -DskipTests
docker build -t "$DOCKER_HUB_USERNAME/ecommerce-order:1.0" -t "$DOCKER_HUB_USERNAME/ecommerce-order:latest" .
echo "[✓] ms-order built successfully"

echo ""
echo "=========================================="
echo "All services built successfully!"
echo "=========================================="
echo ""
echo "Docker images created:"
echo "  - $DOCKER_HUB_USERNAME/ecommerce-membership:1.0"
echo "  - $DOCKER_HUB_USERNAME/ecommerce-product:1.0"
echo "  - $DOCKER_HUB_USERNAME/ecommerce-order:1.0"
echo ""
echo "Next step: Run 'bash docker/publish-all.sh' to push to Docker Hub"
