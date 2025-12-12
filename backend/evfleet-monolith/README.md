# EVFleet Modular Monolith

**Complete EV Fleet Management Platform - Migrated from Microservices to Modular Monolith**

## 🎯 Architecture Overview

This is a **Spring Modulith-based** modular monolith consolidating 11 microservices into a single deployable unit while maintaining strong module boundaries and event-driven communication.

### Why Modular Monolith?

- **90% Cost Reduction**: From ₹56K-64K/month to ₹5K-7K/month
- **Simplified Deployment**: Single Docker container vs 11 separate services
- **Maintained Modularity**: Clean module boundaries with Spring Modulith
- **Easy to Extract**: Modules can be extracted back to microservices when needed

## 📦 Modules

1. **common** - Shared infrastructure (events, exceptions, DTOs, config)
2. **auth** - Authentication & Authorization (User, Role management)
3. **fleet** - Vehicle & Fleet Management (Vehicles, Trips, Geofences)
4. **charging** - Charging Infrastructure (Stations, Sessions)
5. **maintenance** - Maintenance Management (Service records)
6. **driver** - Driver Management (Driver profiles, assignments)
7. **analytics** - Analytics & Reporting (Fleet summaries)
8. **notification** - Notification Management (Alerts, notifications)
9. **billing** - Billing & Invoicing (Subscriptions, invoices)

## 🗄️ Database Architecture

**Multi-DataSource Setup** - 8 Separate PostgreSQL Databases for easy future extraction

## 🚀 Quick Start

```bash
# Start infrastructure
docker-compose up -d postgres redis

# Run application
mvn spring-boot:run

# Access
# - App: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html
```

## 📡 Key Endpoints

- `/api/auth/*` - Authentication
- `/api/fleet/vehicles` - Vehicle management
- `/api/charging/stations` - Charging stations
- `/actuator/health` - Health check

## 🔄 Event-Driven Architecture

Modules communicate via async events (no direct dependencies):
- VehicleCreatedEvent → Notification
- BatteryLowEvent → Notification + Analytics
- ChargingSessionCompletedEvent → Analytics + Billing

## 📊 Cost Savings

| Deployment | Monthly Cost |
|------------|--------------|
| 11 Microservices (AWS) | ₹56,000 |
| Modular Monolith (Hetzner) | ₹3,600 |
| **Savings** | **94%** |

## 🔐 Security

- Firebase Authentication
- JWT token verification
- RBAC (Role-Based Access Control)
- CORS configuration

---

**Built with Spring Boot 3.2, Spring Modulith, Java 17**
