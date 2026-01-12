#!/bin/bash
set -e

USERNAME="silv04"

echo "Logging Docker Hub"
docker login

echo "Tagging images"
docker tag e-commerce-membership $USERNAME/ecommerce-membership:1.0
docker tag e-commerce-product $USERNAME/ecommerce-product:1.0
docker tag e-commerce-order $USERNAME/ecommerce-order:1.0

echo "Pushing images to Docker Hub"
docker push $USERNAME/ecommerce-membership:1.0
docker push $USERNAME/ecommerce-product:1.0
docker push $USERNAME/ecommerce-order:1.0

echo "Finished"
