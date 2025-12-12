# Enterprise Microservices Architecture - EV Fleet Management Platform
## World-Class Architecture Following Industry Best Practices 2025

**Version**: 2.0 (Enterprise Edition)
**Last Updated**: October 25, 2025
**Architecture Style**: Event-Driven Microservices with DDD, CQRS, and Saga Patterns

---

## Ã°Å¸Ââ€”Ã¯Â¸Â Architecture Philosophy

This architecture follows **12-Factor App** principles and implements:

1. **Domain-Driven Design (DDD)** - Business-centric service boundaries
2. **CQRS** (Command Query Responsibility Segregation) - Separate read/write models
3. **Event Sourcing** - Audit trail and temporal queries
4. **Saga Pattern** - Distributed transaction management
5. **Hexagonal Architecture** - Ports & Adapters for testability
6. **Circuit Breaker** - Fault tolerance and resilience
7. **API Gateway** - Single entry point with cross-cutting concerns
8. **Service Mesh** - Advanced networking, security, observability
9. **Database per Service** - Data autonomy
10. **Observability** - Distributed tracing, metrics, logging

---

## Ã°Å¸Å½Â¯ Strategic Architecture Patterns

### 1. Domain-Driven Design (DDD) Implementation

Each microservice is organized around a **Bounded Context** with:

#### **Layered Architecture** (per service):
```
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ domain/                      # Core business logic (framework-independent)
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ model/                   # Entities, Value Objects, Aggregates
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ service/                 # Domain Services
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ repository/              # Repository interfaces (ports)
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ event/                   # Domain Events
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ application/                 # Use cases and orchestration
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ command/                 # Commands (CQRS write)
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ query/                   # Queries (CQRS read)
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ dto/                     # Data Transfer Objects
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ service/                 # Application Services
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ infrastructure/              # Technical implementations (adapters)
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ persistence/             # JPA repositories, database
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ messaging/               # Spring Modulith event adapters
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ external/                # Third-party integrations
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ config/                  # Spring configurations
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ presentation/                # API layer
    Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ rest/                    # REST controllers
    Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ graphql/                 # GraphQL resolvers (optional)
    Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ websocket/               # WebSocket handlers
```

#### **Core DDD Concepts**:

**Aggregates**: Cluster of domain objects treated as a single unit
- Vehicle Aggregate (root: Vehicle, children: TelemetryData, Trips)
- Driver Aggregate (root: Driver, children: Behavior, Assignments)
- Charging Session Aggregate

**Value Objects**: Immutable objects defined by attributes
- Location (latitude, longitude)
- Money (amount, currency)
- BatteryStatus (soc, soh, temperature)

**Domain Events**: Facts that happened in the domain
- VehicleLocationUpdated
- TripCompleted
- BatteryLowDetected
- MaintenanceDue

**Repositories**: Abstract data access
- Interface in domain layer
- Implementation in infrastructure layer

---

### 2. CQRS (Command Query Responsibility Segregation)

**Why**: Optimize read and write operations independently

#### **Command Side (Write Model)**:
- Handles state changes
- Enforces business rules
- Publishes domain events
- Uses normalized relational database (PostgreSQL)

#### **Query Side (Read Model)**:
- Optimized for queries
- Denormalized views
- Eventually consistent
- Can use different database (Redis, Elasticsearch for complex queries)

#### **Implementation**:
```
Command: CreateVehicleCommand Ã¢â€ â€™ VehicleCommandHandler Ã¢â€ â€™ Vehicle Aggregate Ã¢â€ â€™ Event Published
Event: VehicleCreatedEvent Ã¢â€ â€™ EventHandler Ã¢â€ â€™ Update Read Model (materialized view)
Query: GetVehicleByIdQuery Ã¢â€ â€™ QueryHandler Ã¢â€ â€™ Read from optimized read model
```

**Services Using CQRS**:
- Ã¢Å“â€¦ **Analytics Service** - Heavy read operations
- Ã¢Å“â€¦ **Fleet Service** - Real-time queries + complex writes
- Ã¢Å“â€¦ **Driver Service** - Performance dashboards

---

### 3. Event Sourcing

**What**: Store all changes as a sequence of events instead of just current state

#### **Benefits**:
- Complete audit trail
- Temporal queries (state at any point in time)
- Event replay for debugging
- Easy to implement new read models

#### **Event Store Schema**:
```sql
CREATE TABLE event_store (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB,
    version INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    user_id VARCHAR(255),
    INDEX idx_aggregate (aggregate_id, version)
);
```

#### **Services Using Event Sourcing**:
- Ã¢Å“â€¦ **Trip Service** - Full trip history and replay
- Ã¢Å“â€¦ **Maintenance Service** - Audit trail for compliance
- Ã¢Å“â€¦ **Billing Service** - Financial audit requirements

---

### 4. Saga Pattern (Distributed Transactions)

**Problem**: ACID transactions don't work across microservices with separate databases

**Solution**: Saga pattern - sequence of local transactions with compensating transactions

#### **Choreography-based Saga** (Event-driven):
```
Trip Completion Flow:
1. Fleet Service: Trip Ends Ã¢â€ â€™ TripCompletedEvent
2. Driver Service: Updates driver stats Ã¢â€ â€™ DriverStatsUpdatedEvent
3. Analytics Service: Calculates metrics Ã¢â€ â€™ MetricsCalculatedEvent
4. Billing Service: Generates charges Ã¢â€ â€™ ChargesGeneratedEvent

Compensation:
If Billing fails Ã¢â€ â€™ BillingFailedEvent Ã¢â€ â€™ Rollback driver stats, analytics
```

#### **Orchestration-based Saga** (Centralized):
```
Charging Session Saga Orchestrator:
1. Reserve Charging Slot Ã¢â€ â€™ Success/Failure
2. Start Charging Session Ã¢â€ â€™ Success/Failure
3. Deduct Credits Ã¢â€ â€™ Success/Failure
4. Complete Session Ã¢â€ â€™ Success/Failure

If any step fails Ã¢â€ â€™ Execute compensating transactions in reverse
```

#### **Implementation Libraries**:
- **Axon Framework** - CQRS + Event Sourcing + Saga
- **Spring State Machine** - Saga orchestration
- **Eventuate Tram** - Messaging-based sagas

**Key Sagas in Platform**:
- Ã¢Å“â€¦ **Trip Completion Saga**
- Ã¢Å“â€¦ **Charging Session Saga**
- Ã¢Å“â€¦ **Vehicle Onboarding Saga**
- Ã¢Å“â€¦ **Subscription Management Saga**

---

### 5. Hexagonal Architecture (Ports & Adapters)

**Goal**: Isolate core business logic from external dependencies

```
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š                   Presentation Layer                 Ã¢â€â€š
Ã¢â€â€š         (REST API, GraphQL, WebSocket)              Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
                       Ã¢â€â€š
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€“Â¼Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š              Application Layer (Use Cases)           Ã¢â€â€š
Ã¢â€â€š        Commands, Queries, DTOs, Validators          Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
                       Ã¢â€â€š
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€“Â¼Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š                  Domain Layer                        Ã¢â€â€š
Ã¢â€â€š    Entities, Aggregates, Value Objects, Events      Ã¢â€â€š
Ã¢â€â€š           (NO framework dependencies)                Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
                       Ã¢â€â€š
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€“Â¼Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š           Infrastructure Layer (Adapters)            Ã¢â€â€š
Ã¢â€â€š   JPA, Spring Events, Redis, External APIs, Config       Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
```

**Benefits**:
- Testable core logic (no database/framework needed)
- Easy to swap adapters (change database, messaging)
- Clear separation of concerns

---

## Ã°Å¸â€â€ž Event-Driven Architecture

### Event Types

#### 1. **Domain Events** (Business facts)
```json
{
  "eventType": "VehicleLocationUpdated",
  "aggregateId": "vehicle-123",
  "timestamp": "2025-10-25T10:30:00Z",
  "data": {
    "vehicleId": "vehicle-123",
    "location": {"lat": 28.7041, "lon": 77.1025},
    "batterySoc": 65.5,
    "speed": 45
  },
  "metadata": {
    "version": 1,
    "userId": "driver-456",
    "correlationId": "trace-789"
  }
}
```

#### 2. **Integration Events** (Cross-service communication)
```json
{
  "eventType": "TripCompleted",
  "eventId": "evt-uuid-123",
  "timestamp": "2025-10-25T11:00:00Z",
  "source": "fleet-service",
  "destination": ["analytics-service", "billing-service", "driver-service"],
  "data": {
    "tripId": "trip-456",
    "vehicleId": "vehicle-123",
    "driverId": "driver-789",
    "distance": 25.5,
    "duration": 3600,
    "energyConsumed": 8.5
  }
}
```

### Event Flow Architecture

```
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š                   Event Backbone                         Ã¢â€â€š
Ã¢â€â€š              (Spring Events + Kafka Hybrid)                   Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
         Ã¢â€“Â²                    Ã¢â€â€š                    Ã¢â€“Â²
         Ã¢â€â€š                    Ã¢â€â€š                    Ã¢â€â€š
         Ã¢â€â€š Publish            Ã¢â€â€š Subscribe          Ã¢â€â€š
         Ã¢â€â€š                    Ã¢â€“Â¼                    Ã¢â€â€š
Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â´Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â   Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â   Ã¢â€Å’Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â´Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Â
Ã¢â€â€š  Fleet Service  Ã¢â€â€š   Ã¢â€â€š   Analytics  Ã¢â€â€š   Ã¢â€â€š Notification   Ã¢â€â€š
Ã¢â€â€š   (Producer)    Ã¢â€â€š   Ã¢â€â€š   (Consumer) Ã¢â€â€š   Ã¢â€â€š   (Consumer)   Ã¢â€â€š
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€Ëœ
```

### Spring Events + Kafka Strategy

**Spring Events**: Real-time events (low latency)
- Vehicle location updates
- Alert notifications
- Command/Response patterns

**Kafka**: High-throughput, persistent events
- Telemetry data streams
- Event sourcing storage
- Analytics data pipelines

---

## Ã°Å¸â€ºÂ¡Ã¯Â¸Â Resilience & Fault Tolerance

### Circuit Breaker Pattern (Resilience4j)

```java
@CircuitBreaker(name = "chargingService", fallbackMethod = "fallbackGetStations")
@Retry(name = "chargingService", fallbackMethod = "fallbackGetStations")
@RateLimiter(name = "chargingService")
@Bulkhead(name = "chargingService")
public List<ChargingStation> getChargingStations() {
    return chargingClient.getStations();
}

private List<ChargingStation> fallbackGetStations(Exception e) {
    return getCachedStations(); // Fallback to Redis cache
}
```

**Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      chargingService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true

  retry:
    instances:
      chargingService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2

  bulkhead:
    instances:
      chargingService:
        maxConcurrentCalls: 10
        maxWaitDuration: 500ms
```

### Timeout & Retry Strategies

| Operation Type | Timeout | Retry | Strategy |
|----------------|---------|-------|----------|
| Database Query | 5s | 3 | Exponential backoff |
| External API | 10s | 2 | Linear |
| Event Publish | 3s | 5 | Exponential |
| Redis Cache | 1s | 2 | Immediate |

---

## Ã°Å¸â€œÅ  Observability (3 Pillars)

### 1. Distributed Tracing (OpenTelemetry + Zipkin)

```java
@Configuration
public class TracingConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(
                        ZipkinSpanExporter.builder()
                            .setEndpoint("http://zipkin:9411/api/v2/spans")
                            .build()
                    ).build())
                    .build()
            )
            .buildAndRegisterGlobal();
    }
}
```

**Trace Context Propagation**:
- W3C Trace Context headers
- Correlation IDs across all services
- Parent-child span relationships

### 2. Metrics (Prometheus + Grafana)

**Custom Business Metrics**:
```java
@Component
public class FleetMetrics {

    private final Counter vehicleLocationUpdates;
    private final Gauge activeVehicles;
    private final Timer tripDuration;
    private final Histogram batteryLevels;

    public FleetMetrics(MeterRegistry registry) {
        this.vehicleLocationUpdates = Counter.builder("fleet.location.updates")
            .description("Total vehicle location updates")
            .tag("service", "fleet")
            .register(registry);

        this.activeVehicles = Gauge.builder("fleet.vehicles.active", () -> getActiveCount())
            .description("Number of active vehicles")
            .register(registry);
    }
}
```

**Standard Metrics**:
- JVM metrics (heap, GC, threads)
- HTTP metrics (requests, latency, errors)
- Database connection pool
- Message queue metrics
- Custom business KPIs

### 3. Centralized Logging (ELK Stack)

```
Application Ã¢â€ â€™ Logback Ã¢â€ â€™ Logstash Ã¢â€ â€™ Elasticsearch Ã¢â€ â€™ Kibana
```

**Structured Logging**:
```java
log.info("Trip completed",
    kv("tripId", tripId),
    kv("vehicleId", vehicleId),
    kv("distance", distance),
    kv("duration", duration),
    kv("correlationId", correlationId)
);
```

**Log Levels**:
- TRACE: Detailed debug information
- DEBUG: Development debugging
- INFO: Business events
- WARN: Potential issues
- ERROR: Errors requiring attention

---

## Ã°Å¸â€Â Security Architecture

### Authentication & Authorization

#### 1. **OAuth 2.0 + OpenID Connect**
```
User Ã¢â€ â€™ Firebase Auth Ã¢â€ â€™ JWT Token Ã¢â€ â€™ API Gateway Ã¢â€ â€™ Validate Token Ã¢â€ â€™ Microservice
```

#### 2. **JWT Structure**:
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-id-123"
  },
  "payload": {
    "sub": "user-123",
    "email": "user@company.com",
    "roles": ["FLEET_MANAGER", "ADMIN"],
    "companyId": "company-456",
    "exp": 1735132800,
    "iat": 1735129200,
    "iss": "https://firebase.google.com/rentvat"
  }
}
```

#### 3. **Service-to-Service Authentication**:
- mTLS (Mutual TLS) between services
- Service mesh (Istio) for automatic encryption
- API keys for external integrations

### Security Best Practices

Ã¢Å“â€¦ **Secrets Management**: HashiCorp Vault / AWS Secrets Manager
Ã¢Å“â€¦ **Data Encryption**: At-rest (AES-256) and in-transit (TLS 1.3)
Ã¢Å“â€¦ **Input Validation**: Bean Validation (JSR-303)
Ã¢Å“â€¦ **SQL Injection Protection**: Parameterized queries (JPA)
Ã¢Å“â€¦ **Rate Limiting**: API Gateway + Redis
Ã¢Å“â€¦ **CORS**: Configured per environment
Ã¢Å“â€¦ **OWASP Top 10**: Regular security audits

---

## Ã°Å¸â€œÂ¡ API Design Standards

### RESTful API Best Practices

#### **Versioning Strategy**:
```
URI Versioning: /api/v1/vehicles, /api/v2/vehicles
Header Versioning: Accept: application/vnd.evfleet.v1+json
```

#### **Resource Naming**:
```
Ã¢Å“â€¦ Correct:
GET    /api/v1/vehicles
GET    /api/v1/vehicles/{id}
POST   /api/v1/vehicles
PUT    /api/v1/vehicles/{id}
PATCH  /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}

Ã¢ÂÅ’ Incorrect:
GET /api/v1/getVehicles
POST /api/v1/createVehicle
```

#### **HTTP Status Codes**:
- 200: OK (successful GET, PUT, PATCH)
- 201: Created (successful POST)
- 204: No Content (successful DELETE)
- 400: Bad Request (validation error)
- 401: Unauthorized (missing/invalid token)
- 403: Forbidden (insufficient permissions)
- 404: Not Found
- 409: Conflict (duplicate resource)
- 429: Too Many Requests (rate limit)
- 500: Internal Server Error
- 503: Service Unavailable

#### **Pagination**:
```
GET /api/v1/vehicles?page=0&size=20&sort=createdAt,desc

Response:
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 156,
    "totalPages": 8
  }
}
```

#### **Filtering & Search**:
```
GET /api/v1/vehicles?companyId=123&status=ACTIVE&batterySoc[gte]=50
GET /api/v1/trips?startDate=2025-01-01&endDate=2025-01-31
GET /api/v1/vehicles?search=MH-12-AB-1234
```

### GraphQL API (Optional for Complex Queries)

```graphql
type Query {
  vehicle(id: ID!): Vehicle
  vehicles(
    companyId: ID!
    status: VehicleStatus
    page: Int
    size: Int
  ): VehiclePage!

  tripAnalytics(
    vehicleId: ID!
    startDate: DateTime!
    endDate: DateTime!
  ): TripAnalytics!
}

type Mutation {
  createVehicle(input: CreateVehicleInput!): Vehicle!
  updateVehicleLocation(input: LocationInput!): Boolean!
}

type Subscription {
  vehicleLocationUpdated(vehicleId: ID!): VehicleLocation!
}
```

---

## Ã°Å¸â€”â€žÃ¯Â¸Â Data Management Strategies

### Database per Service Pattern

| Service | Database | Type | Justification |
|---------|----------|------|---------------|
| Auth | auth_db | PostgreSQL | Relational user data, ACID |
| Fleet | fleet_db | PostgreSQL | Complex queries, relationships |
| Charging | charging_db | PostgreSQL | Transactional sessions |
| Maintenance | maintenance_db | PostgreSQL | Audit trails, compliance |
| Driver | driver_db | PostgreSQL | Relational data |
| Analytics | analytics_db | PostgreSQL + TimescaleDB | Time-series data |
| Notification | notification_db | PostgreSQL | Message queuing |
| Billing | billing_db | PostgreSQL | Financial data, ACID |

### Polyglot Persistence (Future Enhancement)

- **Redis**: Caching, session storage, geospatial queries
- **Elasticsearch**: Full-text search, log analytics
- **MongoDB**: Unstructured data, rapid schema changes
- **TimescaleDB**: Time-series telemetry data
- **InfluxDB**: High-frequency metrics

### Data Consistency Patterns

#### **Strong Consistency** (within service):
- ACID transactions within aggregate boundaries
- Database transactions for critical operations

#### **Eventual Consistency** (across services):
- Event-driven updates
- CQRS read models updated asynchronously
- Saga pattern for distributed transactions

---

## Ã°Å¸Å¡â‚¬ Deployment Architecture

### Container Orchestration (Kubernetes)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fleet-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: fleet-service
        image: evfleet/fleet-service:1.0.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8082
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 5
```

### Service Mesh (Istio)

**Features**:
- Automatic mTLS between services
- Traffic management (canary, blue-green)
- Observability (tracing, metrics)
- Circuit breaking and retries
- Rate limiting

### CI/CD Pipeline

```
Developer Ã¢â€ â€™ Git Push Ã¢â€ â€™ GitHub Actions
  Ã¢â€ â€œ
Build (Maven) Ã¢â€ â€™ Unit Tests Ã¢â€ â€™ Integration Tests
  Ã¢â€ â€œ
SonarQube (Code Quality) Ã¢â€ â€™ Security Scan (Snyk)
  Ã¢â€ â€œ
Docker Build Ã¢â€ â€™ Push to Registry (ECR/GCR/ACR)
  Ã¢â€ â€œ
Deploy to Dev Ã¢â€ â€™ Automated Tests
  Ã¢â€ â€œ
Deploy to Staging Ã¢â€ â€™ Manual Approval
  Ã¢â€ â€œ
Deploy to Production (Blue-Green)
```

---

## Ã°Å¸â€œË† Scalability Strategy

### Horizontal Scaling

| Service | Min Replicas | Max Replicas | Scale Metric |
|---------|--------------|--------------|--------------|
| API Gateway | 2 | 10 | CPU > 70% |
| Auth Service | 2 | 5 | Requests/sec > 100 |
| Fleet Service | 3 | 15 | CPU > 80%, Queue depth |
| Analytics Service | 2 | 8 | CPU > 75% |
| Notification Service | 2 | 10 | Queue messages |

### Caching Strategy

**L1 Cache (Application-level)**: Caffeine
**L2 Cache (Distributed)**: Redis
**L3 Cache (CDN)**: CloudFlare (for static assets)

**Cache Patterns**:
- Cache-Aside (Lazy Loading)
- Write-Through
- Write-Behind
- Refresh-Ahead

### Database Scaling

- **Read Replicas**: For read-heavy services (Analytics)
- **Sharding**: By company_id for multi-tenancy
- **Connection Pooling**: HikariCP (max 20 connections per service)
- **Indexing**: Optimized indexes for frequent queries

---

## Ã°Å¸Â§Âª Testing Strategy

### Test Pyramid

```
         Ã¢â€“Â²
        / \
       /   \        10% - E2E Tests (Selenium, Postman)
      /     \
     /-------\      20% - Integration Tests (TestContainers)
    /         \
   /-----------\    70% - Unit Tests (JUnit, Mockito)
  /             \
 /_______________\
```

### Test Types

**1. Unit Tests** (70%):
```java
@Test
void shouldCalculateTripDistance() {
    Trip trip = new Trip();
    trip.setStartLocation(new Location(28.7041, 77.1025));
    trip.setEndLocation(new Location(28.5355, 77.3910));

    double distance = tripService.calculateDistance(trip);

    assertThat(distance).isCloseTo(30.5, within(0.1));
}
```

**2. Integration Tests** (20%):
```java
@SpringBootTest
@AutoConfigureTestDatabase
@Testcontainers
class VehicleServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void shouldSaveAndRetrieveVehicle() {
        Vehicle vehicle = vehicleService.create(createRequest);
        Vehicle retrieved = vehicleService.findById(vehicle.getId());
        assertThat(retrieved).isEqualTo(vehicle);
    }
}
```

**3. E2E Tests** (10%):
- API contract tests (Pact, Spring Cloud Contract)
- End-to-end user flows
- Performance tests (JMeter, Gatling)

---

## Ã°Å¸â€œâ€¹ Service Registry

### Complete Service Catalog

| Service | Port | Database | Key Patterns | Scalability |
|---------|------|----------|--------------|-------------|
| Eureka Server | 8761 | - | Service Discovery | 3 replicas |
| Config Server | 8888 | - | Centralized Config | 2 replicas |
| API Gateway | 8080 | - | Gateway, Circuit Breaker | 2-10 replicas |
| Auth Service | 8081 | auth_db | RBAC, JWT | 2-5 replicas |
| Fleet Service | 8082 | fleet_db | DDD, Event Sourcing | 3-15 replicas |
| Charging Service | 8083 | charging_db | Saga, CQRS | 2-8 replicas |
| Maintenance Service | 8084 | maintenance_db | Event Sourcing | 2-6 replicas |
| Driver Service | 8085 | driver_db | DDD, CQRS | 2-8 replicas |
| Analytics Service | 8086 | analytics_db | CQRS, Event Sourcing | 2-10 replicas |
| Notification Service | 8087 | notification_db | Event-driven | 2-10 replicas |
| Billing Service | 8088 | billing_db | Saga, Event Sourcing | 2-5 replicas |

---

## Ã°Å¸Å½Â¯ Performance Targets (SLA)

| Metric | Target | Measurement |
|--------|--------|--------------|
| API Response Time (p95) | < 200ms | Prometheus |
| API Response Time (p99) | < 500ms | Prometheus |
| Database Query Time | < 50ms | Slow query log |
| Event Processing Latency | < 100ms | Application metrics |
| Service Availability | 99.9% | Uptime monitoring |
| Error Rate | < 0.1% | Error logs |
| Concurrent Users | 10,000+ | Load testing |
| Requests Per Second | 5,000+ | Load testing |

---

## Ã°Å¸â€Â® Future Enhancements

### Phase 2 (6-12 months)
- Ã¢Å“â€¦ GraphQL Federation for unified API
- Ã¢Å“â€¦ Service Mesh (Istio) for advanced traffic management
- Ã¢Å“â€¦ Multi-region deployment for HA
- Ã¢Å“â€¦ Machine Learning pipeline integration
- Ã¢Å“â€¦ Mobile apps (React Native)

### Phase 3 (12-24 months)
- Ã¢Å“â€¦ Edge computing for vehicle processing
- Ã¢Å“â€¦ Blockchain for charging settlements
- Ã¢Å“â€¦ AI-powered predictive analytics
- Ã¢Å“â€¦ Advanced fraud detection
- Ã¢Å“â€¦ Global expansion (multi-tenancy enhancements)

---

## Ã°Å¸â€œÅ¡ References & Resources

### Books
- "Domain-Driven Design" - Eric Evans
- "Building Microservices" - Sam Newman
- "Microservices Patterns" - Chris Richardson
- "Release It!" - Michael Nygard

### Frameworks
- Spring Boot 3.2+
- Spring Cloud
- Axon Framework (CQRS/Event Sourcing)
- Resilience4j
- OpenTelemetry

### Tools
- Kubernetes
- Istio (Service Mesh)
- Prometheus + Grafana
- ELK Stack
- Zipkin / Jaeger
- SonarQube
- HashiCorp Vault

---

**Architecture Status**: Production-Ready Enterprise Architecture
**Compliance**: SOC 2 Type II Ready, ISO 27001 Ready, GDPR Compliant
**Last Reviewed**: October 25, 2025
**Next Review**: January 2026
