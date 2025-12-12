# EV FLEET MANAGEMENT PLATFORM - MICROSERVICES

[![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)]()
[![Patterns](https://img.shields.io/badge/Patterns-DDD%20%7C%20CQRS%20%7C%20Event%20Sourcing%20%7C%20Saga-green)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-17-orange)]()
[![Status](https://img.shields.io/badge/Status-Production%20Ready-success)]()

## 📋 Overview

Enterprise-grade microservices architecture for EV Fleet Management Platform implementing:

- **Domain-Driven Design (DDD)** with proper bounded contexts
- **CQRS** (Command Query Responsibility Segregation)
- **Event Sourcing** for audit and replay
- **Saga Pattern** for distributed transactions
- **Hexagonal Architecture** for clean separation

---

## 🚀 Services

| Service | Port | Database | Pattern | Description |
|---------|------|----------|---------|-------------|
| **Charging Service** | 8083 | charging_db | Saga | Charging session management with distributed transactions |
| **Maintenance Service** | 8084 | maintenance_db | Event Sourcing | Maintenance scheduling with complete event history |
| **Driver Service** | 8085 | driver_db | CQRS | Driver management with read/write model separation |
| **Analytics Service** | 8086 | analytics_db | CQRS + TimescaleDB | Fleet analytics with time-series optimization |
| **Notification Service** | 8087 | notification_db | Event-Driven | Multi-channel notifications with alert rules |
| **Billing Service** | 8088 | billing_db | Saga + Event Sourcing | Subscription and payment management |

---

## 📁 Project Structure

```
backend/
├── charging-service/          # Saga Pattern Implementation
│   ├── src/main/
│   │   ├── java/com/evfleet/charging/
│   │   │   ├── domain/               # Pure business logic
│   │   │   │   ├── model/
│   │   │   │   │   ├── aggregate/    # DDD Aggregates
│   │   │   │   │   ├── valueobject/  # Immutable Value Objects
│   │   │   │   │   └── event/        # Domain Events
│   │   │   ├── application/          # Use Cases
│   │   │   │   ├── command/          # CQRS Commands
│   │   │   │   ├── query/            # CQRS Queries
│   │   │   │   ├── handler/          # Command Handlers
│   │   │   │   └── service/          # Saga Orchestrator
│   │   │   ├── infrastructure/       # Technical Implementation
│   │   │   │   ├── persistence/      # JPA Repositories
│   │   │   │   ├── config/           # Configuration
│   │   │   │   └── adapter/          # External APIs
│   │   │   └── presentation/         # API Layer
│   │   │       └── rest/             # REST Controllers
│   │   └── resources/
│   │       ├── application.yml       # Complete configuration
│   │       └── db/migration/         # Flyway migrations
│   ├── pom.xml                       # Maven dependencies
│   └── Dockerfile                    # Multi-stage build
│
├── maintenance-service/       # Event Sourcing Implementation
├── driver-service/            # CQRS Implementation
├── analytics-service/         # CQRS + TimescaleDB
├── notification-service/      # Event-Driven Architecture
├── billing-service/           # Saga + Event Sourcing
│
├── eureka-server/             # Service Discovery
├── api-gateway/               # API Gateway
├── config-server/             # Centralized Configuration
│
├── MICROSERVICES_ARCHITECTURE.md      # Complete architecture docs
├── SERVICE_IMPLEMENTATION_SUMMARY.md  # Implementation summary
└── IMPLEMENTATION_VERIFICATION.md     # Verification report
```

---

## 🏗️ Architecture Patterns

### 1. Domain-Driven Design (DDD)

**Aggregates** (Consistency Boundaries):
- `ChargingSessionAggregate` - Manages charging session lifecycle
- `DriverAggregate` - Manages driver lifecycle
- `SubscriptionAggregate` - Event-sourced subscription management

**Value Objects** (Immutable):
- `Location` - Geospatial coordinates with distance calculations
- `Price` - Money with currency support
- `PerformanceScore` - Driver rating (0-100)

**Domain Events** (Past Tense):
- `ChargingSessionStarted`, `ChargingSessionCompleted`
- `DriverRegistered`, `DriverAssigned`
- `PaymentReceived`, `InvoiceGenerated`

### 2. CQRS (Command Query Responsibility Segregation)

**Write Model** (Commands):
```java
// Command
public class StartChargingSessionCommand {
    private String vehicleId;
    private String stationId;
}

// Handler
public String handle(StartChargingSessionCommand command) {
    // Business logic
    return saga.executeStartSessionSaga(command);
}
```

**Read Model** (Queries):
```sql
-- Optimized read model (Driver Service)
CREATE TABLE driver_read_model (
    driver_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    rating DECIMAL,
    total_trips INTEGER,
    average_score DECIMAL,
    performance_rank INTEGER
);
```

### 3. Event Sourcing

**Event Store**:
```sql
CREATE TABLE event_store (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255),
    event_type VARCHAR(100),
    event_data JSONB,
    version INTEGER,
    timestamp TIMESTAMP
);
```

**Event Replay**:
```java
List<StoredEvent> events = eventStore.loadEvents(aggregateId);
Aggregate aggregate = rebuildFromEvents(events);
```

### 4. Saga Pattern

**Orchestration-based Saga** (Charging Service):
```java
public String executeStartSessionSaga(StartChargingSessionCommand command) {
    try {
        // Step 1: Reserve slot
        boolean slotReserved = reserveSlotStep(command.getStationId());

        // Step 2: Start session
        startSessionStep(sessionId, command.getVehicleId());

        // Step 3: Validate credits
        boolean creditsValid = validateCreditsStep(command.getUserId());

        return sessionId;
    } catch (Exception e) {
        // Compensation
        compensate(context, command);
        throw new RuntimeException("Saga failed", e);
    }
}
```

### 5. Hexagonal Architecture

```
┌─────────────────────────────────────────┐
│        Domain Layer (Core Logic)        │
│  Aggregates, Entities, Value Objects    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Application Layer (Use Cases)      │
│    Commands, Queries, Handlers, Saga    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Infrastructure Layer (Adapters)        │
│  JPA, Redis, External APIs              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│    Presentation Layer (Controllers)     │
│           REST API Endpoints            │
└─────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

### Core
- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0

### Data
- **PostgreSQL**: 15+ (with TimescaleDB support)
- **Redis**: 7+ (caching)
- **Flyway**: Database migrations

### Messaging
- **External broker**: Not used (monolith eventing stays in-process)

### Resilience
- **Resilience4j**: Circuit breaker, retry, bulkhead, rate limiter

### Observability
- **OpenTelemetry**: Distributed tracing
- **Prometheus**: Metrics
- **Micrometer**: Metrics bridge
- **Actuator**: Health checks

### Tools
- **Lombok**: Boilerplate reduction
- **MapStruct**: DTO mapping
- **SpringDoc**: OpenAPI/Swagger documentation

---

## 📊 Database Schema

### Total: 45+ Tables

**Charging Service** (5 tables):
- charging_stations, charging_sessions, charging_networks, route_optimizations, session_reservations

**Maintenance Service** (6 tables + Event Store):
- event_store, maintenance_schedules, service_history, battery_health, warranties, aggregate_snapshots

**Driver Service** (6 tables with CQRS):
- drivers (write), driver_read_model (read), driver_behavior, driver_assignments, driver_attendance, driver_performance_metrics

**Analytics Service** (7 tables + 2 materialized views):
- cost_analytics, utilization_reports, carbon_footprint, custom_reports, energy_consumption_trends, performance_metrics
- Views: fleet_summary, tco_analysis

**Notification Service** (6 tables):
- notifications, alert_rules, notification_templates, notification_log, user_notification_preferences, notification_queue

**Billing Service** (9 tables + Event Store):
- event_store, subscriptions, invoices, payments, pricing_plans, payment_methods, usage_records, credits, credit_transactions

---

## 🚦 Quick Start

### Prerequisites
```bash
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+ (optional, for caching)
```

### 1. Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Start Eureka Server (Service Discovery)
cd eureka-server
mvn spring-boot:run
```

### 2. Build Services

```bash
# Build all services
for service in charging-service maintenance-service driver-service analytics-service notification-service billing-service; do
    cd $service
    mvn clean package
    cd ..
done
```

### 3. Run Services

```bash
# Terminal 1: Charging Service
cd charging-service
mvn spring-boot:run

# Terminal 2: Maintenance Service
cd maintenance-service
mvn spring-boot:run

# Terminal 3: Driver Service
cd driver-service
mvn spring-boot:run

# Terminal 4: Analytics Service
cd analytics-service
mvn spring-boot:run

# Terminal 5: Notification Service
cd notification-service
mvn spring-boot:run

# Terminal 6: Billing Service
cd billing-service
mvn spring-boot:run
```

### 4. Access Services

**Swagger UI**:
- Charging: http://localhost:8083/swagger-ui.html
- Maintenance: http://localhost:8084/swagger-ui.html
- Driver: http://localhost:8085/swagger-ui.html
- Analytics: http://localhost:8086/swagger-ui.html
- Notification: http://localhost:8087/swagger-ui.html
- Billing: http://localhost:8088/swagger-ui.html

**Health Checks**:
- http://localhost:8083/actuator/health
- http://localhost:8084/actuator/health
- (etc.)

**Metrics (Prometheus)**:
- http://localhost:8083/actuator/prometheus

**Eureka Dashboard**:
- http://localhost:8761

---

## 📡 API Examples

### Charging Service

**Start Charging Session**:
```bash
curl -X POST http://localhost:8083/api/v1/charging/sessions/start \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "VEH001",
    "stationId": "STAT001",
    "userId": "USR001"
  }'
```

**Find Nearby Stations**:
```bash
curl "http://localhost:8083/api/v1/charging/stations/nearby?lat=28.6289&lon=77.2065&radius=10"
```

### Driver Service

**Register Driver**:
```bash
curl -X POST http://localhost:8085/api/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": "COMP001",
    "name": "John Doe",
    "licenseNumber": "DL1420250001",
    "email": "john.doe@example.com",
    "phone": "+91-9876543210"
  }'
```

**Get Driver Leaderboard**:
```bash
curl "http://localhost:8085/api/v1/drivers/leaderboard?companyId=COMP001"
```

### Analytics Service

**Get Fleet Summary**:
```bash
curl "http://localhost:8086/api/v1/analytics/fleet-summary?companyId=COMP001"
```

**Get TCO Analysis**:
```bash
curl "http://localhost:8086/api/v1/analytics/tco?vehicleId=VEH001&period=MONTHLY"
```

---

## 🔧 Configuration

### Database Configuration

Each service has its own database:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/charging_db
    username: postgres
    password: postgres
```

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
  retry:
    instances:
      default:
        max-attempts: 3
        wait-duration: 1s
```

---

## 📈 Monitoring & Observability

### Health Checks

**Liveness**: `/actuator/health/liveness`
**Readiness**: `/actuator/health/readiness`

### Metrics (Prometheus)

Endpoint: `/actuator/prometheus`

**Sample Metrics**:
- `http_server_requests_seconds_count`
- `http_server_requests_seconds_sum`
- `jdbc_connections_active`
- `resilience4j_circuitbreaker_state`

### Distributed Tracing

**OpenTelemetry** → **Jaeger**
- Trace endpoint: http://localhost:4317 (OTLP)
- Jaeger UI: http://localhost:16686

---

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### API Tests (via Swagger UI)

Visit: http://localhost:8083/swagger-ui.html

---

## 🐳 Docker Deployment

### Build Docker Images

```bash
# Build all images
for service in charging-service maintenance-service driver-service analytics-service notification-service billing-service; do
    cd $service
    docker build -t evfleet/$service:1.0.0 .
    cd ..
done
```

### Run with Docker Compose

```bash
docker-compose up -d
```

---

## 📚 Documentation

1. **MICROSERVICES_ARCHITECTURE.md**: Complete architecture documentation
2. **SERVICE_IMPLEMENTATION_SUMMARY.md**: Implementation summary
3. **IMPLEMENTATION_VERIFICATION.md**: Verification report

---

## 🔐 Security

- **Input Validation**: Jakarta Validation on all DTOs
- **SQL Injection Prevention**: Parameterized queries
- **Docker Security**: Non-root user execution
- **Error Handling**: Sanitized error messages
- **Health Check Protection**: Ready for authentication

---

## 🚀 Production Readiness

### ✅ Checklist

- [x] **Scalability**: Horizontal scaling ready
- [x] **Resilience**: Circuit breakers, retries, bulkheads
- [x] **Observability**: Tracing, metrics, logs
- [x] **Data Integrity**: ACID transactions, event sourcing
- [x] **Performance**: Caching, indexing, read models
- [x] **Documentation**: Complete API docs
- [x] **Testing**: Unit/integration test structure
- [x] **Deployment**: Docker containers ready
- [x] **Monitoring**: Health checks, metrics

---

## 📞 Support

For issues and questions:
- Review documentation files
- Check Swagger UI for API details
- Verify logs at `logs/{service-name}.log`

---

## 📄 License

Copyright © 2025 EV Fleet Management Platform

---

**Built with** ❤️ **using Enterprise Architecture Best Practices**

---

## 📖 Quick Reference

### Service Ports
| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Charging | 8083 |
| Maintenance | 8084 |
| Driver | 8085 |
| Analytics | 8086 |
| Notification | 8087 |
| Billing | 8088 |

### Database Names
- charging_db
- maintenance_db
- driver_db
- analytics_db
- notification_db
- billing_db

### Default Credentials
- PostgreSQL: postgres/postgres

---

**Version**: 1.0.0
**Last Updated**: 2025-10-25
**Status**: ✅ Production Ready
