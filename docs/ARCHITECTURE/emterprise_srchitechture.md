# Enterprise Microservices Architecture - EV Fleet Management Platform
## World-Class Architecture Following Industry Best Practices 2025

**Version**: 2.0 (Enterprise Edition)
**Last Updated**: October 25, 2025
**Architecture Style**: Event-Driven Microservices with DDD, CQRS, and Saga Patterns

---

## ðŸ—ï¸ Architecture Philosophy

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

## ðŸŽ¯ Strategic Architecture Patterns

### 1. Domain-Driven Design (DDD) Implementation

Each microservice is organized around a **Bounded Context** with:

#### **Layered Architecture** (per service):
```
â”œâ”€â”€ domain/                      # Core business logic (framework-independent)
â”‚   â”œâ”€â”€ model/                   # Entities, Value Objects, Aggregates
â”‚   â”œâ”€â”€ service/                 # Domain Services
â”‚   â”œâ”€â”€ repository/              # Repository interfaces (ports)
â”‚   â””â”€â”€ event/                   # Domain Events
â”œâ”€â”€ application/                 # Use cases and orchestration
â”‚   â”œâ”€â”€ command/                 # Commands (CQRS write)
â”‚   â”œâ”€â”€ query/                   # Queries (CQRS read)
â”‚   â”œâ”€â”€ dto/                     # Data Transfer Objects
â”‚   â””â”€â”€ service/                 # Application Services
â”œâ”€â”€ infrastructure/              # Technical implementations (adapters)
â”‚   â”œâ”€â”€ persistence/             # JPA repositories, database
â”‚   â”œâ”€â”€ messaging/               # RabbitMQ, Kafka adapters
â”‚   â”œâ”€â”€ external/                # Third-party integrations
â”‚   â””â”€â”€ config/                  # Spring configurations
â””â”€â”€ presentation/                # API layer
    â”œâ”€â”€ rest/                    # REST controllers
    â”œâ”€â”€ graphql/                 # GraphQL resolvers (optional)
    â””â”€â”€ websocket/               # WebSocket handlers
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
Command: CreateVehicleCommand â†’ VehicleCommandHandler â†’ Vehicle Aggregate â†’ Event Published
Event: VehicleCreatedEvent â†’ EventHandler â†’ Update Read Model (materialized view)
Query: GetVehicleByIdQuery â†’ QueryHandler â†’ Read from optimized read model
```

**Services Using CQRS**:
- âœ… **Analytics Service** - Heavy read operations
- âœ… **Fleet Service** - Real-time queries + complex writes
- âœ… **Driver Service** - Performance dashboards

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
- âœ… **Trip Service** - Full trip history and replay
- âœ… **Maintenance Service** - Audit trail for compliance
- âœ… **Billing Service** - Financial audit requirements

---

### 4. Saga Pattern (Distributed Transactions)

**Problem**: ACID transactions don't work across microservices with separate databases

**Solution**: Saga pattern - sequence of local transactions with compensating transactions

#### **Choreography-based Saga** (Event-driven):
```
Trip Completion Flow:
1. Fleet Service: Trip Ends â†’ TripCompletedEvent
2. Driver Service: Updates driver stats â†’ DriverStatsUpdatedEvent
3. Analytics Service: Calculates metrics â†’ MetricsCalculatedEvent
4. Billing Service: Generates charges â†’ ChargesGeneratedEvent

Compensation:
If Billing fails â†’ BillingFailedEvent â†’ Rollback driver stats, analytics
```

#### **Orchestration-based Saga** (Centralized):
```
Charging Session Saga Orchestrator:
1. Reserve Charging Slot â†’ Success/Failure
2. Start Charging Session â†’ Success/Failure
3. Deduct Credits â†’ Success/Failure
4. Complete Session â†’ Success/Failure

If any step fails â†’ Execute compensating transactions in reverse
```

#### **Implementation Libraries**:
- **Axon Framework** - CQRS + Event Sourcing + Saga
- **Spring State Machine** - Saga orchestration
- **Eventuate Tram** - Messaging-based sagas

**Key Sagas in Platform**:
- âœ… **Trip Completion Saga**
- âœ… **Charging Session Saga**
- âœ… **Vehicle Onboarding Saga**
- âœ… **Subscription Management Saga**

---

### 5. Hexagonal Architecture (Ports & Adapters)

**Goal**: Isolate core business logic from external dependencies

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                   Presentation Layer                 â”‚
â”‚         (REST API, GraphQL, WebSocket)              â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                       â”‚
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚              Application Layer (Use Cases)           â”‚
â”‚        Commands, Queries, DTOs, Validators          â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                       â”‚
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                  Domain Layer                        â”‚
â”‚    Entities, Aggregates, Value Objects, Events      â”‚
â”‚           (NO framework dependencies)                â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                       â”‚
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚           Infrastructure Layer (Adapters)            â”‚
â”‚   JPA, RabbitMQ, Redis, External APIs, Config       â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

**Benefits**:
- Testable core logic (no database/framework needed)
- Easy to swap adapters (change database, messaging)
- Clear separation of concerns

---

## ðŸ”„ Event-Driven Architecture

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
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                   Event Backbone                         â”‚
â”‚              (RabbitMQ + Kafka Hybrid)                   â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
         â–²                    â”‚                    â–²
         â”‚                    â”‚                    â”‚
         â”‚ Publish            â”‚ Subscribe          â”‚
         â”‚                    â–¼                    â”‚
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚  Fleet Service  â”‚   â”‚   Analytics  â”‚   â”‚ Notification   â”‚
â”‚   (Producer)    â”‚   â”‚   (Consumer) â”‚   â”‚   (Consumer)   â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

### RabbitMQ + Kafka Strategy

**RabbitMQ**: Real-time events (low latency)
- Vehicle location updates
- Alert notifications
- Command/Response patterns

**Kafka**: High-throughput, persistent events
- Telemetry data streams
- Event sourcing storage
- Analytics data pipelines

---

## ðŸ›¡ï¸ Resilience & Fault Tolerance

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
| RabbitMQ Publish | 3s | 5 | Exponential |
| Redis Cache | 1s | 2 | Immediate |

---

## ðŸ“Š Observability (3 Pillars)

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
Application â†’ Logback â†’ Logstash â†’ Elasticsearch â†’ Kibana
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

## ðŸ” Security Architecture

### Authentication & Authorization

#### 1. **OAuth 2.0 + OpenID Connect**
```
User â†’ Firebase Auth â†’ JWT Token â†’ API Gateway â†’ Validate Token â†’ Microservice
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

âœ… **Secrets Management**: HashiCorp Vault / AWS Secrets Manager
âœ… **Data Encryption**: At-rest (AES-256) and in-transit (TLS 1.3)
âœ… **Input Validation**: Bean Validation (JSR-303)
âœ… **SQL Injection Protection**: Parameterized queries (JPA)
âœ… **Rate Limiting**: API Gateway + Redis
âœ… **CORS**: Configured per environment
âœ… **OWASP Top 10**: Regular security audits

---

## ðŸ“¡ API Design Standards

### RESTful API Best Practices

#### **Versioning Strategy**:
```
URI Versioning: /api/v1/vehicles, /api/v2/vehicles
Header Versioning: Accept: application/vnd.evfleet.v1+json
```

#### **Resource Naming**:
```
âœ… Correct:
GET    /api/v1/vehicles
GET    /api/v1/vehicles/{id}
POST   /api/v1/vehicles
PUT    /api/v1/vehicles/{id}
PATCH  /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}

âŒ Incorrect:
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

## ðŸ—„ï¸ Data Management Strategies

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

## ðŸš€ Deployment Architecture

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
Developer â†’ Git Push â†’ GitHub Actions
  â†“
Build (Maven) â†’ Unit Tests â†’ Integration Tests
  â†“
SonarQube (Code Quality) â†’ Security Scan (Snyk)
  â†“
Docker Build â†’ Push to Registry (ECR/GCR/ACR)
  â†“
Deploy to Dev â†’ Automated Tests
  â†“
Deploy to Staging â†’ Manual Approval
  â†“
Deploy to Production (Blue-Green)
```

---

## ðŸ“ˆ Scalability Strategy

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

## ðŸ§ª Testing Strategy

### Test Pyramid

```
         â–²
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

## ðŸ“‹ Service Registry

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

## ðŸŽ¯ Performance Targets (SLA)

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Response Time (p95) | < 200ms | Prometheus |
| API Response Time (p99) | < 500ms | Prometheus |
| Database Query Time | < 50ms | Slow query log |
| Event Processing Latency | < 100ms | RabbitMQ metrics |
| Service Availability | 99.9% | Uptime monitoring |
| Error Rate | < 0.1% | Error logs |
| Concurrent Users | 10,000+ | Load testing |
| Requests Per Second | 5,000+ | Load testing |

---

## ðŸ”® Future Enhancements

### Phase 2 (6-12 months)
- âœ… GraphQL Federation for unified API
- âœ… Service Mesh (Istio) for advanced traffic management
- âœ… Multi-region deployment for HA
- âœ… Machine Learning pipeline integration
- âœ… Mobile apps (React Native)

### Phase 3 (12-24 months)
- âœ… Edge computing for vehicle processing
- âœ… Blockchain for charging settlements
- âœ… AI-powered predictive analytics
- âœ… Advanced fraud detection
- âœ… Global expansion (multi-tenancy enhancements)

---

## ðŸ“š References & Resources

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