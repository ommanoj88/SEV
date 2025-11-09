# MASTER CONTEXT PROMPT FOR GITHUB COPILOT
## Strategic Migration: EV-Only → General + EV Excellence

---

## 🎯 CONTEXT & VISION

### Strategic Pivot
We are migrating our EV Fleet Management System from **EV-specialized** to **General Fleet Management with EV as Premium Feature**.

**Customer Insight:** "Pay more for general which includes EV"

**Three Pricing Tiers:**
1. **BASIC (₹299/month/vehicle)** - All vehicles (ICE, EV, Hybrid) - General fleet management
2. **EV PREMIUM (₹699/month/vehicle)** - All features + EV-specific optimization
3. **ENTERPRISE (₹999/month/vehicle)** - Multi-depot, custom integrations, dedicated support

### Why This Matters
- **Market Reality:** Most logistics companies run mixed fleets (ICE + EV during transition)
- **Competitive Advantage:** EV optimization built-in, not retrofitted
- **Revenue Growth:** Serve entire fleet market, not just EV buyers
- **Future-Ready:** Ready for 100% EV transition

### Total Scope
- **18 PRs** across all layers
- **~1,620 development hours**
- **8-10 weeks timeline** (with Copilot assistance reducing to ~26 days focused work)
- **Zero breaking changes** (backward compatible)

---

## 🏗️ TECHNOLOGY STACK

### Backend Architecture
```
Java 17 + Spring Boot 3.x
├── Microservices: 11 independent services
├── Spring Cloud (Eureka, API Gateway)
├── Spring Data JPA + PostgreSQL 15
├── RabbitMQ for event messaging
├── Redis for caching
└── Flyway for database migrations
```

### Services Overview
1. **Fleet Service** (Port 8082) - Vehicle, Trip, Telemetry, Geofence management
2. **Charging Service** - Charging stations, sessions, route optimization (EV/Hybrid only)
3. **Driver Service** - Driver management, behavior tracking
4. **Maintenance Service** - Service schedules, battery health, ICE + EV
5. **Analytics Service** - Cost analysis, utilization, carbon footprint
6. **Billing Service** - Subscriptions, invoicing, payments
7. **Auth Service** - Firebase-based authentication
8. **Notification Service** - Multi-channel notifications
9. **API Gateway** - Request routing, circuit breaker
10. **Config Server** - Centralized configuration
11. **Eureka Server** - Service registry

### Frontend Stack
```
React 18 + TypeScript
├── Redux Toolkit for state management
├── Material-UI (MUI) for components
├── Mapbox GL for maps
├── Recharts for visualizations
├── Socket.io for real-time updates
└── Firebase for authentication
```

### Database Structure
```
PostgreSQL 15 per service
├── Fleet Service: vehicles, telemetry_data, trips, geofences, fuel_consumption
├── Charging Service: charging_stations, charging_sessions, charging_networks
├── Maintenance Service: maintenance_schedules, service_history, battery_health
├── Driver Service: drivers, driver_behavior, driver_assignments
├── Analytics Service: cost_analytics, utilization_reports, carbon_footprint
├── Billing Service: subscriptions, invoices, payments, pricing_plans
├── Auth Service: users, roles, permissions
└── Notification Service: notifications, alert_rules, notification_templates
```

---

## 📋 THE 18 PRs BREAKDOWN

### Phase 1: Database & Data Model (Weeks 1-2, PRs 1-4)
**Goal:** Add fuel type support to all data models

1. **PR #1:** Add Vehicle Fuel Type Support to Database
   - Add `fuel_type` ENUM (ICE, EV, HYBRID) to vehicles table
   - Add ICE columns: fuelTankCapacity, fuelLevel, engineType
   - Create fuel_consumption table
   - Update Vehicle entity

2. **PR #2:** Create Feature Flag System for EV Features
   - Create FeatureToggle entity
   - Pre-populate features by vehicle type
   - Create @RequireFeature annotation
   - Implement FeatureToggleService

3. **PR #3:** Extend Telemetry Data Model for Multi-Fuel Support
   - Add fuel fields to telemetry_data (fuelLevel, fuelConsumptionRate, engineRpm, etc.)
   - Create TelemetryType enum
   - Add indexes for performance

4. **PR #4:** Update Vehicle Service Repository Queries
   - Add repository methods for fuel type filtering
   - Add fleet composition queries
   - Create new API endpoints for filtering

### Phase 2: API Enhancements (Weeks 3-4, PRs 5-8)
**Goal:** Update all APIs to support multi-fuel operations

5. **PR #5:** Update Vehicle CRUD APIs to Support Fuel Type
   - Create VehicleCreateRequestDTO with fuelType
   - Conditional field validation (EV vs ICE)
   - Create FuelTypeValidator
   - Update all endpoints with feature availability

6. **PR #6:** Extend Telemetry APIs for Multi-Fuel Metrics
   - Create TelemetryRequestDTO supporting both fuel types
   - Create TelemetryValidator
   - Create TelemetryProcessingService
   - Route data to correct processor

7. **PR #7:** Create Multi-Fuel Trip Analytics Endpoints
   - Add energyCost, fuelCost, totalOperatingCost to Trip
   - Create EVCostCalculator and ICECostCalculator
   - Implement MultiFleetAnalyticsService
   - Add analytics endpoints

8. **PR #8:** Conditional Feature Availability in Trip APIs
   - Create AvailableFeaturesDTO
   - Return available features based on vehicle type
   - Add endpoint to get available features per vehicle

### Phase 3: Charging Service (Week 5, PRs 9-10)
**Goal:** Ensure Charging Service only works with EV/Hybrid

9. **PR #9:** Update Charging Service to Handle EV/Hybrid Only
   - Add VehicleTypeValidator
   - Return 400 error for ICE vehicles
   - Add NotAnEVVehicleException

10. **PR #10:** Extend Charging Session Analytics
    - Create ChargingAnalyticsService
    - Add analytics endpoint
    - Show utilization and cost metrics

### Phase 4: Maintenance Service (Weeks 5-6, PRs 11-12)
**Goal:** Add ICE-specific maintenance

11. **PR #11:** Extend Maintenance Service for ICE-Specific Services
    - Create MaintenanceType enum (OIL_CHANGE, AIR_FILTER, TRANSMISSION_FLUID, etc.)
    - Create MaintenanceScheduleBuilder
    - Pre-populate ICE schedules
    - Generate appropriate schedules per vehicle type

12. **PR #12:** Create Multi-Fuel Cost Tracking for Maintenance
    - Track costs by maintenance type
    - Create MaintenanceCostAnalyticsService
    - Show cost breakdown for TCO

### Phase 5: Frontend Updates (Weeks 7-8, PRs 13-16)
**Goal:** Update UI for multi-fuel support

13. **PR #13:** Update Vehicle Form Components for Multi-Fuel Selection
    - Add FuelTypeSelector component
    - Conditional field rendering (EV vs ICE)
    - Form validation per fuel type

14. **PR #14:** Update Vehicle List and Details Pages
    - Show fuel type with icon/color
    - Add fuel type filtering
    - Create tabs based on available features
    - Create FuelStatusPanel

15. **PR #15:** Create Charging and Fuel Station Discovery
    - Create StationDiscovery component
    - Create StationMap (Mapbox integration)
    - Create StationCard component
    - Add fuelService

16. **PR #16:** Create Multi-Fuel Dashboard Overview
    - Create FleetCompositionCard (EV%, ICE%, Hybrid%)
    - Create CostBreakdownCard (Energy + Fuel costs)
    - Create MaintenanceAlertsCard (fuel-type-specific)

### Phase 6: Billing & Monetization (Week 9, PRs 17-18)
**Goal:** Implement pricing tiers

17. **PR #17:** Implement Multi-Tier Pricing Structure
    - Create PricingTier entity (BASIC, EV_PREMIUM, ENTERPRISE)
    - Create PricingService
    - Add pricing endpoints
    - Create frontend PricingPage

18. **PR #18:** Create Invoice Generation and Payment Tracking
    - Create InvoiceGenerationService (monthly schedule)
    - Group vehicles by tier
    - Create PaymentProcessingService
    - Update BillingPage

---

## 🛠️ HOW TO USE THIS PROMPT

### For Each PR, Follow This Pattern:

**Step 1:** Paste this master context into Copilot (one-time setup)

**Step 2:** For each PR, use this format:
```
PR #[NUMBER]: [TITLE]

Work on the following:

1. Backend files to create/modify:
   - [List specific files]

2. Database migrations:
   - [Describe migration]

3. Frontend components (if applicable):
   - [List components]

4. Acceptance criteria:
   - [List what must be tested]

5. Dependencies:
   - [List blocking PRs]

Generate code following our architecture:
- Java: Spring Boot 3, Spring Data JPA, DDD patterns
- Frontend: React 18, TypeScript, Material-UI
- Database: PostgreSQL, Flyway migrations
- Testing: Unit tests, integration tests, > 85% coverage

Refer to MIGRATION_STRATEGY_GENERAL_EV.md for detailed specifications.
```

**Step 3:** Review generated code for:
- ✅ Correct imports and package structure
- ✅ Proper exception handling
- ✅ Test coverage
- ✅ Documentation/comments
- ✅ No security vulnerabilities

**Step 4:** Implement locally, test, create PR

---

## 🏛️ ARCHITECTURE PRINCIPLES

### Database Layer
```java
@Entity
public class Vehicle {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType; // ICE, EV, HYBRID

    // EV fields (optional)
    private Double batteryCapacity;
    private Double currentBatterySoc;
    private String defaultChargerType;

    // ICE fields (optional)
    private Double fuelTankCapacity;
    private Double fuelLevel;

    // Common fields
    private String vehicleNumber;
    private VehicleStatus status;
    private Double latitude;
    private Double longitude;
}
```

### Service Layer Pattern
```java
@Service
public class VehicleService {
    // Multi-fuel aware methods
    public List<Vehicle> getVehiclesByFuelType(Long companyId, FuelType type)
    public Map<FuelType, Long> getFleetComposition(Long companyId)
    public List<String> getAvailableFeatures(Long vehicleId)
}
```

### API Layer Pattern
```java
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(
        @Valid @RequestBody VehicleCreateRequestDTO request) {
        // Validate fuel type specific fields
        // Create vehicle
        // Return with available features
    }
}
```

### Frontend Component Pattern
```typescript
// Conditional rendering based on fuel type
export const VehicleForm: React.FC = () => {
    const [fuelType, setFuelType] = useState<'ICE' | 'EV' | 'HYBRID'>('EV');

    return (
        <>
            <FuelTypeSelector value={fuelType} onChange={setFuelType} />

            {(fuelType === 'EV' || fuelType === 'HYBRID') && (
                <BatteryFields />
            )}

            {(fuelType === 'ICE' || fuelType === 'HYBRID') && (
                <FuelFields />
            )}
        </>
    );
};
```

---

## ✅ ACCEPTANCE CRITERIA (FOR EVERY PR)

### Code Quality
- [ ] Code follows existing style and patterns
- [ ] All classes have proper comments/documentation
- [ ] No code duplication
- [ ] Proper exception handling
- [ ] Security vulnerabilities checked

### Testing
- [ ] Unit tests written for business logic
- [ ] Integration tests for API endpoints
- [ ] Database migration tested
- [ ] Test coverage > 85%
- [ ] All existing tests still pass

### Documentation
- [ ] Swagger/OpenAPI updated
- [ ] Database schema documented
- [ ] Code comments explain non-obvious logic
- [ ] README updated if needed

### Backward Compatibility
- [ ] No breaking API changes
- [ ] Existing vehicle data still works
- [ ] Old queries still return results
- [ ] Feature flags default to safe state

### Performance
- [ ] Database queries use indexes
- [ ] No N+1 query problems
- [ ] API response time < 500ms
- [ ] Frontend component renders < 3s

---

## 📁 KEY FILE LOCATIONS REFERENCE

### Backend - Fleet Service
```
backend/fleet-service/
├── src/main/java/com/evfleet/fleet/
│   ├── model/          (Entities: Vehicle, Trip, TelemetryData, FuelConsumption)
│   ├── repository/     (JPA Repositories with custom queries)
│   ├── service/        (Business logic: VehicleService, TelemetryProcessingService)
│   ├── controller/     (REST Controllers)
│   ├── dto/            (Request/Response DTOs)
│   ├── validation/     (FuelTypeValidator, TelemetryValidator)
│   ├── annotation/     (RequireFeature)
│   └── exception/      (Custom exceptions)
├── src/main/resources/
│   ├── db/migration/   (Flyway migrations: V1__, V2__, etc.)
│   └── application.yml (Configuration)
└── src/test/java/     (Unit & integration tests)
```

### Frontend
```
frontend/src/
├── components/
│   ├── fleet/          (Vehicle-related: VehicleList, VehicleDetails, FuelStatusPanel)
│   ├── charging/       (Charging-related: ChargingStations, ChargingSessionList)
│   ├── dashboard/      (Dashboard cards: FleetCompositionCard, CostBreakdownCard)
│   └── common/         (Reusable: FuelTypeSelector, StatusBadge)
├── services/
│   ├── vehicleService.ts
│   ├── chargingService.ts
│   ├── fuelService.ts  (NEW for ICE fuel stations)
│   └── analyticsService.ts
├── pages/              (Full pages: DashboardPage, FleetManagementPage, PricingPage)
├── redux/              (State management slices)
└── constants/
    └── fuelTypes.ts    (FUEL_TYPES mapping)
```

### Database Migrations
```
backend/fleet-service/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_fuel_type_support.sql      (PR #1)
├── V3__create_fuel_consumption_table.sql (PR #1)
├── V4__create_feature_toggles_table.sql (PR #2)
├── V5__extend_telemetry_for_multi_fuel.sql (PR #3)
└── ... more migrations per PR
```

---

## 🔑 KEY DESIGN PATTERNS

### Feature Flags Pattern
```sql
-- In feature_toggles table
INSERT INTO feature_toggles VALUES
('BATTERY_TRACKING', 'EV,HYBRID', true),
('CHARGING_SESSION', 'EV,HYBRID', true),
('FUEL_CONSUMPTION', 'ICE,HYBRID', true),
('OIL_CHANGE_REMINDER', 'ICE,HYBRID', true);

-- Usage in code
if (featureToggleService.isFeatureEnabled("BATTERY_TRACKING", vehicle.getFuelType())) {
    // Show battery tracking UI
}
```

### Conditional Field Validation
```java
// In FuelTypeValidator
public void validateCreateRequest(VehicleCreateRequestDTO dto) {
    if (dto.getFuelType() == FuelType.EV || dto.getFuelType() == FuelType.HYBRID) {
        if (dto.getBatteryCapacity() == null) {
            throw new ValidationException("Battery capacity required for EV");
        }
    }
    if (dto.getFuelType() == FuelType.ICE || dto.getFuelType() == FuelType.HYBRID) {
        if (dto.getFuelTankCapacity() == null) {
            throw new ValidationException("Tank capacity required for ICE");
        }
    }
}
```

### Multi-Fuel Cost Calculation
```java
// In AnalyticsService
public CostBreakdownDTO getCostBreakdown(Long companyId) {
    List<Vehicle> vehicles = fleetService.getCompanyVehicles(companyId);

    Double evCost = vehicles.stream()
        .filter(v -> v.getFuelType() == FuelType.EV)
        .map(v -> calculateEVCost(v))
        .reduce(0.0, Double::sum);

    Double iceCost = vehicles.stream()
        .filter(v -> v.getFuelType() == FuelType.ICE)
        .map(v -> calculateICECost(v))
        .reduce(0.0, Double::sum);

    return CostBreakdownDTO.builder()
        .evCost(evCost)
        .iceCost(iceCost)
        .totalCost(evCost + iceCost)
        .build();
}
```

---

## ⚠️ CRITICAL REMINDERS

### DO:
✅ Make all new fields NULLABLE in database (backward compatibility)
✅ Default fuel_type to 'EV' for existing vehicles
✅ Keep all existing EV fields in Vehicle entity
✅ Test with mixed fleet (1 EV, 1 ICE, 1 Hybrid)
✅ Write tests for edge cases (zero fuel, zero battery, etc.)
✅ Use @Query annotations for complex JPA queries
✅ Add proper indexes to new columns
✅ Document why fields are optional
✅ Test migration scripts against test database first
✅ Include rollback scripts for migrations

### DON'T:
❌ Make fuel_type changes for existing vehicles (only during initial migration)
❌ Add new required fields without migration
❌ Break existing API contracts
❌ Assume all vehicles have battery capacity
❌ Assume all vehicles have fuel tank capacity
❌ Hardcode fuel type logic in controllers (use service methods)
❌ Forget to update Swagger/OpenAPI docs
❌ Skip performance tests for new queries
❌ Ignore error handling for invalid fuel types
❌ Create large migrations without transaction safety

---

## 📊 METRICS TO TRACK

### Development Progress
- PRs merged: X/18
- Total hours: X/1,620
- Test coverage: X%
- Critical bugs: X

### Code Quality
- Code coverage: > 85%
- SonarQube score: > 80
- Critical issues: 0
- Security issues: 0

### Performance
- API response time: < 500ms (p99)
- Database query time: < 100ms (p95)
- Page load time: < 3s
- Telemetry ingestion: 1000+ msgs/sec

---

## 🎬 GETTING STARTED

### First Time Setup:
1. Read this entire prompt and understand the context
2. Review the full `MIGRATION_STRATEGY_GENERAL_EV.md` document
3. Start with **PR #1: Add Vehicle Fuel Type Support**

### For Each PR:
1. Read PR section from `MIGRATION_STRATEGY_GENERAL_EV.md`
2. Use the detailed "Copilot Prompt - Short Version" from `COPILOT_QUICK_START.md`
3. Generate code with Copilot
4. Review and test locally
5. Create PR on GitHub
6. Update `MIGRATION_ROADMAP_VISUAL.md` with progress

### Expected Workflow:
```
Day 1: PR #1 & #2 (Database foundation)
Day 2-3: PR #3 & #4 (Queries)
Day 4-5: PR #5 & #6 (APIs)
Day 6: PR #7 & #8 (Analytics & Features)
Day 7: PR #9 & #10 (Charging)
Day 8: PR #11 & #12 (Maintenance)
Day 9-10: PR #13 & #14 (Forms & Lists)
Day 11: PR #15 & #16 (Discovery & Dashboard)
Day 12: PR #17 & #18 (Billing)
Days 13-14: Testing & QA
```

---

## 📞 WHEN TO USE THIS PROMPT

**Use this master context when:**
- Starting a new Copilot session
- Switching between different PRs
- Need to refresh context mid-session
- Want to explain the architecture to team members

**Don't need to repeat when:**
- Already in active Copilot session
- Copilot remembers context from earlier in conversation
- Working on continuation of same PR

---

## ✨ EXPECTED OUTCOMES

### After PR #1:
- ✅ Vehicle entity supports fuel types
- ✅ Database migration verified
- ✅ All tests passing

### After PR #4:
- ✅ Complete data model ready
- ✅ All queries working
- ✅ Foundation stable for API layer

### After PR #8:
- ✅ All APIs updated for multi-fuel
- ✅ Feature flags working
- ✅ Ready for service updates

### After PR #16:
- ✅ Complete frontend support
- ✅ Users can manage any vehicle type
- ✅ Dashboard shows full fleet insights

### After PR #18:
- ✅ Revenue model operational
- ✅ Invoicing automated
- ✅ Multi-tier pricing active

---

**THIS IS YOUR MASTER CONTEXT. PASTE IT AT THE START OF EACH COPILOT SESSION.**

Once shared with Copilot, you can then just say:
- "PR #1: Add Vehicle Fuel Type Support to Database"
- "PR #5: Update Vehicle CRUD APIs"
- Etc.

And Copilot will understand the full context and requirements.
