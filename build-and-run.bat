@echo off
echo ===================================================
echo  Spring Boot 4 Microservices: Build and Run
echo ===================================================

echo [1/2] Building microservice JARs using Maven Wrapper...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed.
    exit /b %ERRORLEVEL%
)

echo [2/2] Building Docker images and starting Docker Compose...
docker compose up --build
