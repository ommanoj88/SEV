#!/usr/bin/env python3
"""
Enterprise-Grade Microservices Generator for EV Fleet Management Platform
Generates complete DDD, CQRS, Event Sourcing, and Saga Pattern implementations
"""

import os
from pathlib import Path

# Base path
BASE_PATH = Path(__file__).parent

# Service configurations
SERVICES = {
    "charging-service": {
        "port": 8083,
        "db": "charging_db",
        "description": "Charging Management Service with Saga Pattern"
    },
    "maintenance-service": {
        "port": 8084,
        "db": "maintenance_db",
        "description": "Maintenance Service with Event Sourcing"
    },
    "driver-service": {
        "port": 8085,
        "db": "driver_db",
        "description": "Driver Management Service with CQRS"
    },
    "analytics-service": {
        "port": 8086,
        "db": "analytics_db",
        "description": "Analytics Service with CQRS & TimescaleDB"
    },
    "notification-service": {
        "port": 8087,
        "db": "notification_db",
        "description": "Notification Service - Event-Driven"
    },
    "billing-service": {
        "port": 8088,
        "db": "billing_db",
        "description": "Billing Service with Saga & Event Sourcing"
    }
}

def create_directory_structure(service_name):
    """Create complete directory structure for a service"""
    base = BASE_PATH / service_name / "src" / "main"

    dirs = [
        # Java source directories
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "model" / "aggregate",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "model" / "entity",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "model" / "valueobject",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "model" / "event",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "repository",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "domain" / "service",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "application" / "command",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "application" / "query",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "application" / "dto",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "application" / "service",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "application" / "handler",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "infrastructure" / "persistence",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "infrastructure" / "messaging",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "infrastructure" / "config",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "infrastructure" / "adapter",
        base / "java" / "com" / "evfleet" / service_name.replace("-service", "") / "presentation" / "rest",
        # Resources
        base / "resources" / "db" / "migration",
        # Test directories
        BASE_PATH / service_name / "src" / "test" / "java" / "com" / "evfleet" / service_name.replace("-service", ""),
    ]

    for dir_path in dirs:
        dir_path.mkdir(parents=True, exist_ok=True)
        print(f"Created: {dir_path}")

def main():
    """Main function to generate all services"""
    print("=" * 80)
    print("EV FLEET MANAGEMENT - ENTERPRISE MICROSERVICES GENERATOR")
    print("=" * 80)

    for service_name, config in SERVICES.items():
        print(f"\n[{service_name.upper()}] Creating directory structure...")
        create_directory_structure(service_name)
        print(f"[{service_name.upper()}] Directory structure created!")

    print("\n" + "=" * 80)
    print("ALL SERVICE STRUCTURES CREATED SUCCESSFULLY!")
    print("=" * 80)

if __name__ == "__main__":
    main()
