# GitHub Copilot Quick Start Guide - General + EV Migration

**Document Purpose:** Quick reference for working with GitHub Copilot on migration PRs

---

## TL;DR - The 18 PR Plan

**Duration:** 8-10 weeks with Copilot
**Total PRs:** 18
**Estimated Effort per PR:** 70-115 hours (Backend + Frontend combined)

### Phase Breakdown
- **Phase 1 (2 weeks):** Database & Data Model (PRs 1-4)
- **Phase 2 (2 weeks):** API Enhancements (PRs 5-8)
- **Phase 3 (1 week):** Charging Service (PRs 9-10)
- **Phase 4 (1.5 weeks):** Maintenance Service (PRs 11-12)
- **Phase 5 (2.5 weeks):** Frontend (PRs 13-16)
- **Phase 6 (1 week):** Billing (PRs 17-18)
- **Testing & QA:** 2 weeks

---

## How to Use This Guide with Copilot

### For Each PR:
1. Read the PR summary below
2. Copy the "Copilot Guidance" section
3. Paste into Copilot with the specific file paths
4. Review generated code
5. Run acceptance criteria tests
6. Submit PR

### File Structure Reference
```
backend/
├── api-gateway/src/main/java/com/evfleet/gateway/
├── fleet-service/src/main/java/com/evfleet/fleet/
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── dto/
│   └── validation/
├── charging-service/src/main/java/com/evfleet/charging/
├── driver-service/src/main/java/com/evfleet/driver/
├── maintenance-service/src/main/java/com/evfleet/maintenance/
├── analytics-service/src/main/java/com/evfleet/analytics/
├── billing-service/src/main/java/com/evfleet/billing/
└── notification-service/src/main/java/com/evfleet/notification/

frontend/src/
├── components/
│   ├── fleet/
│   ├── charging/
│   ├── dashboard/
│   ├── maintenance/
│   ├── drivers/
│   ├── analytics/
│   └── common/
├── services/
├── pages/
├── redux/
└── constants/

docker/
├── docker-compose-infrastructure.yml
└── [service]/docker-compose.yml

database/migrations/
└── [service]/V[N]__[description].sql
```

---

## Phase 1: Database & Data Model (Weeks 1-2)

### PR #1: Add Vehicle Fuel Type Support to Database
**Status:** 🔴 NOT STARTED
**Effort:** 3-4 days (70 hours)
**Blocker:** YES - All other PRs depend on this

**What Gets Done:**
- Add `fuel_type` column to vehicles table (ICE, EV, HYBRID enum)
- Add `fuelTankCapacity`, `fuelLevel` for ICE vehicles
- Create `fuel_consumption` table
- Update Vehicle entity with new fields

**Copilot Prompt:**
```
Create a Flyway migration to add fuel type support to the vehicles table:

1. Add fuel_type column as VARCHAR(20) with values: ICE, EV, HYBRID
   - Default existing vehicles to EV
   - Add NOT NULL constraint after data migration

2. Add ICE-specific columns:
   - fuel_tank_capacity: DOUBLE PRECISION
   - fuel_level: DOUBLE PRECISION (%)
   - default_charger_type: VARCHAR(50) (for EV/HYBRID)

3. Create fuel_consumption table with fields:
   - id: BIGSERIAL PRIMARY KEY
   - vehicle_id: BIGINT (FK to vehicles)
   - trip_id: BIGINT (FK to trips)
   - fuel_consumed: DOUBLE PRECISION
   - fuel_type: VARCHAR(20)
   - consumption_rate: DOUBLE PRECISION
   - timestamp: TIMESTAMP
   - created_at: TIMESTAMP

4. Add indexes on fuel_type, fuel_level for query performance

5. Update Vehicle JPA entity to include:
   - @Enumerated FuelType fuelType
   - Double fuelTankCapacity
   - Double fuelLevel
   - String defaultChargerType

Keep all existing EV fields (battery_capacity, battery_soc, etc.) for backward compatibility.
```

**Files to Create/Modify:**
```
✏️ backend/fleet-service/src/main/resources/db/migration/V2__add_fuel_type_support.sql (NEW)
✏️ backend/fleet-service/src/main/resources/db/migration/V3__create_fuel_consumption_table.sql (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/Vehicle.java
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/FuelType.java (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/FuelConsumption.java (NEW)
```

**Test Files:**
```
✏️ backend/fleet-service/src/test/java/com/evfleet/fleet/repository/VehicleRepositoryTest.java
✏️ backend/fleet-service/src/test/java/com/evfleet/fleet/model/VehicleModelTest.java
```

**Acceptance Criteria:**
- ✅ Flyway migrations V2 and V3 execute without errors
- ✅ Vehicle entity has fuelType, fuelTankCapacity, fuelLevel fields
- ✅ FuelType enum created with ICE, EV, HYBRID values
- ✅ Existing vehicles default to EV (backward compatible)
- ✅ Database indexes created for performance
- ✅ All tests pass

---

### PR #2: Create Feature Flag System for EV Features
**Status:** 🔴 NOT STARTED
**Effort:** 2-3 days (80 hours)
**Blocker:** YES - Enables conditional logic

**What Gets Done:**
- Create FeatureToggle entity and table
- Create FeatureToggleService with enable/disable logic
- Create @RequireFeature annotation for methods
- Pre-populate with EV/ICE/Hybrid features

**Copilot Prompt:**
```
Create a feature flag system for managing EV-specific features based on vehicle type:

1. Create FeatureToggle entity:
   - id: Long
   - featureName: String (UNIQUE)
   - applicableVehicleTypes: String (JSON: "EV,HYBRID")
   - enabled: Boolean (default true)
   - createdAt, updatedAt: LocalDateTime

2. Create migration file with table and default data:
   - BATTERY_TRACKING: EV,HYBRID
   - CHARGING_SESSION: EV,HYBRID
   - RANGE_OPTIMIZATION: EV,HYBRID
   - REGENERATIVE_BRAKING: EV,HYBRID
   - BATTERY_HEALTH_MONITORING: EV,HYBRID
   - FUEL_CONSUMPTION: ICE,HYBRID
   - ENGINE_OIL_CHANGE: ICE,HYBRID

3. Create FeatureToggleService with methods:
   - isFeatureEnabled(String featureName, FuelType vehicleType): Boolean
   - getAvailableFeatures(FuelType vehicleType): List<String>
   - toggleFeature(String featureName, boolean enabled): void

4. Create @RequireFeature annotation:
   - value: String (feature name)
   - applicableTypes: String[] (vehicle types)
   - Use as @RequireFeature("BATTERY_TRACKING")

5. Create FeatureToggleRepository extending JpaRepository

6. Add feature validation in API Gateway or AOP interceptor
```

**Files to Create/Modify:**
```
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/FeatureToggle.java (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/repository/FeatureToggleRepository.java (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/service/FeatureToggleService.java (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/annotation/RequireFeature.java (NEW)
✏️ backend/fleet-service/src/main/resources/db/migration/V4__create_feature_toggles_table.sql (NEW)
```

**Acceptance Criteria:**
- ✅ FeatureToggle entity persists correctly
- ✅ Feature toggles pre-populated in database
- ✅ Service methods return correct features per fuel type
- ✅ @RequireFeature annotation functional
- ✅ Tests verify feature availability logic

---

### PR #3: Extend Telemetry Data Model for Multi-Fuel Support
**Status:** 🔴 NOT STARTED
**Effort:** 3-4 days (75 hours)
**Dependency:** PR #1

**What Gets Done:**
- Add fuel-specific fields to TelemetryData
- Create TelemetryType enum
- Update repository queries for new fields

**Copilot Prompt:**
```
Extend TelemetryData entity to support both EV and ICE metrics:

1. Update TelemetryData entity with fields:
   // Keep existing EV fields
   - batterySoc, batteryVoltage, batteryTemperature (nullable)
   - powerConsumption, regenerativePower (nullable)

   // Add ICE fields
   - fuelLevel: Double (%) (nullable)
   - fuelConsumptionRate: Double (L/100km) (nullable)
   - engineRpm: Integer (nullable)
   - engineTemperature: Double (nullable)
   - engineLoad: Double (nullable)
   - engineHours: Double (nullable)
   - fuelType: String (redundant for filtering)

2. Create TelemetryType enum:
   - EV_BATTERY
   - ICE_FUEL
   - HYBRID
   - GENERIC

3. Update TelemetryDataRepository with new query methods:
   - findByVehicleIdAndFuelLevelBetween(Long vehicleId, Double min, Double max)
   - findByVehicleIdAndFuelConsumptionRateGreaterThan(Long vehicleId, Double rate)
   - findByVehicleIdAndEngineRpmGreaterThan(Long vehicleId, Integer rpm)

4. Create migration file (V5__extend_telemetry_for_multi_fuel.sql):
   - ALTER TABLE telemetry_data ADD COLUMN fuel_level DOUBLE PRECISION;
   - ALTER TABLE telemetry_data ADD COLUMN fuel_consumption_rate DOUBLE PRECISION;
   - ALTER TABLE telemetry_data ADD COLUMN engine_rpm INTEGER;
   - ALTER TABLE telemetry_data ADD COLUMN engine_temperature DOUBLE PRECISION;
   - ALTER TABLE telemetry_data ADD COLUMN engine_load DOUBLE PRECISION;
   - ALTER TABLE telemetry_data ADD COLUMN engine_hours DOUBLE PRECISION;
   - ALTER TABLE telemetry_data ADD COLUMN fuel_type VARCHAR(20);
   - CREATE INDEX idx_fuel_level ON telemetry_data(fuel_level);
   - CREATE INDEX idx_engine_rpm ON telemetry_data(engine_rpm);

5. Maintain backward compatibility - all new fields optional
```

**Files to Create/Modify:**
```
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/TelemetryData.java
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/model/TelemetryType.java (NEW)
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/repository/TelemetryDataRepository.java
✏️ backend/fleet-service/src/main/resources/db/migration/V5__extend_telemetry_for_multi_fuel.sql (NEW)
```

**Acceptance Criteria:**
- ✅ All fuel-specific fields added to TelemetryData
- ✅ TelemetryType enum created
- ✅ Migration executes without errors
- ✅ New fields indexed for performance
- ✅ Backward compatibility maintained for EV queries

---

### PR #4: Update Vehicle Service Repository Queries for Multi-Fuel Filtering
**Status:** 🔴 NOT STARTED
**Effort:** 2-3 days (50 hours)
**Dependency:** PRs #1-3

**What Gets Done:**
- Add repository methods for fuel type filtering
- Add fleet composition queries
- Create new API endpoints for filtering

**Copilot Prompt:**
```
Add repository and service methods for multi-fuel vehicle queries:

1. Extend VehicleRepository with methods:
   List<Vehicle> findByFuelType(FuelType fuelType);
   List<Vehicle> findByCompanyIdAndFuelType(Long companyId, FuelType fuelType);
   List<Vehicle> findByCompanyIdAndFuelTypeIn(Long companyId, List<FuelType> types);
   List<Vehicle> findByFuelTypeAndStatusIn(FuelType fuelType, List<VehicleStatus> statuses);
   List<Vehicle> findByCompanyIdOrderByFuelType(Long companyId);
   List<Vehicle> findByCompanyIdAndFuelTypeAndFuelLevelLessThan(Long companyId, FuelType type, Double level);

   @Query("SELECT v FROM Vehicle v WHERE v.companyId = ?1 AND v.fuelType IN ('EV', 'HYBRID') AND v.currentBatterySoc < ?2")
   List<Vehicle> findEVVehiclesNeedingCharge(Long companyId, Double threshold);

   @Query("SELECT new map(v.fuelType as fuelType, COUNT(v) as count) FROM Vehicle v WHERE v.companyId = ?1 GROUP BY v.fuelType")
   List<Map<String, Object>> getFleetComposition(Long companyId);

2. Create VehicleService methods:
   - getVehiclesByFuelType(Long companyId, FuelType fuelType)
   - getFleetComposition(Long companyId) // Returns map of fuel type -> count
   - getLowBatteryVehicles(Long companyId, Double threshold)
   - getLowFuelVehicles(Long companyId, Double threshold)
   - getMixedFleetSummary(Long companyId)

3. Update VehicleController with endpoints:
   @GetMapping("/company/{companyId}/fuel-type/{fuelType}")
   public ResponseEntity<List<VehicleDTO>> getVehiclesByFuelType(...)

   @GetMapping("/company/{companyId}/fleet-composition")
   public ResponseEntity<Map<String, Long>> getFleetComposition(...)

   @GetMapping("/company/{companyId}/low-fuel")
   public ResponseEntity<List<VehicleDTO>> getLowFuelVehicles(...)

4. Ensure all queries use proper @Query annotations and JPA syntax
```

**Files to Create/Modify:**
```
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/repository/VehicleRepository.java
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/service/VehicleService.java
✏️ backend/fleet-service/src/main/java/com/evfleet/fleet/controller/VehicleController.java
```

**Acceptance Criteria:**
- ✅ All new repository methods created and tested
- ✅ Service methods implement filtering
- ✅ API endpoints functional and tested
- ✅ Query performance acceptable (< 500ms for 1000 vehicles)
- ✅ All existing tests still pass

---

## Phase 2: API Enhancements (Weeks 3-4)

### PR #5: Update Vehicle CRUD APIs to Support Fuel Type
**Effort:** 3-4 days (100 hours)
**Dependency:** PR #1

**Copilot Prompt - Short Version:**
```
Update Vehicle CRUD APIs to support fuel type selection:

1. Create VehicleCreateRequestDTO with field:
   @NotNull FuelType fuelType (REQUIRED)

   EV-specific fields (optional, required if fuelType is EV or HYBRID):
   - Double batteryCapacity
   - String defaultChargerType (CCS, CHAdeMO, Type2)

   ICE-specific fields (optional, required if fuelType is ICE or HYBRID):
   - Double fuelTankCapacity

2. Create VehicleResponseDTO with:
   - All fields from request
   - List<String> availableFeatures (populated from FeatureToggleService)

3. Create FuelTypeValidator that validates:
   - If EV/HYBRID: batteryCapacity required, defaultChargerType required
   - If ICE/HYBRID: fuelTankCapacity required

4. Update VehicleController:
   - POST /vehicles: Validate with FuelTypeValidator before creation
   - GET /vehicles/{id}: Return with availableFeatures
   - PUT /vehicles/{id}: Prevent fuel type change (validation)

5. Update Swagger/OpenAPI documentation to show conditional fields

Keep backward compatibility by making fuelType default to EV.
```

**Status:** 🔴 NOT STARTED

---

### PR #6: Extend Telemetry APIs for Multi-Fuel Metrics
**Effort:** 2-3 days (80 hours)
**Dependency:** PR #3

**Copilot Prompt - Short Version:**
```
Extend telemetry API to handle both EV and ICE metrics:

1. Create TelemetryRequestDTO with:
   - vehicleId, timestamp (required)
   - latitude, longitude (required)
   - batterySoc, batteryVoltage, batteryTemperature (optional, for EV)
   - fuelLevel, engineRpm, engineTemperature (optional, for ICE)
   - All other common fields

2. Create TelemetryValidator:
   - validateEVTelemetry(): requires battery fields
   - validateICETelemetry(): requires fuel/engine fields
   - validateHybridTelemetry(): allows both

3. Create TelemetryProcessingService:
   - process(TelemetryRequestDTO, Vehicle): Routes to EV/ICE processor
   - processEVTelemetry()
   - processICETelemetry()
   - processHybridTelemetry()

4. Update TelemetryController:
   POST /telemetry: Validate based on vehicle.fuelType
   POST /telemetry/batch: Batch processing

5. Performance requirement: < 100ms per submission
```

**Status:** 🔴 NOT STARTED

---

### PR #7: Create Multi-Fuel Trip Analytics Endpoints
**Effort:** 2-3 days (95 hours)
**Dependency:** PRs #1-6

**Copilot Prompt - Short Version:**
```
Create trip analytics for multi-fuel vehicles:

1. Update Trip entity with:
   - energyCost (for EV)
   - fuelCost (for ICE)
   - totalOperatingCost
   - carbonFootprint
   - averageFuelConsumption

2. Create EVCostCalculator:
   - calculateCost(trip, energyConsumed, ratePerKwh)
   - calculateCarbonFootprint(energyConsumed)

3. Create ICECostCalculator:
   - calculateCost(trip, fuelConsumed, ratePerLiter)
   - calculateCarbonFootprint(fuelConsumed)

4. Create MultiFleetAnalyticsService:
   - getFleetAnalytics(companyId, startDate, endDate)
   - getCostBreakdown(companyId) // EV cost + ICE cost
   - getCarbonFootprint(companyId)

5. Add endpoints:
   GET /analytics/company/{id}/fleet-summary
   GET /analytics/company/{id}/cost-breakdown
   GET /analytics/company/{id}/trip-analytics/{tripId}
```

**Status:** 🔴 NOT STARTED

---

### PR #8: Conditional Feature Availability in Trip APIs
**Effort:** 1-2 days (55 hours)
**Dependency:** PR #2

**Copilot Prompt - Short Version:**
```
Add feature availability to trip endpoints:

1. Create AvailableFeaturesDTO with:
   List<String> tripFeatures
   List<String> maintenanceFeatures
   List<String> analyticsFeatures

2. Update TripService:
   - getAvailableTripFeatures(vehicleId)
   - Call FeatureToggleService.getAvailableFeatures(vehicle.fuelType)

3. Add endpoint:
   GET /trips/{vehicleId}/available-features
   Returns: AvailableFeaturesDTO

4. Update POST /trips endpoint:
   Enrich response with available features
```

**Status:** 🔴 NOT STARTED

---

## Phase 3: Charging Service (Week 5)

### PR #9: Update Charging Service to Handle EV/Hybrid Only
**Effort:** 2-3 days (40 hours)

**Quick Summary:**
- Add VehicleTypeValidator to check vehicle is EV/Hybrid
- Return 400 error if ICE vehicle tries to charge
- Update ChargingSessionController and RouteOptimizationController

---

### PR #10: Extend Charging Session Analytics
**Effort:** 1-2 days (30 hours)

**Quick Summary:**
- Create ChargingAnalyticsService
- Add endpoint: GET /charging/company/{id}/analytics
- Show station utilization, costs, energy metrics

---

## Phase 4: Maintenance Service (Weeks 5-6)

### PR #11: Extend Maintenance Service for ICE-Specific Services
**Effort:** 3-4 days (70 hours)

**Quick Summary:**
- Create MaintenanceType enum with ICE types (OIL_CHANGE, AIR_FILTER, etc.)
- Create MaintenanceScheduleBuilder that generates schedules per fuel type
- Pre-populate database with ICE and EV maintenance schedules

---

### PR #12: Create Multi-Fuel Cost Tracking for Maintenance
**Effort:** 1-2 days (45 hours)

**Quick Summary:**
- Track maintenance costs by type (EV vs ICE)
- Create MaintenanceCostAnalyticsService
- Show cost breakdown for TCO calculations

---

## Phase 5: Frontend Updates (Weeks 7-8)

### PR #13: Update Vehicle Form Components for Multi-Fuel Selection
**Effort:** 3-4 days (100 hours)

**Copilot Prompt - Short Version:**
```
Update AddVehicle component to support fuel type selection:

1. Add FuelTypeSelector component at top:
   - Radio buttons: Petrol/Diesel (ICE), Electric (EV), Hybrid
   - Icon and color per type

2. Conditional field rendering:
   - Show batteryCapacity + chargerType ONLY if EV/HYBRID
   - Show fuelTankCapacity ONLY if ICE/HYBRID
   - Clear non-applicable fields on fuel type change

3. Form validation:
   - Call validateVehicleForm() per fuel type
   - Show required field errors

4. Update API call:
   - Send fuelType in request payload

Files:
- frontend/src/components/fleet/AddVehicle.tsx (update)
- frontend/src/components/fleet/FuelTypeSelector.tsx (new)
- frontend/src/constants/fuelTypes.ts (new)
```

**Status:** 🔴 NOT STARTED

---

### PR #14: Update Vehicle List and Details Pages for Multi-Fuel Display
**Effort:** 3-4 days (130 hours)

**Copilot Prompt - Short Version:**
```
Update VehicleList and VehicleDetails to show fuel type information:

1. VehicleList updates:
   - Add FuelType column with icon/color
   - Add fuel type filter dropdown
   - Show Battery% for EV/HYBRID, Fuel% for ICE/HYBRID

2. VehicleDetails updates:
   - Fetch availableFeatures from API
   - Create tabs: Overview, Battery (if available), Fuel (if available), Maintenance, Trips
   - Show fuel-type-specific info per tab

3. Create FuelStatusPanel for ICE vehicles:
   - Show fuel level, consumption rate
   - Fuel trend chart

Files:
- frontend/src/components/fleet/VehicleList.tsx
- frontend/src/components/fleet/VehicleDetails.tsx
- frontend/src/components/fleet/FuelStatusPanel.tsx (new)
```

**Status:** 🔴 NOT STARTED

---

### PR #15: Create Charging and Fuel Station Discovery Components
**Effort:** 2-3 days (115 hours)

**Copilot Prompt - Short Version:**
```
Create station discovery component for both charging and fuel stations:

1. StationDiscovery component:
   - Show Charging Stations for EV (always)
   - Show Fuel Stations for ICE (always)
   - Show toggle for Hybrid vehicles

2. StationMap component:
   - Use Mapbox GL
   - Red marker for vehicle
   - Blue markers for nearby stations

3. StationCard component:
   - Station name, distance
   - For charging: ₹/kWh, available chargers
   - For fuel: ₹/liter, availability
   - Action button (Start Charging / Navigate)

Files:
- frontend/src/components/fleet/StationDiscovery.tsx (new)
- frontend/src/components/fleet/StationMap.tsx (new)
- frontend/src/components/fleet/StationCard.tsx (new)
- frontend/src/services/fuelService.ts (new)
```

**Status:** 🔴 NOT STARTED

---

### PR #16: Create Multi-Fuel Dashboard Overview
**Effort:** 2-3 days (100 hours)

**Copilot Prompt - Short Version:**
```
Create dashboard cards for multi-fuel insights:

1. FleetCompositionCard:
   - Pie chart: EV%, ICE%, Hybrid%
   - Total vehicle count

2. CostBreakdownCard:
   - Bar chart: Energy Cost (EV), Fuel Cost (ICE), Maintenance
   - Total cost breakdown

3. MaintenanceAlertsCard:
   - List of upcoming services
   - EV-specific (battery check), ICE-specific (oil change)
   - Days until due

Files:
- frontend/src/components/dashboard/FleetCompositionCard.tsx (new)
- frontend/src/components/dashboard/CostBreakdownCard.tsx (new)
- frontend/src/components/dashboard/MaintenanceAlertsCard.tsx (new)
- frontend/src/pages/DashboardPage.tsx (update)
```

**Status:** 🔴 NOT STARTED

---

## Phase 6: Billing (Week 9)

### PR #17: Implement Multi-Tier Pricing Structure
**Effort:** 2-3 days (110 hours)

**Quick Summary:**
- Create PricingTier entity (BASIC ₹299, EV_PREMIUM ₹699, ENTERPRISE ₹999)
- Create PricingService to assign tiers per vehicle
- Create frontend PricingPage showing tier comparisons

---

### PR #18: Create Invoice Generation and Payment Tracking
**Effort:** 2-3 days (115 hours)

**Quick Summary:**
- Create InvoiceGenerationService (runs monthly)
- Group vehicles by tier, calculate monthly bill
- Create PaymentProcessingService for payment handling
- Update BillingPage to show invoices and payment history

---

## Testing Checklist (2 Weeks)

For each PR, ensure:

```
Unit Tests:
- [ ] Entity model tests
- [ ] Service logic tests
- [ ] DTO mapping tests
- [ ] Validation tests

Integration Tests:
- [ ] API endpoint tests (MockMvc)
- [ ] Database persistence tests
- [ ] Feature flag integration tests

Frontend Tests (if applicable):
- [ ] Component renders correctly
- [ ] Conditional rendering works
- [ ] API calls execute
- [ ] Mobile responsive

Acceptance Criteria:
- [ ] All requirements met
- [ ] No breaking changes
- [ ] Tests passing (> 85% coverage)
- [ ] Swagger/API docs updated
```

---

## How to Submit a PR with Copilot

### Step-by-Step Process:

1. **Create Branch:**
   ```bash
   git checkout -b feature/pr-X-description
   ```

2. **Copy Copilot Prompt:**
   ```
   Take the "Copilot Prompt - Short Version" section from this document
   ```

3. **Paste into Copilot:**
   - Use Claude Code extension
   - Paste prompt with file paths
   - Review and refine suggestions

4. **Implement & Test:**
   - Write/run unit tests
   - Test locally
   - Verify acceptance criteria

5. **Create PR:**
   ```bash
   git add .
   git commit -m "feat: PR #X - [Title]"
   git push origin feature/pr-X-description
   ```

6. **PR Description Template:**
   ```markdown
   ## Summary
   [Copy from migration document]

   ## Changes
   - Backend: [List changes]
   - Frontend: [List changes]
   - Database: [List changes]

   ## Testing
   - [x] Unit tests passed
   - [x] Integration tests passed
   - [x] Acceptance criteria met

   ## Checklist
   - [x] Code follows style guide
   - [x] Tests added/updated
   - [x] Documentation updated
   - [x] No breaking changes
   ```

---

## Success Criteria

✅ **Each PR should have:**
- All acceptance criteria met
- > 85% test coverage
- 0 critical bugs in review
- Clear git history (rebase if needed)
- Updated API docs/Swagger

✅ **Complete Migration Success:**
- All 18 PRs merged
- 100% feature parity with design
- < 8% code regression
- Customer feedback positive

---

## Support & Questions

If Copilot struggles with a PR:
1. Break it into smaller commits
2. Provide more context/examples in prompt
3. Generate one file at a time instead of whole service
4. Use test-driven approach (write tests first)

**Estimated Timeline:**
- **PR Development:** 4-5 days per PR on average
- **With Copilot:** ~2-3 days per PR (60-70% code generation)
- **Total:** 8-10 weeks with full team

---

**Ready to start? Begin with PR #1!**
