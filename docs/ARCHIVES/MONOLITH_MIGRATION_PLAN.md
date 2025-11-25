# Microservices to Modular Monolith Migration Plan
## SEV EV Fleet Management Platform

**Date:** November 12, 2025
**Strategy:** Cost-Efficient + Architecturally Sound
**Timeline:** 15-20 PRs (~2-3 weeks)

---

## Executive Summary

**Goal:** Consolidate 11 microservices into 1 Modular Monolith while maintaining:
- ✅ Clean domain separation (by package)
- ✅ Same business logic
- ✅ Easy future extraction (back to microservices if needed)
- ✅ 80% cost reduction (₹40K → ₹5K/month)

**Deployment Cost Impact:**
```
Before (Microservices):
├─ 11 separate services × ₹3,500 = ₹38,500/month
├─ 8 PostgreSQL databases        = ₹8,000/month
├─ Redis, RabbitMQ, Load Balancer = ₹10,000/month
└─ Total: ₹56,000-60,000/month

After (Modular Monolith):
├─ 1 powerful server (16 vCPU, 32GB) = ₹3,600/month
├─ PostgreSQL (all databases)        = ₹0 (same server)
├─ Redis, optional RabbitMQ          = ₹0 (same server)
├─ Backups + monitoring              = ₹1,500/month
└─ Total: ₹5,000-7,000/month

Savings: ₹50,000+/month (90% reduction)
```

---

## Architecture Strategy: Modular Monolith (Spring Modulith Pattern)

### What is a Modular Monolith?
A single deployable application with:
- **Modules organized by domain** (auth, fleet, charging, etc.)
- **Each module is loosely coupled** (clean boundaries enforced by Spring Modulith)
- **Single codebase, single deployment**
- **Can extract to microservices later** (when you have ₹10L+ MRR)

### Why This Approach? (Industry Best Practices 2025)

**1. Spring Modulith Framework** (VMware/Spring Official)
- Use `@ApplicationModule` for explicit module boundaries
- Automated module verification with ArchUnit (compile-time checks)
- Event-driven inter-module communication
- Built-in documentation generation

**2. Real-World Validation**
- **Amazon Prime Video (2023)**: Migrated monitoring service from microservices → monolith
  - **Result:** 90% cost reduction, better performance
  - **Reason:** Microservices overhead not justified for their use case
- **Segment (acquired by Twilio)**: Started as monolith, scaled to $100M ARR
- **Shopify**: Uses modular monolith for core platform (2M+ merchants)

**3. Cost Efficiency for Startups**
- Industry research (2025): MVP SaaS costs $30K-60K for basic, $500K+ for microservices
- **Serverless/LAMP stack**: Most cost-effective for early-stage startups
- **Monolith-first, microservices-later**: Validated by Google, Uber, Netflix engineering blogs

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                         │
│                   Port 3000                                 │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              MODULAR MONOLITH (Spring Boot)                 │
│                   Port 8080                                 │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │          API Layer (Controllers)                    │   │
│  │  /api/auth  /api/fleet  /api/charging  /api/driver │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            │                               │
│  ┌─────────────────────────▼───────────────────────────┐   │
│  │            Service Layer (Modules)                  │   │
│  │                                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │   │
│  │  │   Auth   │  │  Fleet   │  │ Charging │         │   │
│  │  │  Module  │  │  Module  │  │  Module  │         │   │
│  │  └──────────┘  └──────────┘  └──────────┘         │   │
│  │                                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │   │
│  │  │  Driver  │  │Analytics │  │ Billing  │         │   │
│  │  │  Module  │  │  Module  │  │  Module  │         │   │
│  │  └──────────┘  └──────────┘  └──────────┘         │   │
│  │                                                     │   │
│  │  ┌──────────┐  ┌──────────┐                        │   │
│  │  │Mainten-  │  │Notifica- │                        │   │
│  │  │ ance     │  │tion      │                        │   │
│  │  │Module    │  │Module    │                        │   │
│  │  └──────────┘  └──────────┘                        │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            │                               │
│  ┌─────────────────────────▼───────────────────────────┐   │
│  │         Data Access Layer (Repositories)            │   │
│  │    (Each module has its own repository package)     │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            │                               │
│  ┌─────────────────────────▼───────────────────────────┐   │
│  │         Cross-Cutting Concerns                      │   │
│  │  • Security (Firebase Auth)                         │   │
│  │  • Caching (Redis)                                  │   │
│  │  • Event Bus (Internal events, optional RabbitMQ)   │   │
│  │  • Logging, Monitoring, Metrics                     │   │
│  └─────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              PostgreSQL (All Databases)                     │
│  evfleet_auth, evfleet_fleet, evfleet_charging, ...        │
└─────────────────────────────────────────────────────────────┘
```

---

## New Project Structure

```
backend/
└── evfleet-monolith/                    (NEW - Single Spring Boot app)
    ├── src/main/java/com/evfleet/
    │   ├── EvFleetApplication.java      (Main entry point)
    │   │
    │   ├── auth/                        (Auth Module - was auth-service)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   ├── dto/
    │   │   └── config/
    │   │
    │   ├── fleet/                       (Fleet Module - was fleet-service)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   ├── dto/
    │   │   └── event/
    │   │
    │   ├── charging/                    (Charging Module - was charging-service)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   └── saga/
    │   │
    │   ├── maintenance/                 (Maintenance Module)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   └── event/
    │   │
    │   ├── driver/                      (Driver Module)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   └── cqrs/
    │   │
    │   ├── analytics/                   (Analytics Module)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   └── model/
    │   │
    │   ├── notification/                (Notification Module)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   └── model/
    │   │
    │   ├── billing/                     (Billing Module)
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   └── saga/
    │   │
    │   ├── common/                      (Shared code)
    │   │   ├── config/                  (Security, Redis, RabbitMQ config)
    │   │   ├── dto/                     (Common DTOs)
    │   │   ├── exception/               (Global exception handling)
    │   │   ├── event/                   (Event bus for inter-module communication)
    │   │   ├── security/                (Security filters, JWT)
    │   │   └── util/                    (Common utilities)
    │   │
    │   └── gateway/                     (API Gateway logic - built-in)
    │       ├── filter/                  (Request/Response filters)
    │       ├── ratelimit/               (Rate limiting)
    │       └── routing/                 (Optional routing logic)
    │
    ├── src/main/resources/
    │   ├── application.yml              (Main config)
    │   ├── application-dev.yml
    │   ├── application-prod.yml
    │   └── db/migration/                (All Flyway migrations)
    │       ├── auth/                    (V1-V10: Auth migrations)
    │       ├── fleet/                   (V11-V30: Fleet migrations)
    │       ├── charging/                (V31-V40: Charging migrations)
    │       ├── maintenance/             (V41-V50: Maintenance migrations)
    │       ├── driver/                  (V51-V60: Driver migrations)
    │       ├── analytics/               (V61-V70: Analytics migrations)
    │       ├── notification/            (V71-V80: Notification migrations)
    │       └── billing/                 (V81-V90: Billing migrations)
    │
    ├── src/test/java/com/evfleet/
    │   ├── auth/                        (Auth tests)
    │   ├── fleet/                       (Fleet tests)
    │   └── ... (same structure)
    │
    ├── pom.xml                          (Single Maven file with all dependencies)
    └── Dockerfile                       (Single Docker image)

frontend/                                (NO CHANGES - stays the same)
└── src/
    └── services/
        └── api.ts                       (Update base URL from 8081-8088 → 8080)
```

---

## Key Architectural Principles

### 1. Module Independence (Spring Modulith Pattern)
```java
// Each module is self-contained with explicit boundaries
// Example: Fleet Module

package com.evfleet.fleet;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;

// ✅ Best Practice: Define explicit module with @ApplicationModule
@ApplicationModule(
    displayName = "Fleet Management",
    allowedDependencies = {"common"} // Only allow common module dependency
)
public class FleetModule {
    // Module metadata (optional but recommended for documentation)
}

// ✅ Good: Module only depends on its own classes
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.service.VehicleService;

// ✅ Good: Can depend on common utilities
import com.evfleet.common.exception.ResourceNotFoundException;

// ❌ Bad: Direct dependency on another module's internals
// import com.evfleet.charging.service.ChargingSessionService; // COMPILE ERROR with Spring Modulith!

// ✅ Good: Use events for cross-module communication
import com.evfleet.common.event.VehicleCreatedEvent;

// ✅ Best Practice: Expose public API via @NamedInterface
@NamedInterface("api")
public interface VehicleServiceApi {
    Vehicle getVehicle(Long id);
    // Only exposed methods - internals hidden
}
```

### 2. Inter-Module Communication
**Option A: Event Bus (Recommended for MVP)**
```java
@Service
public class VehicleService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Vehicle createVehicle(VehicleDTO dto) {
        Vehicle vehicle = vehicleRepository.save(new Vehicle(dto));

        // Publish event - other modules can listen
        eventPublisher.publishEvent(new VehicleCreatedEvent(vehicle));

        return vehicle;
    }
}

// In Charging Module
@Component
public class VehicleEventListener {
    @EventListener
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        // Charging module reacts to vehicle creation
        if (event.getVehicle().getFuelType() == FuelType.EV) {
            chargingService.initializeChargingProfile(event.getVehicle().getId());
        }
    }
}
```

**Option B: Direct Service Calls (For MVP - simpler)**
```java
@Service
public class VehicleService {
    @Autowired
    private ChargingService chargingService; // Direct dependency OK in monolith

    public Vehicle createVehicle(VehicleDTO dto) {
        Vehicle vehicle = vehicleRepository.save(new Vehicle(dto));

        // Direct call - simpler for MVP
        if (vehicle.getFuelType() == FuelType.EV) {
            chargingService.initializeChargingProfile(vehicle.getId());
        }

        return vehicle;
    }
}
```

### 3. Database Strategy
**Keep 8 Separate Databases** (easier to split later)
```yaml
# application.yml
spring:
  datasource:
    auth:
      url: jdbc:postgresql://localhost:5432/evfleet_auth
      username: postgres
      password: ${DB_PASSWORD}
    fleet:
      url: jdbc:postgresql://localhost:5432/evfleet_fleet
      username: postgres
      password: ${DB_PASSWORD}
    charging:
      url: jdbc:postgresql://localhost:5432/evfleet_charging
      username: postgres
      password: ${DB_PASSWORD}
    # ... 5 more databases
```

**Configure Multiple DataSources:**
```java
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.auth")
    public DataSource authDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.fleet")
    public DataSource fleetDataSource() {
        return DataSourceBuilder.create().build();
    }

    // ... more data sources
}
```

### 4. Security - Centralized
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/fleet/**").hasRole("FLEET_MANAGER")
                .requestMatchers("/api/charging/**").hasRole("FLEET_MANAGER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## Migration Plan: 15 PRs

### **PHASE 1: Foundation (3 PRs)**

#### **PR 1: Create Monolith Project Structure**
**Goal:** Setup new Spring Boot project with modular structure

**Tasks:**
- Create `evfleet-monolith` Spring Boot project
- Setup Maven with all dependencies
- Create package structure (auth/, fleet/, charging/, etc.)
- Configure application.yml with multiple datasources
- Setup Redis, RabbitMQ connections
- Configure Flyway for 8 databases
- Create Dockerfile
- Add actuator, Swagger, monitoring

**Files Created:**
- `EvFleetApplication.java`
- `pom.xml` (consolidated dependencies)
- `application.yml`, `application-dev.yml`, `application-prod.yml`
- `DataSourceConfig.java`
- `SecurityConfig.java`
- `Dockerfile`
- `docker-compose.yml` (simplified - 1 service)

**Acceptance Criteria:**
- ✅ Application starts successfully
- ✅ All 8 datasources connect
- ✅ Actuator health check passes
- ✅ Swagger UI accessible at /swagger-ui.html
- ✅ Docker image builds successfully

---

#### **PR 2: Migrate Common/Shared Code**
**Goal:** Move shared utilities, exceptions, DTOs, configs

**Tasks:**
- Create `common/` package
- Move common exceptions (ResourceNotFoundException, etc.)
- Move common DTOs (ErrorResponse, etc.)
- Move security filters (FirebaseAuthFilter)
- Move utility classes (DateUtil, ValidationUtil)
- Setup global exception handler
- Configure CORS, rate limiting

**Files Migrated:**
- All `common/exception/*`
- All `common/dto/*`
- All `common/config/*`
- All `common/security/*`
- All `common/util/*`

**Acceptance Criteria:**
- ✅ No compilation errors
- ✅ Security filter works
- ✅ Exception handler catches all errors
- ✅ CORS configured correctly

---

#### **PR 3: Setup Event Bus + Inter-Module Communication**
**Goal:** Enable modules to communicate without tight coupling

**Tasks:**
- Create `common/event/` package
- Implement internal event bus (Spring ApplicationEventPublisher)
- Create base event classes
- Add event listeners
- Optional: Configure RabbitMQ for async events

**Files Created:**
- `common/event/DomainEvent.java` (base class)
- `common/event/EventPublisher.java`
- `common/event/VehicleCreatedEvent.java`
- `common/event/ChargingSessionStartedEvent.java`
- `common/config/EventConfig.java`

**Acceptance Criteria:**
- ✅ Events publish successfully
- ✅ Listeners receive events
- ✅ Async processing works
- ✅ No tight coupling between modules

---

### **PHASE 2: Module Migration (8 PRs - One per module)**

#### **PR 4: Migrate Auth Module**
**Goal:** Move auth-service code into monolith

**Tasks:**
- Copy `auth-service/` code to `auth/` module
- Update package names: `com.evfleet.auth.*` → `com.evfleet.auth.*`
- Update imports (remove cross-service calls)
- Move controllers to `/api/auth/*` endpoints
- Move Flyway migrations to `db/migration/auth/`
- Update tests
- Configure auth datasource

**Files Migrated:**
- `auth/model/User.java`
- `auth/model/Role.java`
- `auth/repository/UserRepository.java`
- `auth/service/UserService.java`
- `auth/controller/AuthController.java`
- `auth/config/FirebaseConfig.java`
- All auth DTOs, exceptions
- All auth tests

**Acceptance Criteria:**
- ✅ Firebase authentication works
- ✅ User CRUD APIs work
- ✅ JWT generation works
- ✅ Role-based access control works
- ✅ All auth tests pass

---

#### **PR 5: Migrate Fleet Module**
**Goal:** Move fleet-service code into monolith

**Tasks:**
- Copy `fleet-service/` code to `fleet/` module
- Update package names
- Update imports - replace RestTemplate calls with direct service calls
- Move controllers to `/api/fleet/*`
- Move Flyway migrations to `db/migration/fleet/`
- Update event publishing (VehicleCreatedEvent)
- Update tests

**Files Migrated:**
- `fleet/model/Vehicle.java`
- `fleet/model/Trip.java`
- `fleet/model/TelemetryData.java`
- `fleet/repository/VehicleRepository.java`
- `fleet/service/VehicleService.java`
- `fleet/service/TripService.java`
- `fleet/controller/VehicleController.java`
- All fleet DTOs, validators
- All fleet tests

**Acceptance Criteria:**
- ✅ Vehicle CRUD works
- ✅ Trip tracking works
- ✅ Telemetry ingestion works
- ✅ Real-time updates work
- ✅ All fleet tests pass

---

#### **PR 6: Migrate Charging Module**
**Goal:** Move charging-service code into monolith

**Tasks:**
- Copy `charging-service/` code to `charging/` module
- Update Saga pattern (use internal events instead of RabbitMQ)
- Update controllers to `/api/charging/*`
- Move migrations
- Listen to VehicleCreatedEvent (from Fleet)
- Update tests

**Files Migrated:**
- `charging/model/ChargingStation.java`
- `charging/model/ChargingSession.java`
- `charging/service/ChargingService.java`
- `charging/saga/ChargingSessionSaga.java`
- `charging/controller/ChargingController.java`
- All charging tests

**Acceptance Criteria:**
- ✅ Station CRUD works
- ✅ Session start/end works
- ✅ Saga orchestration works
- ✅ Cost calculation works
- ✅ All charging tests pass

---

#### **PR 7: Migrate Maintenance Module**
**Goal:** Move maintenance-service code into monolith

**Tasks:**
- Copy code to `maintenance/` module
- Update event sourcing (keep eventsourcing pattern)
- Update controllers to `/api/maintenance/*`
- Listen to VehicleCreatedEvent
- Move migrations
- Update tests

**Files Migrated:**
- `maintenance/model/MaintenanceSchedule.java`
- `maintenance/model/BatteryHealth.java`
- `maintenance/event/MaintenanceEventStore.java`
- `maintenance/service/MaintenanceService.java`
- `maintenance/controller/MaintenanceController.java`
- All maintenance tests

**Acceptance Criteria:**
- ✅ Maintenance scheduling works
- ✅ Battery health tracking works
- ✅ Event sourcing works
- ✅ Predictive alerts work
- ✅ All maintenance tests pass

---

#### **PR 8: Migrate Driver Module**
**Goal:** Move driver-service code into monolith

**Tasks:**
- Copy code to `driver/` module
- Update CQRS (command/query separation)
- Update controllers to `/api/driver/*`
- Move migrations
- Update tests

**Files Migrated:**
- `driver/model/Driver.java`
- `driver/cqrs/DriverReadModel.java`
- `driver/service/DriverService.java`
- `driver/controller/DriverController.java`
- All driver tests

**Acceptance Criteria:**
- ✅ Driver CRUD works
- ✅ Driver assignment works
- ✅ Performance tracking works
- ✅ CQRS pattern works
- ✅ All driver tests pass

---

#### **PR 9: Migrate Analytics Module**
**Goal:** Move analytics-service code into monolith

**Tasks:**
- Copy code to `analytics/` module
- Update TimescaleDB queries
- Update controllers to `/api/analytics/*`
- Listen to multiple events (trips, charging, maintenance)
- Move migrations
- Update tests

**Files Migrated:**
- `analytics/model/CostAnalytics.java`
- `analytics/service/AnalyticsService.java`
- `analytics/controller/AnalyticsController.java`
- All analytics tests

**Acceptance Criteria:**
- ✅ TCO calculation works
- ✅ Reports generate correctly
- ✅ Time-series queries work
- ✅ Dashboard data loads
- ✅ All analytics tests pass

---

#### **PR 10: Migrate Notification Module**
**Goal:** Move notification-service code into monolith

**Tasks:**
- Copy code to `notification/` module
- Update event listeners (listen to all module events)
- Update controllers to `/api/notification/*`
- Move migrations
- Update tests

**Files Migrated:**
- `notification/model/Notification.java`
- `notification/model/AlertRule.java`
- `notification/service/NotificationService.java`
- `notification/controller/NotificationController.java`
- All notification tests

**Acceptance Criteria:**
- ✅ Email notifications work
- ✅ SMS notifications work
- ✅ Push notifications work
- ✅ Alert rules trigger correctly
- ✅ All notification tests pass

---

#### **PR 11: Migrate Billing Module**
**Goal:** Move billing-service code into monolith

**Tasks:**
- Copy code to `billing/` module
- Update Saga + Event Sourcing
- Update controllers to `/api/billing/*`
- Listen to usage events
- Move migrations
- Update tests

**Files Migrated:**
- `billing/model/Invoice.java`
- `billing/model/Subscription.java`
- `billing/saga/BillingSaga.java`
- `billing/service/BillingService.java`
- `billing/controller/BillingController.java`
- All billing tests

**Acceptance Criteria:**
- ✅ Subscription management works
- ✅ Invoice generation works
- ✅ Payment processing works
- ✅ Billing saga works
- ✅ All billing tests pass

---

### **PHASE 3: Integration & Testing (4 PRs)**

#### **PR 12: API Gateway Logic (Built-in)**
**Goal:** Implement gateway features (rate limiting, routing) in monolith

**Tasks:**
- Create `gateway/` package
- Add rate limiting filter
- Add request/response logging
- Add API versioning support
- Add circuit breaker (Resilience4j)
- Update Swagger to show all endpoints

**Files Created:**
- `gateway/filter/RateLimitFilter.java`
- `gateway/filter/LoggingFilter.java`
- `gateway/config/GatewayConfig.java`
- `common/config/SwaggerConfig.java` (updated)

**Acceptance Criteria:**
- ✅ Rate limiting works
- ✅ Request logging works
- ✅ Swagger shows all APIs
- ✅ Circuit breaker triggers
- ✅ API versioning works

---

#### **PR 13: Update Frontend API Calls**
**Goal:** Change frontend to call single monolith endpoint

**Tasks:**
- Update `frontend/src/services/api.ts`
- Change base URLs from multiple (8081-8088) to single (8080)
- Update environment variables
- Test all frontend flows

**Files Updated:**
- `frontend/src/services/api.ts`
- `frontend/src/services/authService.ts`
- `frontend/src/services/fleetService.ts`
- `frontend/src/services/chargingService.ts`
- All other service files
- `.env`, `.env.production`

**Acceptance Criteria:**
- ✅ All API calls work
- ✅ Authentication works
- ✅ Real-time updates work
- ✅ No CORS issues
- ✅ Frontend fully functional

---

#### **PR 14: Integration Testing**
**Goal:** Test entire application end-to-end

**Tasks:**
- Write integration tests for critical flows:
  - User registration → vehicle creation → trip tracking
  - Vehicle creation → charging session → billing
  - Maintenance scheduling → notification
- Test inter-module communication
- Test event flow
- Performance testing

**Files Created:**
- `src/test/java/com/evfleet/integration/VehicleFlowTest.java`
- `src/test/java/com/evfleet/integration/ChargingFlowTest.java`
- `src/test/java/com/evfleet/integration/BillingFlowTest.java`

**Acceptance Criteria:**
- ✅ All integration tests pass
- ✅ Inter-module events work
- ✅ No data inconsistencies
- ✅ Performance acceptable (<500ms p99)
- ✅ No memory leaks

---

#### **PR 15: Deployment & Documentation**
**Goal:** Deploy monolith and update docs

**Tasks:**
- Update docker-compose.yml (remove 11 services → 1 service)
- Update Dockerfile
- Update deployment scripts
- Update README.md
- Create migration guide
- Update architecture docs

**Files Updated:**
- `docker-compose.yml` (simplified)
- `Dockerfile` (single service)
- `README.md`
- `docs/DEPLOYMENT_GUIDE.md`
- `docs/MICROSERVICES_TO_MONOLITH.md` (new)
- `docs/ARCHITECTURE.md` (updated)

**Acceptance Criteria:**
- ✅ Docker Compose starts with 1 command
- ✅ All services work
- ✅ Documentation is clear
- ✅ Migration guide is complete
- ✅ CI/CD pipeline updated

---

## Benefits Summary

### Cost Efficiency
```
Before: 11 containers + managed services = ₹56,000/month
After:  1 container on powerful server  = ₹5,000/month
Savings: 90% reduction = ₹51,000/month
```

### Architectural Efficiency
- ✅ **Simpler deployment** - 1 Docker container vs 11
- ✅ **Faster development** - No network calls between services
- ✅ **Easier debugging** - Single codebase, single log stream
- ✅ **Better performance** - In-process calls vs HTTP/REST
- ✅ **Maintains modularity** - Can extract services later
- ✅ **Same features** - All microservices patterns kept (CQRS, Saga, Event Sourcing)

### When to Split Back to Microservices?
Split when:
- 🔥 **Traffic:** >1000 requests/second
- 🔥 **Team size:** >15 developers
- 🔥 **Revenue:** >₹50L MRR (can afford ₹50K/month infrastructure)
- 🔥 **Scaling need:** Individual services need different resources

For MVP with 50-500 customers: **Monolith is perfect**

---

## Timeline

**2-3 Weeks with Focused Work:**
- Week 1: Phase 1 (Foundation) - PRs 1-3
- Week 2: Phase 2 (Module Migration) - PRs 4-11
- Week 3: Phase 3 (Integration) - PRs 12-15

**With GitHub Copilot:** Can be done in 10-15 days

---

## Next Steps

1. Review this plan - approve or request changes
2. I'll create Copilot prompt for these 15 PRs
3. Start migration PR by PR
4. Test after each module
5. Deploy monolith to Hetzner/DigitalOcean
6. Update frontend
7. Launch MVP

**Ready to create the Copilot prompt?** Let me know!
