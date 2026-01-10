#!/bin/bash

# Publish All Services to Docker Hub
# Script pour publier les images Docker sur Docker Hub

set -e

DOCKER_HUB_USERNAME="${DOCKER_HUB_USERNAME:-your-username}"

echo "=========================================="
echo "Publishing to Docker Hub"
echo "=========================================="

# Check if user is logged in to Docker
if ! docker info | grep -q "Username"; then
    echo "Please login to Docker Hub first:"
    echo "  docker login"
    exit 1
fi

# Publish Membership Service
echo ""
echo "[1/3] Publishing ecommerce-membership..."
docker push "$DOCKER_HUB_USERNAME/ecommerce-membership:1.0"
docker push "$DOCKER_HUB_USERNAME/ecommerce-membership:latest"
echo "[✓] ecommerce-membership published"

# Publish Product Service
echo ""
echo "[2/3] Publishing ecommerce-product..."
docker push "$DOCKER_HUB_USERNAME/ecommerce-product:1.0"
docker push "$DOCKER_HUB_USERNAME/ecommerce-product:latest"
echo "[✓] ecommerce-product published"

# Publish Order Service
echo ""
echo "[3/3] Publishing ecommerce-order..."
docker push "$DOCKER_HUB_USERNAME/ecommerce-order:1.0"
docker push "$DOCKER_HUB_USERNAME/ecommerce-order:latest"
echo "[✓] ecommerce-order published"

echo ""
echo "=========================================="
echo "All services published successfully!"
echo "=========================================="
echo ""
echo "Docker Hub repositories:"
echo "  - https://hub.docker.com/r/$DOCKER_HUB_USERNAME/ecommerce-membership"
echo "  - https://hub.docker.com/r/$DOCKER_HUB_USERNAME/ecommerce-product"
echo "  - https://hub.docker.com/r/$DOCKER_HUB_USERNAME/ecommerce-order"
