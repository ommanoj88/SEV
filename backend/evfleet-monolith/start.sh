#!/bin/bash

# EVFleet Monolith Startup Script
# Author: SEV Platform Team

set -e

echo "========================================="
echo "  EVFleet Monolith Startup Script"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check Java
echo -n "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo -e " ${GREEN}✓${NC} Java $JAVA_VERSION found"
    else
        echo -e " ${RED}✗${NC} Java 17+ required, found Java $JAVA_VERSION"
        exit 1
    fi
else
    echo -e " ${RED}✗${NC} Java not found"
    exit 1
fi

# Check Maven
echo -n "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    echo -e " ${GREEN}✓${NC} Maven found"
else
    echo -e " ${RED}✗${NC} Maven not found"
    exit 1
fi

# Check PostgreSQL
echo -n "Checking PostgreSQL connection..."
if nc -z localhost 5432 2>/dev/null; then
    echo -e " ${GREEN}✓${NC} PostgreSQL running on port 5432"
else
    echo -e " ${YELLOW}!${NC} PostgreSQL not detected on localhost:5432"
    echo "  Starting PostgreSQL with Docker..."
    docker-compose up -d postgres
    sleep 5
fi

# Check Redis
echo -n "Checking Redis connection..."
if nc -z localhost 6379 2>/dev/null; then
    echo -e " ${GREEN}✓${NC} Redis running on port 6379"
else
    echo -e " ${YELLOW}!${NC} Redis not detected on localhost:6379"
    echo "  Starting Redis with Docker..."
    docker-compose up -d redis
    sleep 3
fi

# Check Firebase config
echo -n "Checking Firebase configuration..."
if [ -f "src/main/resources/firebase-service-account.json" ]; then
    echo -e " ${GREEN}✓${NC} Firebase config found"
else
    echo -e " ${RED}✗${NC} firebase-service-account.json not found"
    echo "  Please place your Firebase config at:"
    echo "  src/main/resources/firebase-service-account.json"
    exit 1
fi

echo ""
echo "========================================="
echo "  Starting EVFleet Monolith"
echo "========================================="
echo ""

# Build
echo "Building application..."
mvn clean package -DskipTests

# Run
echo ""
echo "Starting application..."
echo "Access Swagger UI at: http://localhost:8080/swagger-ui.html"
echo ""
java -jar target/evfleet-monolith-1.0.0.jar
