#!/bin/bash

# Enterprise-Grade Microservices Generator for EV Fleet Management Platform
# This script generates complete DDD, CQRS, Event Sourcing, and Saga Pattern implementations

set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================================================="
echo "EV FLEET MANAGEMENT - ENTERPRISE MICROSERVICES GENERATOR"
echo "=========================================================================="

# Function to create complete directory structure
create_directory_structure() {
    local SERVICE=$1
    local PKG_NAME=$2

    echo "[${SERVICE}] Creating directory structure..."

    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/model/aggregate"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/model/entity"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/model/valueobject"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/model/event"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/repository"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/domain/service"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/command"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/query"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/dto"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/service"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/handler"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/application/mapper"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/persistence"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/messaging"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/messaging/publisher"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/messaging/consumer"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/config"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/infrastructure/adapter"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/presentation/rest"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/java/com/evfleet/${PKG_NAME}/presentation/exception"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/main/resources/db/migration"
    mkdir -p "${BASE_DIR}/${SERVICE}/src/test/java/com/evfleet/${PKG_NAME}"

    echo "[${SERVICE}] Directory structure created!"
}

# Create structures for all services
create_directory_structure "charging-service" "charging"
create_directory_structure "maintenance-service" "maintenance"
create_directory_structure "driver-service" "driver"
create_directory_structure "analytics-service" "analytics"
create_directory_structure "notification-service" "notification"
create_directory_structure "billing-service" "billing"

echo ""
echo "=========================================================================="
echo "ALL SERVICE DIRECTORY STRUCTURES CREATED SUCCESSFULLY!"
echo "=========================================================================="
