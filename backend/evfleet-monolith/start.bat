@echo off
REM EVFleet Monolith Startup Script for Windows
REM Author: SEV Platform Team

echo =========================================
echo   EVFleet Monolith Startup Script
echo =========================================
echo.

REM Check Java
echo Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found. Please install Java 17 or higher.
    pause
    exit /b 1
)
echo [OK] Java found

REM Check Maven
echo Checking Maven installation...
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found. Please install Maven 3.9 or higher.
    pause
    exit /b 1
)
echo [OK] Maven found

REM Check PostgreSQL
echo Checking PostgreSQL...
netstat -an | find "5432" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] PostgreSQL not detected on port 5432
    echo Starting PostgreSQL with Docker...
    docker-compose up -d postgres
    timeout /t 5 /nobreak >nul
)
echo [OK] PostgreSQL running

REM Check Redis
echo Checking Redis...
netstat -an | find "6379" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Redis not detected on port 6379
    echo Starting Redis with Docker...
    docker-compose up -d redis
    timeout /t 3 /nobreak >nul
)
echo [OK] Redis running

REM Check Firebase config
echo Checking Firebase configuration...
if not exist "src\main\resources\firebase-service-account.json" (
    echo [ERROR] firebase-service-account.json not found
    echo Please place your Firebase config at:
    echo src\main\resources\firebase-service-account.json
    pause
    exit /b 1
)
echo [OK] Firebase config found

echo.
echo =========================================
echo   Building Application
echo =========================================
echo.
mvn clean package -DskipTests

echo.
echo =========================================
echo   Starting Application
echo =========================================
echo.
echo Access Swagger UI at: http://localhost:8080/swagger-ui.html
echo.
java -jar target\evfleet-monolith-1.0.0.jar
