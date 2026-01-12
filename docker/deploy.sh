#!/bin/bash
set -e

echo "Pull images from Docker Hub"
docker-compose pull

echo "Starting the stack"
docker-compose up -d

echo "Finished"