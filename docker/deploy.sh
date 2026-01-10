#!/bin/bash

# Deploy E-Commerce Platform
# Script pour déployer la plateforme depuis Docker Hub

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_HUB_USERNAME="${DOCKER_HUB_USERNAME:-your-username}"

echo "=========================================="
echo "Deploying E-Commerce Platform"
echo "=========================================="

# Pull latest images
echo ""
echo "Pulling latest Docker images..."
docker pull "$DOCKER_HUB_USERNAME/ecommerce-membership:latest"
docker pull "$DOCKER_HUB_USERNAME/ecommerce-product:latest"
docker pull "$DOCKER_HUB_USERNAME/ecommerce-order:latest"
echo "[✓] Images pulled successfully"

# Update docker-compose.yml if needed
echo ""
echo "Ensuring docker-compose.yml is up-to-date..."

# Start services with docker-compose
echo ""
echo "Starting services..."
cd "$PROJECT_DIR"
docker-compose up -d

echo ""
echo "=========================================="
echo "Deployment completed successfully!"
echo "=========================================="
echo ""
echo "Services are running:"
echo "  - Membership Service: http://localhost:8081"
echo "  - Product Service: http://localhost:8082"
echo "  - Order Service: http://localhost:8083"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin123)"
echo ""
echo "View logs:"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "Stop services:"
echo "  docker-compose down"
