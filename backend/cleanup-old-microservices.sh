#!/bin/bash

# Cleanup Script for Old Microservices
# This script removes the old microservice folders that have been migrated to the monolith
# Location: backend/evfleet-monolith/

echo "=========================================="
echo "  EVFleet Old Microservices Cleanup"
echo "=========================================="
echo ""

echo "⚠️  WARNING: This will DELETE the following old microservice folders:"
echo ""
echo "  - analytics-service"
echo "  - auth-service"
echo "  - billing-service"
echo "  - charging-service"
echo "  - driver-service"
echo "  - fleet-service"
echo "  - maintenance-service"
echo "  - notification-service"
echo "  - api-gateway"
echo "  - config-server"
echo "  - eureka-server"
echo ""
echo "✅ These have ALL been migrated to: evfleet-monolith/"
echo ""

read -p "Are you sure you want to delete these folders? (yes/no): " confirm

if [ "$confirm" = "yes" ]; then
    echo ""
    echo "Deleting old microservices..."

    cd "$(dirname "$0")"

    rm -rf analytics-service
    echo "✓ Deleted analytics-service"

    rm -rf auth-service
    echo "✓ Deleted auth-service"

    rm -rf billing-service
    echo "✓ Deleted billing-service"

    rm -rf charging-service
    echo "✓ Deleted charging-service"

    rm -rf driver-service
    echo "✓ Deleted driver-service"

    rm -rf fleet-service
    echo "✓ Deleted fleet-service"

    rm -rf maintenance-service
    echo "✓ Deleted maintenance-service"

    rm -rf notification-service
    echo "✓ Deleted notification-service"

    rm -rf api-gateway
    echo "✓ Deleted api-gateway"

    rm -rf config-server
    echo "✓ Deleted config-server"

    rm -rf eureka-server
    echo "✓ Deleted eureka-server"

    echo ""
    echo "=========================================="
    echo "✅ Cleanup Complete!"
    echo "=========================================="
    echo ""
    echo "Remaining folders:"
    ls -la | grep "^d" | awk '{print "  - " $NF}'
    echo ""
    echo "Your monolith is at: evfleet-monolith/"
    echo ""
else
    echo ""
    echo "❌ Cleanup cancelled."
    echo ""
fi
