# EV FLEET MANAGEMENT - ENTERPRISE MICROSERVICES ARCHITECTURE

## Overview

This document describes the complete enterprise-grade microservices architecture for the EV Fleet Management Platform, implementing Domain-Driven Design (DDD), CQRS, Event Sourcing, Saga patterns, and Hexagonal Architecture.

---

## Architecture Principles

### 1. Domain-Driven Design (DDD)
- **Aggregates**: Encapsulate business logic and maintain consistency boundaries
- **Entities**: Objects with identity and lifecycle
- **Value Objects**: Immutable objects defined by their attributes
- **Domain Events**: Capture business-significant occurrences
- **Repositories**: Abstract data access

### 2. CQRS (Command Query Responsibility Segregation)
- **Write Model**: Handles commands and business logic
- **Read Model**: Optimized for queries (materialized views)
- **Event-driven synchronization** between models

### 3. Event Sourcing
- **Event Store**: Append-only log of all domain events
- **Event Replay**: Rebuild aggregate state from events
- **Snapshots**: Performance optimization for large event streams

### 4. Saga Pattern
- **Orchestration-based Sagas**: Centralized coordination
- **Compensation Logic**: Rollback on failures
- **Distributed Transactions**: Across multiple services

### 5. Hexagonal Architecture (Ports & Adapters)
```
Domain (Pure Business Logic)
    ↓
Application (Use Cases)
    ↓
Infrastructure (Technical Implementation)
    ↓
Presentation (API Layer)
```

---

## Service Architecture

### 1. CHARGING SERVICE (Port 8083)
**Database**: `charging_db`
**Pattern**: Saga Pattern

#### Domain Layer
**Aggregates**:
- `ChargingSessionAggregate`: Manages charging session lifecycle with domain events
- `ChargingStationAggregate`: Manages station availability and slot reservations

**Value Objects**:
- `Location`: Geographical coordinates with distance calculations
- `Price`: Money representation with currency support
- `Energy`: Energy in kWh with unit conversions
- `SessionDuration`: Time duration calculations

**Domain Events**:
- `ChargingSessionStarted`
- `ChargingSessionCompleted`
- `ChargingSessionFailed`
- `StationOccupied`
- `StationAvailable`

#### Application Layer
**Commands (CQRS Write)**:
- `StartChargingSessionCommand`
- `EndChargingSessionCommand`
- `ReserveChargingSlotCommand`
- `CreateStationCommand`

**Queries (CQRS Read)**:
- `GetAvailableStationsQuery`
- `GetSessionHistoryQuery`
- `GetStationByLocationQuery`

**Saga Orchestrator**:
- `ChargingSessionSaga`: Distributed transaction coordination
  - Step 1: Reserve slot
  - Step 2: Start session
  - Step 3: Validate billing credits
  - Step 4: Complete session
  - Compensation: Release slot, refund credits

#### Infrastructure Layer
- **RabbitMQ**: Event publishing with retry and DLQ
- **Redis**: Station availability caching (10-min TTL)
- **Resilience4j**: Circuit breaker for external APIs
- **External Adapters**: Tata Power, Statiq, Ather (mocked)

#### REST API Endpoints
```
POST   /api/v1/charging/stations
GET    /api/v1/charging/stations
GET    /api/v1/charging/stations/nearby?lat={lat}&lon={lon}&radius={radius}
POST   /api/v1/charging/sessions/start
POST   /api/v1/charging/sessions/{id}/end
GET    /api/v1/charging/sessions/{id}
GET    /api/v1/charging/sessions?vehicleId={id}
```

#### Database Schema
- `charging_stations`: Station details with geospatial indexing
- `charging_sessions`: Session tracking with status
- `charging_networks`: External network integrations
- `route_optimizations`: Route planning with charging stops
- `session_reservations`: Slot reservations with TTL

---

### 2. MAINTENANCE SERVICE (Port 8084)
**Database**: `maintenance_db` + `event_store`
**Pattern**: Event Sourcing

#### Domain Layer
**Aggregates**:
- `MaintenanceScheduleAggregate`
- `BatteryHealthAggregate`: Event-sourced aggregate

**Value Objects**:
- `BatteryHealth`: SOH, SOC, temperature
- `ServiceInterval`: Days and kilometers
- `Cost`: Service cost representation

**Domain Events (Event Sourced)**:
- `MaintenanceScheduled`
- `MaintenanceCompleted`
- `BatteryHealthDegraded`
- `WarrantyExpiringSoon`
- `ServiceDue`

#### Event Sourcing Implementation
**Event Store Table**:
```sql
CREATE TABLE event_store (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255),
    aggregate_type VARCHAR(100),
    event_type VARCHAR(100),
    event_data JSONB,
    version INTEGER,
    timestamp TIMESTAMP,
    user_id VARCHAR(255),
    UNIQUE (aggregate_id, version)
);
```

**EventStore Service**:
- Append events
- Load events by aggregate
- Rebuild aggregate from event stream
- Snapshot creation for performance

#### Application Layer
**Commands**:
- `ScheduleMaintenanceCommand`
- `CompleteMaintenanceCommand`
- `UpdateBatteryHealthCommand`

**Queries**:
- `GetMaintenanceScheduleQuery`
- `GetServiceHistoryQuery`
- `GetBatteryHealthTrendsQuery`

#### Event Consumers
- `trip.completed` → Update vehicle mileage
- `battery.data.updated` → Track battery degradation

#### Database Schema
- `event_store`: Event sourcing store
- `maintenance_schedules`: Scheduled maintenance
- `service_history`: Completed services
- `battery_health`: Battery metrics (time-series)
- `warranties`: Warranty tracking
- `aggregate_snapshots`: Performance optimization

---

### 3. DRIVER SERVICE (Port 8085)
**Database**: `driver_db` + `driver_read_model`
**Pattern**: CQRS

#### Domain Layer
**Aggregates**:
- `DriverAggregate`: Root aggregate managing driver lifecycle

**Value Objects**:
- `LicenseNumber`: Validated license
- `PhoneNumber`: Formatted phone
- `PerformanceScore`: 0-100 score with rating

**Domain Events**:
- `DriverRegistered`
- `DriverAssigned`
- `DriverBehaviorAnalyzed`
- `DriverPerformanceUpdated`

#### CQRS Implementation
**Write Model** (Normalized):
- `drivers`: Master driver data
- `driver_behavior`: Behavior incidents
- `driver_assignments`: Vehicle assignments
- `driver_attendance`: Check-in/out tracking

**Read Model** (Denormalized for queries):
```sql
CREATE TABLE driver_read_model (
    driver_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    rating DECIMAL,
    total_trips INTEGER,
    average_score DECIMAL,
    total_distance DECIMAL,
    total_hours DECIMAL,
    current_vehicle_id VARCHAR(255),
    last_trip_date TIMESTAMP,
    performance_rank INTEGER,
    updated_at TIMESTAMP
);
```

**Synchronization**: Trigger-based from write model to read model

#### Application Layer
**Commands (Write)**:
- `RegisterDriverCommand`
- `AssignDriverCommand`
- `RecordAttendanceCommand`

**Queries (Read - from read model)**:
- `GetDriverPerformanceQuery`
- `GetLeaderboardQuery`
- `GetDriverAnalyticsQuery`

#### Database Features
- Automatic read model synchronization via triggers
- Performance ranking calculation
- Leaderboard materialization

---

### 4. ANALYTICS SERVICE (Port 8086)
**Database**: `analytics_db` (PostgreSQL + TimescaleDB)
**Pattern**: CQRS & Read-Optimized DB

#### Domain Layer
**Aggregates**:
- `AnalyticsReportAggregate`

**Value Objects**:
- `ReportPeriod`: Date range
- `CostBreakdown`: Cost components
- `UtilizationMetrics`: Usage statistics

#### Time-Series Data (TimescaleDB)
**Hypertables**:
- `cost_analytics`: Time-series cost data
- `energy_consumption_trends`: Energy usage patterns

**Materialized Views**:
```sql
-- Fleet Summary View
CREATE MATERIALIZED VIEW fleet_summary AS
SELECT
    company_id,
    COUNT(DISTINCT vehicle_id) as total_vehicles,
    AVG(utilization_percentage) as avg_utilization,
    SUM(total_cost) as total_cost,
    SUM(distance_traveled) as total_distance
FROM cost_analytics
GROUP BY company_id;

-- TCO Analysis View
CREATE MATERIALIZED VIEW tco_analysis AS
SELECT
    vehicle_id,
    SUM(energy_cost) as total_energy_cost,
    SUM(maintenance_cost) as total_maintenance_cost,
    SUM(total_cost) as total_ownership_cost,
    AVG(cost_per_km) as avg_cost_per_km
FROM cost_analytics
WHERE period_start >= CURRENT_DATE - INTERVAL '365 days'
GROUP BY vehicle_id;
```

#### Event Consumers
- `trip.completed` → Update utilization and distance
- `charging.session.completed` → Update energy costs
- `maintenance.completed` → Update maintenance costs

#### Application Layer
**Queries (Read-Optimized)**:
- `GetFleetSummaryQuery`
- `GetTCOAnalysisQuery`
- `GetCarbonFootprintQuery`
- `GetUtilizationTrendsQuery`

#### REST API Endpoints
```
GET /api/v1/analytics/fleet-summary?companyId={id}
GET /api/v1/analytics/tco?vehicleId={id}&period={period}
GET /api/v1/analytics/carbon-footprint?companyId={id}
GET /api/v1/analytics/utilization?vehicleId={id}
POST /api/v1/analytics/custom-report
```

#### Database Schema
- `cost_analytics`: TCO analysis
- `utilization_reports`: Vehicle utilization
- `carbon_footprint`: CO2 savings tracking
- `custom_reports`: User-defined reports
- `energy_consumption_trends`: Energy patterns
- `performance_metrics`: KPIs and metrics

---

### 5. NOTIFICATION SERVICE (Port 8087)
**Database**: `notification_db`
**Pattern**: Event-Driven Architecture

#### Domain Layer
**Aggregates**:
- `NotificationAggregate`
- `AlertRuleAggregate`

**Value Objects**:
- `NotificationChannel`: EMAIL, SMS, IN_APP, PUSH
- `NotificationPriority`: LOW, MEDIUM, HIGH, CRITICAL

#### Event Consumers (Subscribe to ALL events)
- `battery.low` → Send battery alert
- `maintenance.due` → Send maintenance reminder
- `driver.behavior.alert` → Send warning notification
- `charging.session.completed` → Send charging receipt
- `trip.completed` → Send trip summary

#### Alert Rules Engine
```java
public interface AlertRule {
    boolean evaluate(Event event);
    List<NotificationAction> getActions();
}
```

**Sample Rules**:
- Battery < 20% → Email + Push
- Maintenance overdue → Email + SMS
- Harsh driving > 5 incidents → Email to manager

#### External Adapters (Mocked)
- `EmailAdapter`: SendGrid/AWS SES integration
- `SMSAdapter`: Twilio integration
- `PushNotificationAdapter`: Firebase Cloud Messaging

#### Database Schema
- `notifications`: Notification records
- `alert_rules`: Configurable alert rules
- `notification_templates`: Message templates
- `notification_log`: Delivery logs
- `user_notification_preferences`: User settings
- `notification_queue`: Batch processing queue

---

### 6. BILLING SERVICE (Port 8088)
**Database**: `billing_db` + `event_store`
**Pattern**: Saga & Event Sourcing

#### Domain Layer
**Aggregates**:
- `SubscriptionAggregate`: Event-sourced subscription lifecycle
- `InvoiceAggregate`: Invoice management

**Value Objects**:
- `Money`: Amount with currency
- `BillingCycle`: MONTHLY, QUARTERLY, ANNUAL

**Domain Events (Event Sourced)**:
- `SubscriptionCreated`
- `SubscriptionUpgraded`
- `SubscriptionDowngraded`
- `InvoiceGenerated`
- `PaymentReceived`
- `PaymentFailed`

#### Subscription Management Saga
**Steps**:
1. Create subscription
2. Generate first invoice
3. Process payment
4. Activate subscription

**Compensation** (on failure):
- Refund payment
- Deactivate subscription
- Rollback credits

#### External Adapters
- `RazorpayAdapter`: Payment gateway (mocked)
- `InvoiceGeneratorAdapter`: PDF generation (mocked)

#### Database Schema
- `event_store`: Event sourcing
- `subscriptions`: Active subscriptions
- `invoices`: Invoice records
- `payments`: Payment transactions
- `pricing_plans`: Available plans
- `payment_methods`: Saved payment methods
- `usage_records`: Metered billing
- `credits`: Prepaid balance
- `credit_transactions`: Credit usage log

---

## Common Infrastructure Components

### 1. RabbitMQ Configuration
**Exchanges**:
- Topic exchanges for all services
- Dead Letter Exchange (DLX) for failed messages

**Queues**:
- Service-specific queues with DLQ
- Message TTL: 24 hours
- Auto-retry with exponential backoff (3 attempts)

**Message Flow**:
```
Producer → Exchange → Queue → Consumer
              ↓ (failure)
            DLX → DLQ
```

### 2. Resilience4j Patterns
**Circuit Breaker**:
- Sliding window: 10 requests
- Failure threshold: 50%
- Wait in open state: 10 seconds
- Half-open attempts: 3

**Retry**:
- Max attempts: 3
- Wait duration: 1 second
- Exponential backoff: 2x multiplier

**Bulkhead**:
- Max concurrent calls: 10
- Max wait duration: 1 second

**Rate Limiter**:
- Requests per period: 10
- Period: 1 second

### 3. OpenTelemetry Tracing
- Distributed tracing across all services
- Jaeger propagation
- OTLP exporter (port 4317)
- Automatic instrumentation for:
  - HTTP requests
  - Database queries
  - Message publishing/consuming

### 4. Prometheus Metrics
**Exposed Metrics**:
- HTTP request duration
- Database query performance
- Circuit breaker state
- Message queue depth
- Business metrics (custom)

**Actuator Endpoints**:
```
/actuator/health
/actuator/metrics
/actuator/prometheus
/actuator/info
```

### 5. Database Migrations (Flyway)
- Version-controlled schema changes
- Baseline on migrate
- Validate on startup
- Location: `db/migration/`

---

## Service Communication Patterns

### Synchronous Communication
- **REST APIs**: Inter-service REST calls
- **Feign Clients**: Declarative REST clients
- **Circuit Breakers**: Protect from cascading failures

### Asynchronous Communication
- **Event-Driven**: RabbitMQ message broker
- **Event Sourcing**: Audit trail and replay
- **CQRS**: Eventually consistent read models

### Data Patterns
- **Database per Service**: Each service owns its data
- **Saga Pattern**: Distributed transactions
- **Event Sourcing**: Immutable event log
- **CQRS**: Separate read and write models

---

## Deployment Architecture

### Docker Containers
Each service is containerized with:
- Multi-stage builds (Maven + JRE)
- Non-root user execution
- Health checks
- Resource limits

### Health Checks
**Liveness Probe**: `/actuator/health/liveness`
**Readiness Probe**: `/actuator/health/readiness`

### Service Discovery
- **Eureka Server**: Service registry (port 8761)
- **Auto-registration**: All services register on startup
- **Heartbeat**: 5-second intervals
- **Lease expiration**: 10 seconds

---

## API Documentation

### OpenAPI/Swagger
Each service exposes:
- **Swagger UI**: `/swagger-ui.html`
- **API Docs**: `/v3/api-docs`
- **Interactive Testing**: Swagger UI interface

---

## Security Considerations

1. **Authentication**: JWT tokens (handled by Auth Service)
2. **Authorization**: Role-based access control
3. **API Gateway**: Single entry point
4. **Rate Limiting**: Prevent abuse
5. **Input Validation**: Jakarta Validation
6. **SQL Injection Prevention**: Parameterized queries
7. **Secrets Management**: Environment variables

---

## Monitoring & Observability

### Logs
- **Format**: JSON structured logs
- **Level**: DEBUG (development), INFO (production)
- **Retention**: 30 days
- **Aggregation**: ELK Stack / Grafana Loki

### Traces
- **OpenTelemetry**: Distributed tracing
- **Jaeger**: Trace visualization
- **Sampling**: 100% (development), 10% (production)

### Metrics
- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **Alerting**: Alert Manager

---

## Performance Optimizations

1. **Redis Caching**: Hot data (stations, drivers)
2. **Database Indexing**: Strategic indexes on query columns
3. **Connection Pooling**: HikariCP (20 max connections)
4. **Batch Processing**: JPA batch inserts (size: 20)
5. **Materialized Views**: Pre-aggregated analytics
6. **TimescaleDB**: Time-series optimization
7. **Event Snapshots**: Reduce event replay time

---

## Error Handling

### Global Exception Handler
All services implement:
- `MethodArgumentNotValidException` → 400 Bad Request
- `IllegalArgumentException` → 400 Bad Request
- `RuntimeException` → 500 Internal Server Error
- Custom domain exceptions → Appropriate status codes

### Error Response Format
```json
{
  "timestamp": "2025-10-25T10:30:00",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "vehicleId": "Vehicle ID is required",
    "stationId": "Station ID is required"
  }
}
```

---

## Testing Strategy

1. **Unit Tests**: Domain logic, value objects
2. **Integration Tests**: Repository, message consumers
3. **API Tests**: REST endpoint testing
4. **Contract Tests**: Service interface contracts
5. **End-to-End Tests**: Full user workflows

---

## Development Guidelines

### Code Structure
```
service-name/
├── domain/                    # Pure business logic (no Spring)
│   ├── model/
│   │   ├── aggregate/        # DDD Aggregates
│   │   ├── entity/           # Entities
│   │   ├── valueobject/      # Value Objects
│   │   └── event/            # Domain Events
│   ├── repository/           # Repository Interfaces
│   └── service/              # Domain Services
├── application/               # Use Cases
│   ├── command/              # CQRS Commands
│   ├── query/                # CQRS Queries
│   ├── dto/                  # Data Transfer Objects
│   ├── service/              # Application Services
│   ├── handler/              # Command/Query Handlers
│   └── mapper/               # DTO Mappers (MapStruct)
├── infrastructure/            # Technical Implementation
│   ├── persistence/          # JPA Repositories
│   ├── messaging/            # RabbitMQ
│   │   ├── publisher/        # Event Publishers
│   │   └── consumer/         # Event Consumers
│   ├── config/               # Spring Configuration
│   └── adapter/              # External API Adapters
└── presentation/              # API Layer
    ├── rest/                 # REST Controllers
    └── exception/            # Exception Handlers
```

### Naming Conventions
- **Aggregates**: `*Aggregate` (e.g., `ChargingSessionAggregate`)
- **Commands**: `*Command` (e.g., `StartChargingSessionCommand`)
- **Queries**: `*Query` (e.g., `GetAvailableStationsQuery`)
- **Events**: Past tense (e.g., `ChargingSessionStarted`)
- **Services**: `*Service` (e.g., `ChargingSessionSaga`)

---

## Future Enhancements

1. **Kafka Integration**: Replace RabbitMQ for higher throughput
2. **GraphQL API**: Flexible querying
3. **gRPC**: High-performance inter-service communication
4. **Service Mesh**: Istio for advanced traffic management
5. **Kubernetes**: Container orchestration
6. **Event Replay**: Full event sourcing replay capabilities
7. **Multi-tenancy**: Enhanced isolation
8. **Real-time Analytics**: Apache Flink/Kafka Streams

---

## Conclusion

This architecture provides a robust, scalable, and maintainable foundation for the EV Fleet Management Platform, implementing industry-standard patterns and best practices for enterprise microservices.

---

**Generated on**: 2025-10-25
**Version**: 1.0.0
**Architecture Team**: EV Fleet Management Platform
