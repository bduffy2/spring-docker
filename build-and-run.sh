#!/usr/bin/env bash
set -e

echo "==================================================="
echo " Spring Boot 4 Microservices: Build and Run"
echo "==================================================="

echo "[1/2] Building microservice JARs using Maven Wrapper..."
./mvnw clean package -DskipTests

echo "[2/2] Building Docker images and starting Docker Compose..."
docker compose up --build
