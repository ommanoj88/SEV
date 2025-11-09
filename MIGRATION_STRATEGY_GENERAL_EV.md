# MIGRATION STRATEGY: EV-ONLY → GENERAL + EV EXCELLENCE

**Document Status:** Migration Planning Document
**Last Updated:** 2025-11-09
**Strategic Focus:** Pivot from EV-specialized to General Fleet Management with EV as Premium Feature

---

## EXECUTIVE SUMMARY

This document outlines the complete migration path to transform the EV Fleet Management System from **EV-specialized** to **General Fleet Management + EV Excellence Premium Tier**.

### Strategic Outcome
- **Basic Tier (₹299/vehicle/month):** All vehicles (ICE, Hybrid, EV) - General fleet management
- **EV Premium Tier (₹699/vehicle/month):** All features + EV-specific optimization
- **Enterprise Tier (₹999/vehicle/month):** Multi-depot, custom integrations, dedicated support

### Estimated Effort
- **Total PRs:** 18 PRs
- **Total Development Time:** 8-10 weeks
- **Risk Level:** Medium (no breaking changes, backward compatible)
- **Testing Effort:** 3 weeks comprehensive testing

---

## PART 1: TECHNICAL ANALYSIS - WHAT NEEDS TO CHANGE

### Current State (EV-Specific)
```
✗ Vehicle entity assumes EV-only architecture
✗ All features assume battery/charging operations
✗ UI shows EV-specific fields for all vehicles
✗ Billing assumes EV-only pricing
✗ APIs don't differentiate vehicle types
```

### Target State (General + EV Excellence)
```
✓ Vehicle entity supports ICE, EV, HYBRID
✓ Features are conditionally available by vehicle type
✓ UI dynamically shows/hides features based on fuel type
✓ Billing supports multi-tier pricing
✓ APIs include vehicle type filtering and classification
✓ Feature flags control EV feature availability
```

### Impact Analysis

| Component | Current | Required Changes | Impact |
|-----------|---------|------------------|--------|
| **Database** | EV-only schema | Add fuel_type column, new tables for ICE metrics | **Medium** - Additive migrations |
| **Vehicle Service** | EV properties required | Make EV properties optional, add ICE properties | **High** - Core entity changes |
| **Charging Service** | All vehicles | Only EV/Hybrid vehicles | **Low** - Conditional routing |
| **Fleet Service** | Battery-centric telemetry | Multi-fuel telemetry, fuel gauge for ICE | **High** - New telemetry fields |
| **Maintenance Service** | Battery focus | Add fuel-based maintenance | **Medium** - New schedule types |
| **Frontend Components** | All show EV fields | Conditional rendering by vehicle type | **High** - Many component updates |
| **Analytics Service** | EV cost/carbon | Add fuel cost calculations for ICE | **Medium** - New metric calculations |
| **Billing Service** | EV pricing | Multi-tier pricing based on fuel type | **Low** - Pricing table update |

---

## PART 2: DETAILED PR BREAKDOWN - 18 PRs TOTAL

### PHASE 1: DATA MODEL & DATABASE (PRs 1-4)

#### **PR #1: Add Vehicle Fuel Type Support to Database**
**Priority:** CRITICAL (Blocker for all other changes)
**Effort:** 3-5 days
**Files Modified:** ~2 files
**Type:** Database Migration + Entity Update

**What This PR Does:**
```sql
-- Add fuel_type ENUM to vehicles table
-- Existing vehicles default to EV for backward compatibility
-- Create new columns for ICE-specific metrics
-- Create new tables for fuel-based operations
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - `backend/fleet-service/src/main/java/com/evfleet/fleet/model/Vehicle.java`
     - Add `fuelType: FuelType ENUM (ICE, EV, HYBRID)` field
     - Add `fuelTankCapacity: Double` (for ICE vehicles)
     - Add `fuelLevel: Double` (current fuel %)
     - Add `defaultChargerType: String` (for EV - CCS, CHAdeMO, Type2)
     - Keep all existing EV fields (maintain backward compatibility)

   - `backend/fleet-service/src/main/java/com/evfleet/fleet/model/FuelType.java`
     - Create new ENUM: `ICE, EV, HYBRID`

2. **Database - Fleet Service:**
   - Create migration: `V2__add_fuel_type_support.sql`
     ```sql
     ALTER TABLE vehicles ADD COLUMN fuel_type VARCHAR(20) DEFAULT 'EV';
     ALTER TABLE vehicles ADD COLUMN fuel_tank_capacity DOUBLE PRECISION;
     ALTER TABLE vehicles ADD COLUMN fuel_level DOUBLE PRECISION;
     ALTER TABLE vehicles ADD COLUMN default_charger_type VARCHAR(50);
     CREATE INDEX idx_fuel_type ON vehicles(fuel_type);
     ```

   - Create new table: `V3__create_fuel_consumption_table.sql`
     ```sql
     CREATE TABLE fuel_consumption (
         id BIGSERIAL PRIMARY KEY,
         vehicle_id BIGINT NOT NULL,
         trip_id BIGINT,
         fuel_consumed DOUBLE PRECISION,
         fuel_type VARCHAR(20) NOT NULL,
         consumption_rate DOUBLE PRECISION (L/100km),
         timestamp TIMESTAMP NOT NULL,
         created_at TIMESTAMP,
         FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
         INDEX idx_vehicle_trip (vehicle_id, trip_id)
     );
     ```

**Acceptance Criteria:**
- [x] Existing EV vehicles migration (UPDATE vehicles SET fuel_type='EV' WHERE fuel_type IS NULL)
- [x] FuelType ENUM created with ICE, EV, HYBRID
- [x] Backward compatibility maintained (existing endpoints still work)
- [x] Database migrations execute without errors
- [x] New columns indexed for query performance

**Testing:**
- [x] Migration script tested against test database
- [x] Existing vehicle queries still return results
- [x] New columns properly initialized
- [x] Database constraints enforced

**Copilot Guidance:**
"Create a Flyway migration to add fuel_type support to the vehicles table. Add fuel_type as ENUM (ICE, EV, HYBRID) with default 'EV'. Create fuel_consumption table for non-EV vehicles. Update Vehicle entity to include fuelType, fuelTankCapacity, fuelLevel fields. Ensure backward compatibility by making new fields optional."

---

#### **PR #2: Create Feature Flag System for EV Features**
**Priority:** HIGH (Enables conditional logic throughout)
**Effort:** 2-3 days
**Files Modified:** ~8 files
**Type:** Core Infrastructure

**What This PR Does:**
```
Create a centralized feature flag system that controls EV feature availability
based on vehicle fuel type. This enables clean conditional logic without
scattered if-else statements throughout the codebase.
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - Create new entity: `backend/fleet-service/src/main/java/com/evfleet/fleet/model/FeatureToggle.java`
     ```java
     @Entity
     public class FeatureToggle {
         Long id;
         String featureName; // "BATTERY_TRACKING", "CHARGING_SESSION", "RANGE_CALC"
         String applicableVehicleTypes; // "EV,HYBRID"
         Boolean enabled;
         LocalDateTime createdAt;
         LocalDateTime updatedAt;
     }
     ```

   - Create feature flag service: `backend/fleet-service/src/main/java/com/evfleet/fleet/service/FeatureToggleService.java`
     ```java
     public class FeatureToggleService {
         public boolean isFeatureEnabled(String featureName, FuelType vehicleType)
         public List<String> getAvailableFeatures(FuelType vehicleType)
         public void toggleFeature(String featureName, boolean enabled)
     }
     ```

   - Create custom annotation: `backend/fleet-service/src/main/java/com/evfleet/fleet/annotation/RequireFeature.java`
     ```java
     @Target(ElementType.METHOD)
     @Retention(RetentionPolicy.RUNTIME)
     public @interface RequireFeature {
         String value(); // Feature name
         String[] applicableTypes() default {}; // FuelTypes
     }
     ```

2. **Database - Fleet Service:**
   - Create migration: `V4__create_feature_toggles_table.sql`
     ```sql
     CREATE TABLE feature_toggles (
         id BIGSERIAL PRIMARY KEY,
         feature_name VARCHAR(100) UNIQUE NOT NULL,
         applicable_vehicle_types VARCHAR(100) NOT NULL,
         enabled BOOLEAN DEFAULT TRUE,
         created_at TIMESTAMP,
         updated_at TIMESTAMP
     );

     INSERT INTO feature_toggles VALUES
     ('BATTERY_TRACKING', 'EV,HYBRID', true),
     ('CHARGING_SESSION', 'EV,HYBRID', true),
     ('RANGE_OPTIMIZATION', 'EV,HYBRID', true),
     ('FUEL_CONSUMPTION', 'ICE,HYBRID', true),
     ('REGENERATIVE_BRAKING', 'EV,HYBRID', true),
     ('BATTERY_HEALTH_MONITORING', 'EV,HYBRID', true);
     ```

3. **Backend - API Gateway:**
   - Update authentication filter to support feature-based authorization
   - Add feature validation in request interceptor

**Acceptance Criteria:**
- [x] FeatureToggle entity created with proper DB table
- [x] FeatureToggleService implements enable/disable logic
- [x] @RequireFeature annotation created and functional
- [x] Default features pre-populated for all vehicle types
- [x] Feature toggles can be updated via API/admin panel

**Testing:**
- [x] Feature toggle repository tests
- [x] Feature service logic tests
- [x] Annotation interception tests
- [x] End-to-end feature availability tests

**Copilot Guidance:**
"Create a FeatureToggle entity and service that manages feature availability based on vehicle type. Implement a @RequireFeature annotation for methods that require specific vehicle types. Pre-populate the database with EV features (BATTERY_TRACKING, CHARGING_SESSION, etc.) and ICE features (FUEL_CONSUMPTION). Add feature validation to the API Gateway."

---

#### **PR #3: Extend Telemetry Data Model for Multi-Fuel Support**
**Priority:** HIGH (Data collection foundation)
**Effort:** 3-4 days
**Files Modified:** ~6 files
**Type:** Data Model Enhancement

**What This PR Does:**
```
Extend telemetry_data table to capture fuel-specific metrics (fuel level,
fuel consumption rate for ICE) alongside battery metrics (for EV). Create
new telemetry type classification.
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/model/TelemetryData.java`
     ```java
     // Keep existing EV fields (backward compatible)
     private Double batterySoc;
     private Double batteryVoltage;
     private Double batteryTemperature;

     // Add new fuel-specific fields
     private Double fuelLevel; // For ICE vehicles (%)
     private Double fuelConsumptionRate; // L/100km
     private Double fuelType; // Redundant copy for filtering
     private Integer engineRpm; // For ICE
     private Double engineTemperature; // For ICE
     private Double engineLoad; // For ICE
     private Integer engineHours; // For ICE
     ```

   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/model/TelemetryType.java`
     ```java
     public enum TelemetryType {
         EV_BATTERY, // Battery metrics
         ICE_FUEL,   // Fuel metrics
         HYBRID,     // Both
         GENERIC     // Common metrics (speed, location, etc.)
     }
     ```

   - Update repository: `backend/fleet-service/src/main/java/com/evfleet/fleet/repository/TelemetryDataRepository.java`
     ```java
     // Add new query methods
     List<TelemetryData> findByVehicleIdAndFuelLevelBetween(Long vehicleId, Double min, Double max);
     List<TelemetryData> findByVehicleIdAndFuelConsumptionRateGreaterThan(Long vehicleId, Double rate);
     ```

2. **Database - Fleet Service:**
   - Create migration: `V5__extend_telemetry_for_multi_fuel.sql`
     ```sql
     ALTER TABLE telemetry_data ADD COLUMN fuel_level DOUBLE PRECISION;
     ALTER TABLE telemetry_data ADD COLUMN fuel_consumption_rate DOUBLE PRECISION;
     ALTER TABLE telemetry_data ADD COLUMN fuel_type VARCHAR(20);
     ALTER TABLE telemetry_data ADD COLUMN engine_rpm INTEGER;
     ALTER TABLE telemetry_data ADD COLUMN engine_temperature DOUBLE PRECISION;
     ALTER TABLE telemetry_data ADD COLUMN engine_load DOUBLE PRECISION;
     ALTER TABLE telemetry_data ADD COLUMN engine_hours DOUBLE PRECISION;

     CREATE INDEX idx_fuel_level ON telemetry_data(fuel_level);
     CREATE INDEX idx_fuel_consumption ON telemetry_data(fuel_consumption_rate);
     CREATE INDEX idx_engine_rpm ON telemetry_data(engine_rpm);
     ```

3. **Testing Data:**
   - Create test data generator for ICE telemetry

**Acceptance Criteria:**
- [x] TelemetryData updated with fuel-specific fields
- [x] TelemetryType enum created
- [x] Database migration executes successfully
- [x] New fields properly indexed
- [x] Backward compatibility maintained (EV fields still work)

**Testing:**
- [x] Telemetry persistence tests (EV + ICE data)
- [x] Query tests for new fuel fields
- [x] Index performance tests
- [x] Data validation tests

**Copilot Guidance:**
"Extend the TelemetryData entity to include fuel-specific metrics (fuelLevel, fuelConsumptionRate, engineRpm, engineTemperature, etc.) alongside existing battery metrics. Create TelemetryType enum (EV_BATTERY, ICE_FUEL, HYBRID, GENERIC). Add Flyway migration to add these columns to telemetry_data table with proper indexes. Keep backward compatibility for existing EV queries."

---

#### **PR #4: Update Vehicle Service Repository Queries for Multi-Fuel Filtering**
**Priority:** MEDIUM (Supports queries across PRs)
**Effort:** 2-3 days
**Files Modified:** ~4 files
**Type:** Query Enhancement

**What This PR Does:**
```
Add new repository methods to filter vehicles by fuel type, charging status
(EV-only), and mixed fleet queries. This supports both general and EV-specific
views throughout the application.
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/repository/VehicleRepository.java`
     ```java
     // New query methods
     List<Vehicle> findByFuelType(FuelType fuelType);
     List<Vehicle> findByCompanyIdAndFuelType(Long companyId, FuelType fuelType);
     List<Vehicle> findByCompanyIdAndFuelTypeIn(Long companyId, List<FuelType> fuelTypes);
     List<Vehicle> findByFuelTypeAndStatusIn(FuelType fuelType, List<VehicleStatus> statuses);

     // Fleet composition queries
     List<Vehicle> findByCompanyIdOrderByFuelType(Long companyId);

     // Low fuel queries (for ICE vehicles)
     List<Vehicle> findByCompanyIdAndFuelTypeAndFuelLevelLessThan(Long companyId, FuelType fuelType, Double fuelLevel);

     // Charging availability (for EV)
     @Query("SELECT v FROM Vehicle v WHERE v.companyId = ?1 AND v.fuelType IN ('EV', 'HYBRID') AND v.currentBatterySoc < ?2")
     List<Vehicle> findEVVehiclesNeedingCharge(Long companyId, Double threshold);

     // Mixed fleet composition
     @Query("SELECT new map(v.fuelType as fuelType, COUNT(v) as count) FROM Vehicle v WHERE v.companyId = ?1 GROUP BY v.fuelType")
     List<Map<String, Object>> getFleetComposition(Long companyId);
     ```

   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/service/VehicleService.java`
     ```java
     // New service methods
     public List<Vehicle> getVehiclesByFuelType(Long companyId, FuelType fuelType)
     public Map<FuelType, Long> getFleetComposition(Long companyId)
     public List<Vehicle> getLowBatteryVehicles(Long companyId, Double threshold)
     public List<Vehicle> getLowFuelVehicles(Long companyId, Double threshold)
     public List<Vehicle> getMixedFleetSummary(Long companyId)
     ```

2. **Backend - API Updates:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/VehicleController.java`
     ```java
     // New endpoints
     @GetMapping("/company/{companyId}/fuel-type/{fuelType}")
     public ResponseEntity<List<VehicleDTO>> getVehiclesByFuelType(
         @PathVariable Long companyId,
         @PathVariable FuelType fuelType)

     @GetMapping("/company/{companyId}/fleet-composition")
     public ResponseEntity<Map<String, Long>> getFleetComposition(@PathVariable Long companyId)

     @GetMapping("/company/{companyId}/low-fuel")
     public ResponseEntity<List<VehicleDTO>> getLowFuelVehicles(
         @PathVariable Long companyId,
         @RequestParam(defaultValue = "20") Double threshold)
     ```

3. **Testing:**
   - Test data with mix of ICE, EV, Hybrid vehicles
   - Query performance tests for large fleets

**Acceptance Criteria:**
- [x] New repository methods created and tested
- [x] Service methods implement filtering logic
- [x] New API endpoints functional
- [x] Query performance acceptable (< 500ms for 1000 vehicles)
- [x] All existing tests still pass

**Testing:**
- [x] Repository query tests (JPA/SQL)
- [x] Service layer integration tests
- [x] API endpoint tests (MockMvc)
- [x] Performance tests with large datasets

**Copilot Guidance:**
"Add new repository methods to VehicleRepository for filtering by fuelType (findByFuelType, findByCompanyIdAndFuelTypeIn, etc.). Add service methods for fleet composition analysis. Create new API endpoints: GET /vehicles/company/{id}/fuel-type/{type}, GET /vehicles/company/{id}/fleet-composition, GET /vehicles/company/{id}/low-fuel. Include custom @Query annotations for complex queries."

---

### PHASE 2: API ENHANCEMENTS (PRs 5-8)

#### **PR #5: Update Vehicle CRUD APIs to Support Fuel Type**
**Priority:** HIGH (API contract change)
**Effort:** 3-4 days
**Files Modified:** ~8 files
**Type:** API Enhancement

**What This PR Does:**
```
Update all Vehicle DTOs and API endpoints to support fuel type selection,
dynamic field display, and fuel-specific metadata. Create separate DTOs
for request/response to manage optional fields cleanly.
```

**Changes Required:**

1. **Backend - Fleet Service - DTOs:**
   - Create/Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/VehicleCreateRequestDTO.java`
     ```java
     @Data
     @Builder
     public class VehicleCreateRequestDTO {
         @NotBlank
         private String vehicleNumber;

         @NotNull
         private FuelType fuelType; // NEW: REQUIRED FIELD

         @NotBlank
         private String make;

         @NotBlank
         private String model;

         @NotNull
         private Integer year;

         @NotNull
         private VehicleType type;

         // EV-Specific Fields (Conditional)
         private Double batteryCapacity;
         private String defaultChargerType; // CCS, CHAdeMO, Type2

         // ICE-Specific Fields (Conditional)
         private Double fuelTankCapacity;
         private String fuelType;

         @NotEmpty
         @Email
         private String company

Email;
     }
     ```

   - Create/Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/VehicleResponseDTO.java`
     ```java
     @Data
     @Builder
     public class VehicleResponseDTO {
         private Long id;
         private String vehicleNumber;
         private FuelType fuelType;
         private String make;
         private String model;
         private Integer year;

         // Dynamic fields based on fuelType
         private Double batteryCapacity;
         private Double currentBatterySoc;
         private String defaultChargerType;

         private Double fuelTankCapacity;
         private Double currentFuelLevel;

         // Common fields
         private VehicleStatus status;
         private Double latitude;
         private Double longitude;
         private Double totalDistance;

         @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
         private List<String> availableFeatures; // Based on fuelType
     }
     ```

   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/mapper/VehicleMapper.java`
     ```java
     public class VehicleMapper {
         public VehicleResponseDTO toResponseDTO(Vehicle vehicle, List<String> availableFeatures)
         public Vehicle toEntity(VehicleCreateRequestDTO dto)
         public void updateEntity(Vehicle vehicle, VehicleCreateRequestDTO dto)
     }
     ```

2. **Backend - Fleet Service - Controllers:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/VehicleController.java`
     ```java
     @PostMapping
     public ResponseEntity<VehicleResponseDTO> createVehicle(
         @Valid @RequestBody VehicleCreateRequestDTO request,
         @RequestHeader String companyId) {
         // Validate required fields based on fuelType
         if (request.getFuelType() == FuelType.EV || request.getFuelType() == FuelType.HYBRID) {
             validateBatteryCapacity(request);
         }
         if (request.getFuelType() == FuelType.ICE || request.getFuelType() == FuelType.HYBRID) {
             validateFuelTankCapacity(request);
         }
         // Create and return with available features
     }

     @PutMapping("/{id}")
     public ResponseEntity<VehicleResponseDTO> updateVehicle(
         @PathVariable Long id,
         @Valid @RequestBody VehicleCreateRequestDTO request) {
         // Prevent fuel type change for existing vehicles (migration only)
     }

     @GetMapping("/{id}")
     public ResponseEntity<VehicleResponseDTO> getVehicle(@PathVariable Long id) {
         // Return with available features populated
     }
     ```

3. **Validation:**
   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/validation/FuelTypeValidator.java`
     ```java
     @Component
     public class FuelTypeValidator {
         public void validateCreateRequest(VehicleCreateRequestDTO dto)
         public void validateUpdateRequest(Vehicle current, VehicleCreateRequestDTO dto)
     }
     ```

4. **Testing:**
   - Test data fixtures for ICE, EV, Hybrid vehicles
   - API contract tests for all vehicle types

**Acceptance Criteria:**
- [x] VehicleCreateRequestDTO updated with fuelType field
- [x] VehicleResponseDTO includes dynamic fields
- [x] All CRUD endpoints support fuel type operations
- [x] Validation ensures required fields per fuel type
- [x] API documentation updated
- [x] Backward compatibility maintained for existing vehicles

**Testing:**
- [x] Controller integration tests for all vehicle types
- [x] DTO validation tests
- [x] Mapper tests
- [x] API contract tests (OpenAPI compatibility)
- [x] Error handling tests (invalid fuel type combinations)

**Copilot Guidance:**
"Update VehicleController and DTOs to include fuelType in request/response. Create separate validation logic for EV-specific fields (batteryCapacity, chargerType) and ICE-specific fields (fuelTankCapacity). Update Swagger/OpenAPI documentation to show conditional fields. Ensure backward compatibility by making fuelType default to EV for existing vehicles. Add FuelTypeValidator to enforce field requirements per vehicle type."

---

#### **PR #6: Extend Telemetry APIs for Multi-Fuel Metrics**
**Priority:** HIGH (Data ingestion)
**Effort:** 2-3 days
**Files Modified:** ~6 files
**Type:** API Enhancement

**What This PR Does:**
```
Extend telemetry API to accept fuel-specific metrics (fuel level, engine RPM
for ICE) and route data to appropriate processing pipelines. Create separate
DTOs for EV vs ICE telemetry ingestion.
```

**Changes Required:**

1. **Backend - Fleet Service - DTOs:**
   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/TelemetryRequestDTO.java`
     ```java
     @Data
     @Builder
     public class TelemetryRequestDTO {
         @NotNull
         private Long vehicleId;

         @NotNull
         private Long timestamp;

         // Location
         @NotNull
         private Double latitude;

         @NotNull
         private Double longitude;

         private Double altitude;
         private Double heading;
         private Double speed;

         // EV Metrics (conditional)
         private Double batterySoc;
         private Double batteryVoltage;
         private Double batteryTemperature;
         private Double batteryHealth;

         // ICE Metrics (conditional)
         private Double fuelLevel;
         private Integer engineRpm;
         private Double engineTemperature;
         private Double engineLoad;

         // Common
         private Double odometer;
         private Boolean isChargingActive;
         private Boolean isIgnitionOn;
         private String errorCodes;
     }
     ```

   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/TelemetryResponseDTO.java`
     ```java
     @Data
     @Builder
     public class TelemetryResponseDTO {
         private Long id;
         private Long vehicleId;
         private Long timestamp;
         private TelemetryStatus status; // STORED, PROCESSED, ANOMALY
         private String message;
     }
     ```

2. **Backend - Fleet Service - Controllers:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/TelemetryController.java`
     ```java
     @PostMapping
     public ResponseEntity<TelemetryResponseDTO> submitTelemetry(
         @Valid @RequestBody TelemetryRequestDTO request) {
         // Validate fields based on vehicle fuel type
         Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
         validateTelemetryByFuelType(request, vehicle.getFuelType());

         // Route to appropriate processor
         TelemetryProcessingResult result = telemetryProcessingService.process(request, vehicle);
         return ResponseEntity.ok(mapToResponse(result));
     }

     @PostMapping("/batch")
     public ResponseEntity<List<TelemetryResponseDTO>> submitBatchTelemetry(
         @Valid @RequestBody List<TelemetryRequestDTO> requests) {
         // Batch processing with fuel type validation
     }
     ```

3. **Backend - Processing Service:**
   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/service/TelemetryProcessingService.java`
     ```java
     @Service
     public class TelemetryProcessingService {
         public TelemetryProcessingResult process(TelemetryRequestDTO request, Vehicle vehicle) {
             if (vehicle.getFuelType() == FuelType.EV) {
                 return processEVTelemetry(request);
             } else if (vehicle.getFuelType() == FuelType.ICE) {
                 return processICETelemetry(request);
             } else {
                 return processHybridTelemetry(request);
             }
         }
     }
     ```

4. **Validation:**
   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/validation/TelemetryValidator.java`
     ```java
     public class TelemetryValidator {
         public void validateEVTelemetry(TelemetryRequestDTO request)
         public void validateICETelemetry(TelemetryRequestDTO request)
         public void validateHybridTelemetry(TelemetryRequestDTO request)
     }
     ```

**Acceptance Criteria:**
- [x] TelemetryRequestDTO supports both EV and ICE metrics
- [x] API endpoint accepts multi-fuel telemetry
- [x] Validation enforces fuel-type-specific fields
- [x] Processing routes data to correct handler
- [x] Batch telemetry submission works
- [x] Performance: < 100ms per telemetry submission

**Testing:**
- [x] EV telemetry submission tests
- [x] ICE telemetry submission tests
- [x] Hybrid telemetry submission tests
- [x] Batch submission tests
- [x] Validation error tests
- [x] Performance/load tests

**Copilot Guidance:**
"Update TelemetryController to accept multi-fuel telemetry data. Create separate validation for EV metrics (batterySoc, batteryVoltage) and ICE metrics (fuelLevel, engineRpm). Create TelemetryProcessingService that routes telemetry to appropriate handlers based on vehicle.fuelType. Ensure batch telemetry endpoint supports mixed vehicle types. Add proper error handling for missing required fields per vehicle type."

---

#### **PR #7: Create Multi-Fuel Trip Analytics Endpoints**
**Priority:** MEDIUM (Analytics enablement)
**Effort:** 2-3 days
**Files Modified:** ~6 files
**Type:** API Enhancement

**What This PR Does:**
```
Extend Trip and Analytics APIs to handle multi-fuel calculations:
- Energy cost for EV (₹/kWh)
- Fuel cost for ICE (₹/liter)
- Combined fleet analytics
- Carbon footprint calculations for both
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/model/Trip.java`
     ```java
     // Add new fields
     private Double energyCost; // For EV (rupees)
     private Double fuelCost; // For ICE (rupees)
     private Double totalOperatingCost; // EV + ICE
     private Double carbonFootprint; // kg CO2
     private Double averageFuelConsumption; // For ICE (L/100km)
     ```

   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/TripAnalyticsDTO.java`
     ```java
     @Data
     public class TripAnalyticsDTO {
         private Long tripId;
         private Long vehicleId;
         private FuelType vehicleType;

         // Energy metrics (EV)
         private Double energyConsumed; // kWh
         private Double energyEfficiency; // km/kWh
         private Double energyCost;

         // Fuel metrics (ICE)
         private Double fuelConsumed; // Liters
         private Double fuelEfficiency; // km/L
         private Double fuelCost;

         // Common
         private Double distance;
         private Long durationMinutes;
         private Double carbonFootprint;
         private Double costPerKm;
         private EfficiencyRating rating; // EXCELLENT, GOOD, FAIR, POOR
     }
     ```

2. **Backend - Analytics Service:**
   - Create: `backend/analytics-service/src/main/java/com/evfleet/analytics/service/MultiFleetAnalyticsService.java`
     ```java
     @Service
     public class MultiFleetAnalyticsService {
         // Get fleet composition and costs
         public FleetAnalyticsSummaryDTO getFleetAnalytics(Long companyId, LocalDate startDate, LocalDate endDate)

         // Calculate blended costs
         public CostBreakdownDTO getCostBreakdown(Long companyId) {
             // EV cost + ICE cost breakdown
         }

         // Carbon footprint (both vehicle types)
         public CarbonFootprintDTO getCarbonFootprint(Long companyId)
     }
     ```

   - Create: `backend/analytics-service/src/main/java/com/evfleet/analytics/calculator/EVCostCalculator.java`
     ```java
     public class EVCostCalculator {
         public EVCostMetrics calculateCost(Trip trip, Double energyConsumedKwh, Double ratePerKwh)
         public Double calculateCarbonFootprint(Double energyConsumedKwh)
     }
     ```

   - Create: `backend/analytics-service/src/main/java/com/evfleet/analytics/calculator/ICECostCalculator.java`
     ```java
     public class ICECostCalculator {
         public ICECostMetrics calculateCost(Trip trip, Double fuelConsumedLiters, Double ratePerLiter)
         public Double calculateCarbonFootprint(Double fuelConsumedLiters, String fuelType)
     }
     ```

3. **Backend - API Controller:**
   - Create: `backend/analytics-service/src/main/java/com/evfleet/analytics/controller/MultiFleetAnalyticsController.java`
     ```java
     @GetMapping("/company/{companyId}/fleet-summary")
     public ResponseEntity<FleetAnalyticsSummaryDTO> getFleetSummary(
         @PathVariable Long companyId,
         @RequestParam LocalDate startDate,
         @RequestParam LocalDate endDate)

     @GetMapping("/company/{companyId}/cost-breakdown")
     public ResponseEntity<CostBreakdownDTO> getCostBreakdown(@PathVariable Long companyId)

     @GetMapping("/company/{companyId}/trip-analytics/{tripId}")
     public ResponseEntity<TripAnalyticsDTO> getTripAnalytics(@PathVariable Long tripId)
     ```

**Acceptance Criteria:**
- [x] Trip entity includes fuel/energy cost fields
- [x] Multi-fuel cost calculations implemented
- [x] Analytics endpoints support fleet composition queries
- [x] Carbon footprint calculations for both fuel types
- [x] API returns proper DTOs with all metrics
- [x] Calculations validated against manual calculations

**Testing:**
- [x] Cost calculator tests (EV and ICE)
- [x] Analytics aggregation tests
- [x] API endpoint tests
- [x] Carbon footprint calculation tests
- [x] Edge case tests (mixed fleets, zero consumption)

**Copilot Guidance:**
"Create EVCostCalculator and ICECostCalculator classes with separate cost calculation logic. Update Trip entity to include energyCost, fuelCost, totalOperatingCost, and carbonFootprint fields. Create MultiFleetAnalyticsService with methods for fleet summary, cost breakdown, and carbon metrics. Add new endpoints: GET /analytics/company/{id}/fleet-summary, GET /analytics/company/{id}/cost-breakdown. Ensure all calculations handle both vehicle types correctly."

---

#### **PR #8: Conditional Feature Availability in Trip APIs**
**Priority:** MEDIUM (Feature gating)
**Effort:** 1-2 days
**Files Modified:** ~4 files
**Type:** API Enhancement

**What This PR Does:**
```
Add feature availability validation to Trip endpoints so that features like
"range optimization", "charging stop planning" only appear for EV vehicles,
while "fuel stop planning" only appears for ICE vehicles.
```

**Changes Required:**

1. **Backend - Fleet Service:**
   - Create: `backend/fleet-service/src/main/java/com/evfleet/fleet/dto/AvailableFeaturesDTO.java`
     ```java
     @Data
     @Builder
     public class AvailableFeaturesDTO {
         private List<String> tripFeatures; // Based on vehicle fuel type
         private List<String> maintenanceFeatures;
         private List<String> analyticsFeatures;
     }
     ```

   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/service/TripService.java`
     ```java
     public AvailableFeaturesDTO getAvailableTripFeatures(Long vehicleId) {
         Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
         List<String> features = featureToggleService.getAvailableFeatures(vehicle.getFuelType());
         return filterTripFeatures(features);
     }
     ```

   - Update: `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/TripController.java`
     ```java
     @PostMapping("/{vehicleId}/available-features")
     public ResponseEntity<AvailableFeaturesDTO> getAvailableFeatures(@PathVariable Long vehicleId)

     @PostMapping
     public ResponseEntity<TripDTO> startTrip(@Valid @RequestBody TripStartRequestDTO request) {
         Trip trip = tripService.startTrip(request);
         Trip enrichedTrip = enrichWithAvailableFeatures(trip);
         return ResponseEntity.ok(mapToDTO(enrichedTrip));
     }
     ```

2. **Frontend Updates (Preliminary):**
   - Consumer will call `/trips/{vehicleId}/available-features` before starting trip
   - Only show UI controls for available features

**Acceptance Criteria:**
- [x] Feature availability DTO created
- [x] Trip service checks feature availability
- [x] API endpoint returns available features per vehicle type
- [x] Trip start doesn't expose unavailable features
- [x] Filtering works for all vehicle types

**Testing:**
- [x] Feature availability tests per vehicle type
- [x] API endpoint tests for all types
- [x] Integration tests with feature toggle system

**Copilot Guidance:**
"Create AvailableFeaturesDTO that lists features available for each vehicle type. Update TripService.startTrip() to enrich response with available features. Add new endpoint GET /trips/{vehicleId}/available-features that returns features specific to that vehicle's fuel type. Integrate with FeatureToggleService to get the correct feature set. Ensure charging-related features only appear for EV/HYBRID vehicles."

---

### PHASE 3: CHARGING SERVICE ENHANCEMENTS (PRs 9-10)

#### **PR #9: Update Charging Service to Handle EV/Hybrid Vehicles Only**
**Priority:** MEDIUM (Feature refinement)
**Effort:** 2-3 days
**Files Modified:** ~8 files
**Type:** Service Enhancement

**What This PR Does:**
```
Add validation to Charging Service to ensure charging operations only work
with EV and Hybrid vehicles. Add proper error responses for ICE vehicles.
```

**Changes Required:**

1. **Backend - Charging Service:**
   - Create: `backend/charging-service/src/main/java/com/evfleet/charging/validation/VehicleTypeValidator.java`
     ```java
     @Component
     public class VehicleTypeValidator {
         public void validateEVVehicle(Long vehicleId) throws NotAnEVVehicleException
         public void validateChargingCapability(Vehicle vehicle) throws NotChargingCapableException
     }
     ```

   - Create Exception: `backend/charging-service/src/main/java/com/evfleet/charging/exception/NotAnEVVehicleException.java`
     ```java
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public class NotAnEVVehicleException extends RuntimeException {
         public NotAnEVVehicleException(String vehicleId, FuelType fuelType) {
             super("Vehicle " + vehicleId + " has fuel type " + fuelType + " and does not support charging");
         }
     }
     ```

   - Update: `backend/charging-service/src/main/java/com/evfleet/charging/controller/ChargingSessionController.java`
     ```java
     @PostMapping
     public ResponseEntity<ChargingSessionDTO> startChargingSession(
         @Valid @RequestBody StartChargingRequestDTO request) {

         // NEW: Validate vehicle is EV or Hybrid
         vehicleTypeValidator.validateEVVehicle(request.getVehicleId());

         ChargingSession session = chargingSessionService.startSession(request);
         return ResponseEntity.ok(mapToDTO(session));
     }
     ```

   - Update: `backend/charging-service/src/main/java/com/evfleet/charging/controller/RouteOptimizationController.java`
     ```java
     @PostMapping
     public ResponseEntity<OptimizedRouteDTO> optimizeRoute(
         @Valid @RequestBody OptimizeRouteRequestDTO request) {

         // NEW: Validate vehicle is EV
         vehicleTypeValidator.validateEVVehicle(request.getVehicleId());

         OptimizedRoute route = routeOptimizationService.optimize(request);
         return ResponseEntity.ok(mapToDTO(route));
     }
     ```

2. **Error Handling:**
   - Create: `backend/charging-service/src/main/java/com/evfleet/charging/exception/ChargingExceptionHandler.java`
     ```java
     @ExceptionHandler(NotAnEVVehicleException.class)
     public ResponseEntity<ErrorResponse> handleNotEVVehicle(NotAnEVVehicleException ex) {
         return ResponseEntity.badRequest().body(ErrorResponse.builder()
             .code("VEHICLE_NOT_EV")
             .message(ex.getMessage())
             .suggestion("Only EV and Hybrid vehicles support charging operations")
             .build());
     }
     ```

3. **Documentation:**
   - Update OpenAPI/Swagger docs to clarify EV-only endpoints

**Acceptance Criteria:**
- [x] Charging operations reject ICE vehicles with proper error
- [x] Error response includes helpful message
- [x] EV/Hybrid vehicles work normally
- [x] Exception handling integrated
- [x] API documentation updated
- [x] Error codes standardized

**Testing:**
- [x] Test starting charging for EV (success)
- [x] Test starting charging for ICE (failure with proper error)
- [x] Test route optimization for Hybrid (success)
- [x] Test route optimization for ICE (failure)
- [x] Error response format tests

**Copilot Guidance:**
"Create VehicleTypeValidator in ChargingService that calls FleetService to get vehicle details and validates fuel type. Add NotAnEVVehicleException that returns HTTP 400 with clear message. Update ChargingSessionController.startChargingSession() to call validator.validateEVVehicle() before processing. Do same for RouteOptimization. Add exception handler in ChargingExceptionHandler. Include 'suggestion' field in error response with friendly message about vehicle compatibility."

---

#### **PR #10: Extend Charging Session Analytics for Better UX**
**Priority:** LOW (Enhancement)
**Effort:** 1-2 days
**Files Modified:** ~3 files
**Type:** Service Enhancement

**What This PR Does:**
```
Add charging session analytics endpoint to show:
- Cost per charging session
- Average charging time per station
- Availability trends
- Station utilization metrics
```

**Changes Required:**

1. **Backend - Charging Service:**
   - Create: `backend/charging-service/src/main/java/com/evfleet/charging/dto/ChargingAnalyticsDTO.java`
     ```java
     @Data
     public class ChargingAnalyticsDTO {
         private List<StationUtilizationDTO> stationUtilization;
         private List<ChargingCostTrendDTO> costTrends;
         private Double averageChargingTime;
         private Double averageCostPerSession;
         private Double totalEnergyCharged;
         private Double totalChargingCost;
     }
     ```

   - Create: `backend/charging-service/src/main/java/com/evfleet/charging/service/ChargingAnalyticsService.java`
     ```java
     @Service
     public class ChargingAnalyticsService {
         public ChargingAnalyticsDTO getCompanyChargingAnalytics(Long companyId, LocalDate startDate, LocalDate endDate)
         public StationUtilizationDTO getStationUtilization(Long stationId)
     }
     ```

   - Create: `backend/charging-service/src/main/java/com/evfleet/charging/controller/ChargingAnalyticsController.java`
     ```java
     @GetMapping("/company/{companyId}/analytics")
     public ResponseEntity<ChargingAnalyticsDTO> getChargingAnalytics(
         @PathVariable Long companyId,
         @RequestParam LocalDate startDate,
         @RequestParam LocalDate endDate)
     ```

**Acceptance Criteria:**
- [x] Analytics service calculates metrics correctly
- [x] API endpoint returns analytics data
- [x] Performance acceptable for large datasets

**Testing:**
- [x] Analytics calculation tests
- [x] API endpoint tests

**Copilot Guidance:**
"Create ChargingAnalyticsService with methods to calculate: totalEnergyCharged, totalChargingCost, averageChargingTime, averageCostPerSession. Create StationUtilizationDTO showing how many times each station was used. Add endpoint GET /charging/company/{id}/analytics?startDate=X&endDate=Y returning ChargingAnalyticsDTO with all metrics aggregated by date range."

---

### PHASE 4: MAINTENANCE SERVICE UPDATES (PRs 11-12)

#### **PR #11: Extend Maintenance Service for ICE-Specific Services**
**Priority:** HIGH (Multi-fuel support)
**Effort:** 3-4 days
**Files Modified:** ~10 files
**Type:** Service Enhancement

**What This PR Does:**
```
Add ICE-specific maintenance schedules and tracking:
- Oil changes (every 5000-10000 km)
- Filter replacements (air, cabin, fuel)
- Transmission fluid (every 40000-60000 km)
- Brake fluid (every 2 years)
- Coolant (every 3-5 years)

Keep EV-specific services (battery monitoring, thermal management).
```

**Changes Required:**

1. **Backend - Maintenance Service:**
   - Create: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/model/MaintenanceType.java`
     ```java
     public enum MaintenanceType {
         // Common
         TIRE_ROTATION,
         BRAKE_SERVICE,
         ALIGNMENT,

         // EV-Specific
         BATTERY_HEALTH_CHECK,
         THERMAL_SYSTEM_CHECK,
         REGENERATIVE_BRAKING_CHECK,
         MOTOR_INSPECTION,

         // ICE-Specific
         OIL_CHANGE,
         AIR_FILTER_REPLACEMENT,
         FUEL_FILTER_REPLACEMENT,
         CABIN_FILTER_REPLACEMENT,
         TRANSMISSION_FLUID_CHECK,
         COOLANT_FLUSH,
         SPARK_PLUG_REPLACEMENT,
         ENGINE_TUNE_UP,

         // Hybrid
         BATTERY_SYSTEM_CHECK,
         ENGINE_OIL_CHANGE
     }
     ```

   - Update: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/model/MaintenanceSchedule.java`
     ```java
     // Add new fields
     private FuelType applicableVehicleType; // ICE, EV, HYBRID, ALL
     private MaintenanceType maintenanceType;
     private Integer frequencyKm;
     private Integer frequencyMonths;
     private Double estimatedCost;
     private String partsRequired; // JSON list
     ```

   - Create: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/service/MaintenanceScheduleBuilder.java`
     ```java
     @Service
     public class MaintenanceScheduleBuilder {
         public List<MaintenanceSchedule> buildSchedulesForVehicle(Vehicle vehicle) {
             if (vehicle.getFuelType() == FuelType.EV) {
                 return buildEVSchedules();
             } else if (vehicle.getFuelType() == FuelType.ICE) {
                 return buildICESchedules();
             } else {
                 return buildHybridSchedules();
             }
         }

         private List<MaintenanceSchedule> buildEVSchedules()
         private List<MaintenanceSchedule> buildICESchedules()
         private List<MaintenanceSchedule> buildHybridSchedules()
     }
     ```

2. **Database - Maintenance Service:**
   - Create migration: `V1__add_ice_maintenance_schedules.sql`
     ```sql
     ALTER TABLE maintenance_schedules ADD COLUMN applicable_vehicle_type VARCHAR(20);
     ALTER TABLE maintenance_schedules ADD COLUMN maintenance_type VARCHAR(100);
     ALTER TABLE maintenance_schedules ADD COLUMN frequency_km INTEGER;
     ALTER TABLE maintenance_schedules ADD COLUMN estimated_cost DOUBLE PRECISION;
     ALTER TABLE maintenance_schedules ADD COLUMN parts_required TEXT;

     -- Pre-populate ICE maintenance schedules
     INSERT INTO maintenance_schedules VALUES
     ('OIL_CHANGE', 'ICE', 5000, 6, 500),
     ('AIR_FILTER_REPLACEMENT', 'ICE', 10000, 12, 300),
     ...
     ```

3. **Backend - API:**
   - Update: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/controller/MaintenanceController.java`
     ```java
     @PostMapping("/vehicle/{vehicleId}/generate-schedule")
     public ResponseEntity<List<MaintenanceScheduleDTO>> generateSchedule(@PathVariable Long vehicleId) {
         // Calls MaintenanceScheduleBuilder to create appropriate schedules
     }
     ```

**Acceptance Criteria:**
- [x] ICE maintenance types defined (oil change, filters, etc.)
- [x] MaintenanceScheduleBuilder creates correct schedules per vehicle type
- [x] Database migration adds new columns and pre-populates schedules
- [x] API endpoint generates schedules for new vehicles
- [x] Existing EV schedules still work

**Testing:**
- [x] Schedule builder tests for EV, ICE, Hybrid
- [x] API endpoint tests
- [x] Database migration tests

**Copilot Guidance:**
"Create MaintenanceType enum with ICE-specific values (OIL_CHANGE, AIR_FILTER, TRANSMISSION_FLUID_CHECK, etc.), EV-specific (BATTERY_HEALTH_CHECK, THERMAL_SYSTEM), and common (TIRE_ROTATION, BRAKE_SERVICE). Update MaintenanceSchedule entity to include applicableVehicleType and maintenanceType. Create MaintenanceScheduleBuilder service that returns appropriate schedules based on vehicle.fuelType. Add Flyway migration to add columns and pre-populate schedules for ICE vehicles. Add endpoint POST /maintenance/vehicle/{id}/generate-schedule that auto-generates schedules."

---

#### **PR #12: Create Multi-Fuel Cost Tracking for Maintenance**
**Priority:** MEDIUM (Cost management)
**Effort:** 1-2 days
**Files Modified:** ~4 files
**Type:** Service Enhancement

**What This PR Does:**
```
Track maintenance costs separately for EV (battery, thermal) vs ICE (oil, filters)
to provide accurate TCO (Total Cost of Ownership) analysis.
```

**Changes Required:**

1. **Backend - Maintenance Service:**
   - Update: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/model/ServiceRecord.java`
     ```java
     // Add tracking fields
     private FuelType vehicleType;
     private MaintenanceType maintenanceType;
     private Double costEV; // Cost specific to EV maintenance
     private Double costICE; // Cost specific to ICE maintenance
     private Double totalCost;
     ```

   - Create: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/dto/MaintenanceCostAnalyticsDTO.java`
     ```java
     @Data
     public class MaintenanceCostAnalyticsDTO {
         private Double totalEVMaintenanceCost;
         private Double totalICEMaintenanceCost;
         private Double totalHybridMaintenanceCost;
         private Map<MaintenanceType, Double> costByType;
         private Double averageCostPerService;
     }
     ```

   - Create: `backend/maintenance-service/src/main/java/com/evfleet/maintenance/service/MaintenanceCostAnalyticsService.java`
     ```java
     @Service
     public class MaintenanceCostAnalyticsService {
         public MaintenanceCostAnalyticsDTO getCompanyMaintenance Costs(Long companyId)
         public MaintenanceCostAnalyticsDTO getVehicleMaintenance Costs(Long vehicleId)
     }
     ```

2. **API:**
   - Add endpoints for cost analytics

**Acceptance Criteria:**
- [x] Cost tracking fields added to ServiceRecord
- [x] Analytics service calculates costs correctly
- [x] API returns cost breakdown by fuel type

**Testing:**
- [x] Cost calculation tests
- [x] Analytics aggregation tests

**Copilot Guidance:**
"Update ServiceRecord to track FuelType and MaintenanceType for accurate cost categorization. Create MaintenanceCostAnalyticsDTO showing totalEVMaintenanceCost, totalICEMaintenanceCost, and costByType breakdown. Add MaintenanceCostAnalyticsService with methods getCompanyMaintenanceCosts(companyId) and getVehicleMaintenanceCosts(vehicleId). These are used for TCO calculations."

---

### PHASE 5: FRONTEND UPDATES (PRs 13-16)

#### **PR #13: Update Vehicle Form Components for Multi-Fuel Selection**
**Priority:** HIGH (Critical UX)
**Effort:** 3-4 days
**Files Modified:** ~10 files
**Type:** Frontend Feature

**What This PR Does:**
```
Update AddVehicle.tsx and EditVehicle.tsx to:
1. Show fuel type selector (ICE, EV, Hybrid)
2. Dynamically show/hide EV fields (battery, charger type)
3. Dynamically show/hide ICE fields (tank capacity, fuel type)
4. Validate field presence based on fuel type
```

**Changes Required:**

1. **Frontend - Components:**
   - Update: `frontend/src/components/fleet/AddVehicle.tsx`
     ```typescript
     const AddVehicle: React.FC = () => {
         const [formData, setFormData] = useState({
             vehicleNumber: '',
             fuelType: 'EV', // NEW: Fuel type selector
             make: '',
             model: '',
             year: '',
             batteryCapacity: '',  // Show if EV or HYBRID
             defaultChargerType: '', // Show if EV or HYBRID
             fuelTankCapacity: '', // Show if ICE or HYBRID
             color: '',
             vin: ''
         });

         const [selectedFuelType, setSelectedFuelType] = useState<'ICE' | 'EV' | 'HYBRID'>('EV');

         const handleFuelTypeChange = (newType: string) => {
             setSelectedFuelType(newType as any);
             // Clear irrelevant fields
             if (newType === 'ICE') {
                 setFormData(prev => ({
                     ...prev,
                     batteryCapacity: '',
                     defaultChargerType: ''
                 }));
             }
             if (newType === 'EV') {
                 setFormData(prev => ({
                     ...prev,
                     fuelTankCapacity: ''
                 }));
             }
         };

         return (
             <Box>
                 {/* Fuel Type Selector */}
                 <FormControl fullWidth margin="normal">
                     <InputLabel>Fuel Type</InputLabel>
                     <Select
                         value={selectedFuelType}
                         onChange={(e) => handleFuelTypeChange(e.target.value)}
                     >
                         <MenuItem value="ICE">Internal Combustion Engine</MenuItem>
                         <MenuItem value="EV">Electric Vehicle</MenuItem>
                         <MenuItem value="HYBRID">Hybrid</MenuItem>
                     </Select>
                 </FormControl>

                 {/* Common Fields */}
                 <TextField
                     fullWidth
                     label="Vehicle Number"
                     name="vehicleNumber"
                     value={formData.vehicleNumber}
                     onChange={handleInputChange}
                     margin="normal"
                     required
                 />

                 {/* EV-Specific Fields */}
                 {(selectedFuelType === 'EV' || selectedFuelType === 'HYBRID') && (
                     <>
                         <TextField
                             fullWidth
                             label="Battery Capacity (kWh)"
                             name="batteryCapacity"
                             type="number"
                             value={formData.batteryCapacity}
                             onChange={handleInputChange}
                             margin="normal"
                             required={selectedFuelType !== 'ICE'}
                         />
                         <FormControl fullWidth margin="normal">
                             <InputLabel>Charger Type</InputLabel>
                             <Select
                                 value={formData.defaultChargerType}
                                 name="defaultChargerType"
                             >
                                 <MenuItem value="CCS">CCS</MenuItem>
                                 <MenuItem value="CHAdeMO">CHAdeMO</MenuItem>
                                 <MenuItem value="Type2">Type 2</MenuItem>
                             </Select>
                         </FormControl>
                     </>
                 )}

                 {/* ICE-Specific Fields */}
                 {(selectedFuelType === 'ICE' || selectedFuelType === 'HYBRID') && (
                     <TextField
                         fullWidth
                         label="Fuel Tank Capacity (Liters)"
                         name="fuelTankCapacity"
                         type="number"
                         value={formData.fuelTankCapacity}
                         onChange={handleInputChange}
                         margin="normal"
                         required={selectedFuelType !== 'EV'}
                     />
                 )}

                 {/* Submit */}
                 <Button
                     variant="contained"
                     onClick={handleSubmit}
                     fullWidth
                     sx={{ mt: 2 }}
                 >
                     Create Vehicle
                 </Button>
             </Box>
         );
     };
     ```

   - Create: `frontend/src/components/fleet/FuelTypeSelector.tsx` (Reusable component)
     ```typescript
     interface FuelTypeSelectorProps {
         value: 'ICE' | 'EV' | 'HYBRID';
         onChange: (type: string) => void;
     }

     export const FuelTypeSelector: React.FC<FuelTypeSelectorProps> = ({value, onChange}) => (
         <FormControl fullWidth>
             <RadioGroup value={value} onChange={(e) => onChange(e.target.value)}>
                 <FormControlLabel value="ICE" control={<Radio />} label="Petrol/Diesel" />
                 <FormControlLabel value="EV" control={<Radio />} label="Electric Vehicle" />
                 <FormControlLabel value="HYBRID" control={<Radio />} label="Hybrid" />
             </RadioGroup>
         </FormControl>
     );
     ```

2. **Frontend - Services:**
   - Update: `frontend/src/services/vehicleService.ts`
     ```typescript
     // Add validation helper
     export const validateVehicleForm = (data: any): string[] => {
         const errors: string[] = [];

         if (data.fuelType === 'EV' || data.fuelType === 'HYBRID') {
             if (!data.batteryCapacity) errors.push('Battery capacity required for EV');
             if (!data.defaultChargerType) errors.push('Charger type required for EV');
         }

         if (data.fuelType === 'ICE' || data.fuelType === 'HYBRID') {
             if (!data.fuelTankCapacity) errors.push('Tank capacity required for ICE');
         }

         return errors;
     };
     ```

**Acceptance Criteria:**
- [x] Fuel type selector shown prominently
- [x] EV fields hidden for ICE vehicles
- [x] ICE fields hidden for EV vehicles
- [x] Hybrid shows both field sets
- [x] Form validation enforces required fields per type
- [x] Responsive design on mobile
- [x] Clear UI with helpful labels

**Testing:**
- [x] Component renders correctly for each fuel type
- [x] Field visibility toggles properly
- [x] Validation works (test missing EV fields, missing ICE fields)
- [x] Form submission (success + error cases)
- [x] Mobile responsiveness tests

**Copilot Guidance:**
"Update AddVehicle component to include FuelTypeSelector at top. Use React state to track selectedFuelType. Conditionally render battery/charger fields for EV/HYBRID, fuel tank field for ICE/HYBRID. Update form validation to check required fields based on fuelType. Call vehicleService.validateVehicleForm() before submission. Add visual indicators (icons, colors) for each fuel type. Create reusable FuelTypeSelector component with radio buttons."

---

#### **PR #14: Update Vehicle List and Details Pages for Multi-Fuel Display**
**Priority:** HIGH (Core UX)
**Effort:** 3-4 days
**Files Modified:** ~8 files
**Type:** Frontend Feature

**What This PR Does:**
```
Update VehicleList and VehicleDetails pages to:
1. Show fuel type in list (with icon/color coding)
2. Show relevant metrics based on fuel type
3. Add fuel type filtering to list
4. Display EV or ICE-specific tabs/sections
```

**Changes Required:**

1. **Frontend - Components:**
   - Update: `frontend/src/components/fleet/VehicleList.tsx`
     ```typescript
     const VehicleList: React.FC = () => {
         const [filterFuelType, setFilterFuelType] = useState<'ALL' | 'ICE' | 'EV' | 'HYBRID'>('ALL');

         // Filter data
         const filteredVehicles = useMemo(() => {
             let result = vehicles;
             if (filterFuelType !== 'ALL') {
                 result = result.filter(v => v.fuelType === filterFuelType);
             }
             return result;
         }, [vehicles, filterFuelType]);

         const columns = [
             {
                 field: 'vehicleNumber',
                 headerName: 'Vehicle Number',
                 width: 150,
                 renderCell: (params) => (
                     <Box sx={{ display: 'flex', alignItems: 'center' }}>
                         {getFuelTypeIcon(params.row.fuelType)}
                         <span style={{ marginLeft: 8 }}>{params.value}</span>
                     </Box>
                 )
             },
             {
                 field: 'fuelType',
                 headerName: 'Type',
                 width: 100,
                 renderCell: (params) => (
                     <Chip
                         label={params.value}
                         color={getFuelTypeColor(params.value)}
                         size="small"
                     />
                 )
             },
             {
                 field: 'make',
                 headerName: 'Make',
                 width: 120
             },
             // Conditional columns based on filter
             ...(filterFuelType === 'ALL' || filterFuelType === 'EV' || filterFuelType === 'HYBRID' ? [
                 {
                     field: 'currentBatterySoc',
                     headerName: 'Battery %',
                     width: 100,
                     renderCell: (params) => (
                         <LinearProgress
                             variant="determinate"
                             value={params.value}
                             sx={{ width: '100%' }}
                         />
                     )
                 }
             ] : []),
             ...(filterFuelType === 'ALL' || filterFuelType === 'ICE' || filterFuelType === 'HYBRID' ? [
                 {
                     field: 'currentFuelLevel',
                     headerName: 'Fuel %',
                     width: 100,
                     renderCell: (params) => (
                         <LinearProgress
                             variant="determinate"
                             value={params.value}
                             sx={{ width: '100%' }}
                         />
                     )
                 }
             ] : []),
             {
                 field: 'status',
                 headerName: 'Status',
                 width: 100,
                 renderCell: (params) => (
                     <StatusBadge status={params.value} />
                 )
             }
         ];

         return (
             <Box>
                 {/* Filters */}
                 <Box sx={{ mb: 2, display: 'flex', gap: 2 }}>
                     <FormControl sx={{ minWidth: 200 }}>
                         <InputLabel>Fuel Type</InputLabel>
                         <Select
                             value={filterFuelType}
                             onChange={(e) => setFilterFuelType(e.target.value as any)}
                         >
                             <MenuItem value="ALL">All Types</MenuItem>
                             <MenuItem value="ICE">Internal Combustion</MenuItem>
                             <MenuItem value="EV">Electric</MenuItem>
                             <MenuItem value="HYBRID">Hybrid</MenuItem>
                         </Select>
                     </FormControl>
                 </Box>

                 {/* Table */}
                 <DataGridPro columns={columns} rows={filteredVehicles} ... />
             </Box>
         );
     };

     const getFuelTypeIcon = (fuelType: string) => {
         switch (fuelType) {
             case 'EV': return <ElectricBoltIcon sx={{ color: 'green' }} />;
             case 'ICE': return <LocalGasStationIcon sx={{ color: 'orange' }} />;
             case 'HYBRID': return <HandshakeIcon sx={{ color: 'blue' }} />;
         }
     };

     const getFuelTypeColor = (fuelType: string): 'success' | 'warning' | 'info' => {
         switch (fuelType) {
             case 'EV': return 'success';
             case 'ICE': return 'warning';
             case 'HYBRID': return 'info';
             default: return 'info';
         }
     };
     ```

   - Update: `frontend/src/components/fleet/VehicleDetails.tsx`
     ```typescript
     const VehicleDetails: React.FC<{vehicleId: string}> = ({ vehicleId }) => {
         const [vehicle, setVehicle] = useState<any>(null);
         const [availableFeatures, setAvailableFeatures] = useState<string[]>([]);

         useEffect(() => {
             // Fetch available features based on vehicle type
             vehicleService.getAvailableFeatures(vehicleId).then(setAvailableFeatures);
         }, [vehicleId]);

         if (!vehicle) return <CircularProgress />;

         return (
             <Box>
                 {/* Header */}
                 <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                     <Box>
                         <Typography variant="h4">{vehicle.vehicleNumber}</Typography>
                         <Chip label={vehicle.fuelType} color={getFuelTypeColor(vehicle.fuelType)} />
                     </Box>
                     {getFuelTypeIcon(vehicle.fuelType)}
                 </Box>

                 {/* Tabs for different sections */}
                 <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)}>
                     <Tab label="Overview" />
                     {availableFeatures.includes('BATTERY_TRACKING') && <Tab label="Battery" />}
                     {availableFeatures.includes('FUEL_CONSUMPTION') && <Tab label="Fuel" />}
                     <Tab label="Maintenance" />
                     <Tab label="Trips" />
                 </Tabs>

                 {/* Tab Panels */}
                 {tabValue === 0 && (
                     <Box>
                         <Grid container spacing={2}>
                             <Grid item xs={12} sm={6}>
                                 <InfoCard label="Make" value={vehicle.make} />
                             </Grid>
                             <Grid item xs={12} sm={6}>
                                 <InfoCard label="Model" value={vehicle.model} />
                             </Grid>

                             {vehicle.fuelType !== 'ICE' && (
                                 <Grid item xs={12} sm={6}>
                                     <InfoCard label="Battery Capacity" value={`${vehicle.batteryCapacity} kWh`} />
                                 </Grid>
                             )}

                             {vehicle.fuelType !== 'EV' && (
                                 <Grid item xs={12} sm={6}>
                                     <InfoCard label="Tank Capacity" value={`${vehicle.fuelTankCapacity} L`} />
                                 </Grid>
                             )}
                         </Grid>
                     </Box>
                 )}

                 {/* Battery Tab - Only for EV/Hybrid */}
                 {availableFeatures.includes('BATTERY_TRACKING') && tabValue === 1 && (
                     <BatteryStatusPanel vehicleId={vehicleId} />
                 )}

                 {/* Fuel Tab - Only for ICE/Hybrid */}
                 {availableFeatures.includes('FUEL_CONSUMPTION') && tabValue === 2 && (
                     <FuelStatusPanel vehicleId={vehicleId} />
                 )}
             </Box>
         );
     };
     ```

   - Create: `frontend/src/components/fleet/FuelStatusPanel.tsx`
     ```typescript
     export const FuelStatusPanel: React.FC<{vehicleId: string}> = ({ vehicleId }) => {
         const [fuelData, setFuelData] = useState(null);

         useEffect(() => {
             vehicleService.getFuelMetrics(vehicleId).then(setFuelData);
         }, [vehicleId]);

         return (
             <Box>
                 <Grid container spacing={2}>
                     <Grid item xs={12} sm={6}>
                         <MetricCard
                             title="Current Fuel Level"
                             value={`${fuelData?.currentFuelLevel}%`}
                             icon={<LocalGasStationIcon />}
                         />
                     </Grid>
                     <Grid item xs={12} sm={6}>
                         <MetricCard
                             title="Average Consumption"
                             value={`${fuelData?.avgConsumption} L/100km`}
                             icon={<TrendingDownIcon />}
                         />
                     </Grid>
                 </Grid>
                 <FuelTrendChart vehicleId={vehicleId} />
             </Box>
         );
     };
     ```

2. **Frontend - Constants:**
   - Create: `frontend/src/constants/fuelTypes.ts`
     ```typescript
     export const FUEL_TYPES = {
         EV: { label: 'Electric', color: 'green', icon: 'ElectricBolt' },
         ICE: { label: 'Petrol/Diesel', color: 'orange', icon: 'LocalGasStation' },
         HYBRID: { label: 'Hybrid', color: 'blue', icon: 'Handshake' }
     };
     ```

**Acceptance Criteria:**
- [x] Vehicle list shows fuel type with icon
- [x] Filtering by fuel type works
- [x] Vehicle details show appropriate tabs based on fuel type
- [x] EV-only components hidden for ICE vehicles
- [x] ICE-only components hidden for EV vehicles
- [x] Responsive design maintained
- [x] Smooth transitions between fuel types

**Testing:**
- [x] Component renders for all fuel types
- [x] Filtering works (all combinations)
- [x] Tab visibility based on availableFeatures
- [x] API calls for fuel metrics (when available)
- [x] Mobile responsiveness

**Copilot Guidance:**
"Update VehicleList to add fuel type column with icon/color coding. Add FuelTypeSelector to filter list. Use conditional rendering for battery% vs fuel% columns. Update VehicleDetails to fetch availableFeatures from API. Create tabs: Overview (always), Battery (if EV/HYBRID), Fuel (if ICE/HYBRID), Maintenance, Trips. Create FuelStatusPanel showing currentFuelLevel and avgConsumption (like BatteryStatusPanel). Add FUEL_TYPES constant object with color and icon mappings."

---

#### **PR #15: Create Charging and Fuel Station Discovery Components**
**Priority:** HIGH (Feature completeness)
**Effort:** 2-3 days
**Files Modified:** ~6 files
**Type:** Frontend Feature

**What This PR Does:**
```
Create new components for discovering nearby:
1. Charging stations (EV/Hybrid only)
2. Fuel stations (ICE/Hybrid only)
Unified UI that shows appropriate stations based on vehicle fuel type.
```

**Changes Required:**

1. **Frontend - Components:**
   - Create: `frontend/src/components/fleet/StationDiscovery.tsx`
     ```typescript
     export const StationDiscovery: React.FC<{vehicleId: string}> = ({ vehicleId }) => {
         const [vehicle, setVehicle] = useState(null);
         const [stations, setStations] = useState([]);
         const [stationType, setStationType] = useState<'charging' | 'fuel'>('charging');

         useEffect(() => {
             vehicleService.getVehicle(vehicleId).then(setVehicle);
         }, [vehicleId]);

         useEffect(() => {
             if (vehicle?.fuelType === 'ICE') {
                 setStationType('fuel');
             } else {
                 setStationType('charging');
             }
         }, [vehicle]);

         const handleStationTypeChange = (type: string) => {
             setStationType(type as any);
             // Fetch appropriate stations
             if (type === 'charging') {
                 chargingService.getNearbyStations(vehicle?.latitude, vehicle?.longitude)
                     .then(setStations);
             } else {
                 fuelService.getNearbyStations(vehicle?.latitude, vehicle?.longitude)
                     .then(setStations);
             }
         };

         if (!vehicle) return <CircularProgress />;

         return (
             <Box>
                 {/* Conditionally show station type selector */}
                 {(vehicle.fuelType === 'HYBRID') && (
                     <Box sx={{ mb: 2 }}>
                         <ButtonGroup>
                             <Button
                                 variant={stationType === 'charging' ? 'contained' : 'outlined'}
                                 onClick={() => handleStationTypeChange('charging')}
                             >
                                 Charging Stations
                             </Button>
                             <Button
                                 variant={stationType === 'fuel' ? 'contained' : 'outlined'}
                                 onClick={() => handleStationTypeChange('fuel')}
                             >
                                 Fuel Stations
                             </Button>
                         </ButtonGroup>
                     </Box>
                 )}

                 {/* For EV: Always show charging */}
                 {vehicle.fuelType === 'EV' && (
                     <Typography variant="h6" sx={{ mb: 2 }}>Nearby Charging Stations</Typography>
                 )}

                 {/* For ICE: Always show fuel */}
                 {vehicle.fuelType === 'ICE' && (
                     <Typography variant="h6" sx={{ mb: 2 }}>Nearby Fuel Stations</Typography>
                 )}

                 {/* Map View */}
                 <Box sx={{ height: 400, mb: 3 }}>
                     <StationMap stations={stations} vehicleLocation={vehicle} />
                 </Box>

                 {/* List View */}
                 <Box>
                     {stations.map(station => (
                         <StationCard
                             key={station.id}
                             station={station}
                             onSelect={() => handleStationSelect(station)}
                             stationType={stationType}
                         />
                     ))}
                 </Box>
             </Box>
         );
     };
     ```

   - Create: `frontend/src/components/fleet/StationCard.tsx`
     ```typescript
     interface StationCardProps {
         station: any;
         onSelect: () => void;
         stationType: 'charging' | 'fuel';
     }

     export const StationCard: React.FC<StationCardProps> = ({ station, onSelect, stationType }) => (
         <Card sx={{ mb: 2 }}>
             <CardContent>
                 <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                     <Box>
                         <Typography variant="h6">{station.name}</Typography>
                         <Typography variant="body2" color="textSecondary">
                             {station.distance.toFixed(1)} km away
                         </Typography>
                     </Box>
                     <Box sx={{ textAlign: 'right' }}>
                         {stationType === 'charging' && (
                             <>
                                 <Typography variant="body2">₹{station.costPerKwh}/kWh</Typography>
                                 <Typography variant="caption" color="textSecondary">
                                     {station.availableChargers} chargers
                                 </Typography>
                             </>
                         )}
                         {stationType === 'fuel' && (
                             <>
                                 <Typography variant="body2">₹{station.costPerLiter}/L</Typography>
                                 <Typography variant="caption" color="textSecondary">
                                     {station.availability ? 'Available' : 'Closed'}
                                 </Typography>
                             </>
                         )}
                     </Box>
                 </Box>
                 <Button variant="outlined" onClick={onSelect} fullWidth sx={{ mt: 2 }}>
                     {stationType === 'charging' ? 'Start Charging' : 'Navigate'}
                 </Button>
             </CardContent>
         </Card>
     );
     ```

   - Create: `frontend/src/components/fleet/StationMap.tsx`
     ```typescript
     export const StationMap: React.FC<{stations: any[], vehicleLocation: any}> = ({
         stations,
         vehicleLocation
     }) => {
         const mapContainer = useRef<HTMLDivElement>(null);
         const map = useRef<mapboxgl.Map | null>(null);

         useEffect(() => {
             if (!mapContainer.current) return;

             map.current = new mapboxgl.Map({
                 container: mapContainer.current,
                 style: 'mapbox://styles/mapbox/streets-v12',
                 center: [vehicleLocation.longitude, vehicleLocation.latitude],
                 zoom: 13
             });

             // Add vehicle marker
             new mapboxgl.Marker({ color: 'red' })
                 .setLngLat([vehicleLocation.longitude, vehicleLocation.latitude])
                 .addTo(map.current);

             // Add station markers
             stations.forEach(station => {
                 new mapboxgl.Marker({ color: 'blue' })
                     .setLngLat([station.longitude, station.latitude])
                     .setPopup(new mapboxgl.Popup().setHTML(`
                         <div>
                             <h3>${station.name}</h3>
                             <p>${station.distance} km away</p>
                         </div>
                     `))
                     .addTo(map.current!);
             });
         }, [stations, vehicleLocation]);

         return <Box ref={mapContainer} sx={{ height: '100%', width: '100%' }} />;
     };
     ```

2. **Frontend - Services:**
   - Create: `frontend/src/services/fuelService.ts`
     ```typescript
     export const fuelService = {
         getNearbyStations: (latitude: number, longitude: number, radius = 5) =>
             api.get(`/fuel/stations/nearby`, { params: { latitude, longitude, radius } }),

         getStationDetails: (stationId: string) =>
             api.get(`/fuel/stations/${stationId}`)
     };
     ```

**Acceptance Criteria:**
- [x] Charging stations show for EV vehicles
- [x] Fuel stations show for ICE vehicles
- [x] Hybrid vehicles can toggle between both
- [x] Map view shows nearby stations
- [x] List view shows station details
- [x] Distance and pricing displayed
- [x] Responsive design

**Testing:**
- [x] Component renders for all fuel types
- [x] Station toggle works for Hybrid
- [x] Map markers appear correctly
- [x] Station card click triggers actions
- [x] API calls execute properly

**Copilot Guidance:**
"Create StationDiscovery component that shows Charging Stations for EV/HYBRID and Fuel Stations for ICE. Include map view using Mapbox showing vehicle location (red marker) and nearby stations (blue markers). Create StationCard showing station name, distance, pricing (₹/kWh for charging, ₹/L for fuel), and action button. Create StationMap component with Mapbox integration. Add fuelService with getNearbyStations method similar to chargingService."

---

#### **PR #16: Create Multi-Fuel Dashboard Overview**
**Priority:** MEDIUM (Analytics)
**Effort:** 2-3 days
**Files Modified:** ~6 files
**Type:** Frontend Feature

**What This PR Does:**
```
Update Dashboard to show:
1. Fleet composition breakdown (ICE%, EV%, Hybrid%)
2. Blended cost analysis (fuel + energy costs)
3. Vehicles needing service (oil change for ICE, battery check for EV)
4. Upcoming fuel/charging needs
```

**Changes Required:**

1. **Frontend - Components:**
   - Create: `frontend/src/components/dashboard/FleetCompositionCard.tsx`
     ```typescript
     export const FleetCompositionCard: React.FC<{companyId: string}> = ({ companyId }) => {
         const [composition, setComposition] = useState(null);

         useEffect(() => {
             vehicleService.getFleetComposition(companyId).then(setComposition);
         }, [companyId]);

         if (!composition) return <CircularProgress />;

         return (
             <Card>
                 <CardHeader title="Fleet Composition" />
                 <CardContent>
                     <PieChart width={400} height={300} data={[
                         { name: 'EV', value: composition.ev, fill: 'green' },
                         { name: 'ICE', value: composition.ice, fill: 'orange' },
                         { name: 'Hybrid', value: composition.hybrid, fill: 'blue' }
                     ]} />
                     <Box sx={{ mt: 2 }}>
                         <Grid container spacing={1}>
                             <Grid item xs={12}>
                                 <Typography variant="body2">
                                     Total: {composition.total} vehicles
                                 </Typography>
                             </Grid>
                             <Grid item xs={12}>
                                 <LinearProgress
                                     variant="determinate"
                                     value={(composition.ev / composition.total) * 100}
                                     sx={{ backgroundColor: 'lightgreen', height: 8 }}
                                 />
                                 <Typography variant="caption">
                                     EV: {composition.ev} ({(composition.ev / composition.total) * 100}%)
                                 </Typography>
                             </Grid>
                         </Grid>
                     </Box>
                 </CardContent>
             </Card>
         );
     };
     ```

   - Create: `frontend/src/components/dashboard/CostBreakdownCard.tsx`
     ```typescript
     export const CostBreakdownCard: React.FC<{companyId: string}> = ({ companyId }) => {
         const [costs, setCosts] = useState(null);

         useEffect(() => {
             analyticsService.getCostBreakdown(companyId).then(setCosts);
         }, [companyId]);

         if (!costs) return <CircularProgress />;

         return (
             <Card>
                 <CardHeader title="Cost Breakdown" />
                 <CardContent>
                     <BarChart width={400} height={300} data={[
                         { name: 'Energy (EV)', value: costs.evCost },
                         { name: 'Fuel (ICE)', value: costs.iceCost },
                         { name: 'Maintenance', value: costs.maintenanceCost }
                     ]} />
                     <Box sx={{ mt: 2 }}>
                         <Grid container spacing={2}>
                             <Grid item xs={12}>
                                 <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                     <Typography>Total Cost:</Typography>
                                     <Typography variant="h6">
                                         ₹{costs.totalCost.toFixed(2)}
                                     </Typography>
                                 </Box>
                             </Grid>
                             <Grid item xs={6}>
                                 <Typography variant="body2">EV Cost: ₹{costs.evCost}</Typography>
                             </Grid>
                             <Grid item xs={6}>
                                 <Typography variant="body2">ICE Cost: ₹{costs.iceCost}</Typography>
                             </Grid>
                         </Grid>
                     </Box>
                 </CardContent>
             </Card>
         );
     };
     ```

   - Create: `frontend/src/components/dashboard/MaintenanceAlertsCard.tsx`
     ```typescript
     export const MaintenanceAlertsCard: React.FC<{companyId: string}> = ({ companyId }) => {
         const [alerts, setAlerts] = useState([]);

         useEffect(() => {
             maintenanceService.getUpcomingServices(companyId).then(setAlerts);
         }, [companyId]);

         const getAlertColor = (type: string, vehicleType: string) => {
             // Oil change for ICE, battery check for EV, etc.
             return type === 'BATTERY_CHECK' && vehicleType === 'ICE' ? 'warning' : 'info';
         };

         return (
             <Card>
                 <CardHeader title="Upcoming Maintenance" />
                 <CardContent>
                     {alerts.length === 0 ? (
                         <Typography color="textSecondary">All vehicles up to date</Typography>
                     ) : (
                         <List>
                             {alerts.map(alert => (
                                 <ListItem key={alert.id}>
                                     <ListItemText
                                         primary={alert.vehicleNumber}
                                         secondary={`${alert.maintenanceType} - ${alert.daysUntilDue} days`}
                                     />
                                     <Chip
                                         label={alert.maintenanceType}
                                         color={getAlertColor(alert.maintenanceType, alert.vehicleType)}
                                         size="small"
                                     />
                                 </ListItem>
                             ))}
                         </List>
                     )}
                 </CardContent>
             </Card>
         );
     };
     ```

   - Update: `frontend/src/pages/DashboardPage.tsx`
     ```typescript
     export const DashboardPage: React.FC = () => {
         const companyId = useAuth().companyId;

         return (
             <Box sx={{ p: 3 }}>
                 <Grid container spacing={3}>
                     {/* New Fleet Composition */}
                     <Grid item xs={12} sm={6}>
                         <FleetCompositionCard companyId={companyId} />
                     </Grid>

                     {/* New Cost Breakdown */}
                     <Grid item xs={12} sm={6}>
                         <CostBreakdownCard companyId={companyId} />
                     </Grid>

                     {/* Updated Alerts */}
                     <Grid item xs={12}>
                         <MaintenanceAlertsCard companyId={companyId} />
                     </Grid>

                     {/* Existing components */}
                     <Grid item xs={12} sm={6}>
                         <UtilizationChart />
                     </Grid>
                 </Grid>
             </Box>
         );
     };
     ```

**Acceptance Criteria:**
- [x] Fleet composition chart shows EV/ICE/Hybrid breakdown
- [x] Cost breakdown shows blended costs
- [x] Maintenance alerts show fuel-type-specific services
- [x] Dashboard responsive on all screens
- [x] Real-time data updates
- [x] Charts and graphs display correctly

**Testing:**
- [x] Component renders with data
- [x] Charts display correctly
- [x] API calls execute
- [x] Responsive design on mobile

**Copilot Guidance:**
"Create FleetCompositionCard showing pie chart of EV%, ICE%, Hybrid% distribution. Create CostBreakdownCard with bar chart showing Energy Cost (EV), Fuel Cost (ICE), Maintenance Cost breakdown. Create MaintenanceAlertsCard listing upcoming services with fuel-type-specific icons (battery icon for EV, oil can for ICE). Update DashboardPage to include these new cards. Use Recharts for visualizations."

---

### PHASE 6: BILLING & MONETIZATION (PRs 17-18)

#### **PR #17: Implement Multi-Tier Pricing Structure**
**Priority:** HIGH (Revenue model)
**Effort:** 2-3 days
**Files Modified:** ~6 files
**Type:** Billing Feature

**What This PR Does:**
```
Create three pricing tiers:
- Basic (₹299/vehicle/month) - All vehicles, general features
- EV Premium (₹699/vehicle/month) - All features + EV-specific
- Enterprise (₹999/vehicle/month) - Multi-depot, integrations, support
```

**Changes Required:**

1. **Backend - Billing Service:**
   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/model/PricingTier.java`
     ```java
     @Entity
     public class PricingTier {
         Long id;
         String name; // BASIC, EV_PREMIUM, ENTERPRISE
         String description;
         Double monthlyPricePerVehicle;
         List<String> includedFeatures; // JSON array
         Integer maxVehicles; // Unlimited if null
         Integer maxDepots; // For Enterprise
         LocalDateTime createdAt;
         LocalDateTime updatedAt;
     }
     ```

   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/model/Subscription.java`
     ```java
     // Update existing entity
     private String tier; // BASIC, EV_PREMIUM, ENTERPRISE
     private FuelType applicableVehicleType; // For tier matching
     private List<Long> vehicleIds; // Vehicles under this subscription
     ```

   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/service/PricingService.java`
     ```java
     @Service
     public class PricingService {
         public PricingTier getApplicableTier(Long vehicleId) {
             Vehicle vehicle = fleetService.getVehicle(vehicleId);
             if (vehicle.getFuelType() == FuelType.EV) {
                 return tierRepository.findByName("EV_PREMIUM");
             } else {
                 return tierRepository.findByName("BASIC");
             }
         }

         public Double calculateMonthlyBill(Long companyId) {
             // Sum all vehicles * their tier prices
         }
     }
     ```

   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/controller/PricingController.java`
     ```java
     @GetMapping("/tiers")
     public ResponseEntity<List<PricingTierDTO>> getAllTiers()

     @GetMapping("/vehicle/{vehicleId}/applicable-tier")
     public ResponseEntity<PricingTierDTO> getApplicableTier(@PathVariable Long vehicleId)

     @GetMapping("/company/{companyId}/monthly-bill")
     public ResponseEntity<BillDetailDTO> getMonthlyBill(@PathVariable Long companyId)
     ```

2. **Database - Billing Service:**
   - Create migration: `V__initialize_pricing_tiers.sql`
     ```sql
     INSERT INTO pricing_tiers VALUES
     ('BASIC', 'General Fleet Management', 299, 'TRACKING,GEOFENCE,TRIP_HISTORY,BASIC_ANALYTICS', NULL, NULL),
     ('EV_PREMIUM', 'All Features + EV Optimization', 699, 'TRACKING,GEOFENCE,TRIP_HISTORY,CHARGING,BATTERY_TRACKING,ADVANCED_ANALYTICS', NULL, NULL),
     ('ENTERPRISE', 'Multi-Depot + Custom', 999, 'ALL_FEATURES,MULTI_DEPOT,CUSTOM_INTEGRATIONS,DEDICATED_SUPPORT', 'UNLIMITED', NULL);
     ```

3. **Frontend:**
   - Create: `frontend/src/pages/PricingPage.tsx`
     ```typescript
     export const PricingPage: React.FC = () => {
         const [tiers, setTiers] = useState<PricingTier[]>([]);

         useEffect(() => {
             billingService.getAllTiers().then(setTiers);
         }, []);

         return (
             <Box sx={{ p: 3 }}>
                 <Typography variant="h3" sx={{ mb: 4, textAlign: 'center' }}>
                     Flexible Pricing for Every Fleet
                 </Typography>

                 <Grid container spacing={3} sx={{ maxWidth: 1200, mx: 'auto' }}>
                     {tiers.map(tier => (
                         <Grid item xs={12} sm={6} md={4} key={tier.name}>
                             <PricingCard tier={tier} />
                         </Grid>
                     ))}
                 </Grid>
             </Box>
         );
     };
     ```

   - Create: `frontend/src/components/pricing/PricingCard.tsx`
     ```typescript
     export const PricingCard: React.FC<{tier: PricingTier}> = ({ tier }) => (
         <Card
             sx={{
                 height: '100%',
                 display: 'flex',
                 flexDirection: 'column',
                 position: 'relative',
                 border: tier.name === 'EV_PREMIUM' ? '3px solid green' : '1px solid gray'
             }}
         >
             {tier.name === 'EV_PREMIUM' && (
                 <Chip label="Popular" color="success" sx={{ position: 'absolute', top: -12, right: 16 }} />
             )}

             <CardHeader title={tier.name.replace('_', ' ')} />

             <CardContent sx={{ flexGrow: 1 }}>
                 <Typography variant="h4" sx={{ mb: 2 }}>
                     ₹{tier.monthlyPricePerVehicle}/month
                     <Typography variant="caption" display="block">per vehicle</Typography>
                 </Typography>

                 <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                     {tier.description}
                 </Typography>

                 <Divider sx={{ my: 2 }} />

                 <Typography variant="subtitle2" sx={{ mb: 1 }}>Includes:</Typography>
                 <List dense>
                     {tier.includedFeatures.map(feature => (
                         <ListItem key={feature}>
                             <CheckIcon sx={{ mr: 1, color: 'green' }} />
                             <ListItemText primary={feature} />
                         </ListItem>
                     ))}
                 </List>
             </CardContent>

             <CardActions>
                 <Button variant="contained" fullWidth>
                     {tier.name === 'EV_PREMIUM' ? 'Start Free Trial' : 'Upgrade'}
                 </Button>
             </CardActions>
         </Card>
     );
     ```

**Acceptance Criteria:**
- [x] Three pricing tiers defined in database
- [x] PricingService calculates applicable tier per vehicle
- [x] API endpoints return pricing information
- [x] Billing calculated correctly per tier
- [x] Frontend pricing page displays tiers clearly
- [x] Tier comparison visible

**Testing:**
- [x] Pricing tier calculation tests (per fuel type)
- [x] Monthly bill calculation tests
- [x] API endpoint tests
- [x] Frontend rendering tests

**Copilot Guidance:**
"Create PricingTier entity with name (BASIC/EV_PREMIUM/ENTERPRISE), monthlyPricePerVehicle, and includedFeatures JSON array. Create PricingService.getApplicableTier(vehicleId) that returns BASIC for ICE, EV_PREMIUM for EV. Create PricingController with endpoints: GET /billing/tiers, GET /billing/vehicle/{id}/applicable-tier, GET /billing/company/{id}/monthly-bill. Pre-populate database with three tiers: BASIC (₹299), EV_PREMIUM (₹699), ENTERPRISE (₹999). Create PricingPage and PricingCard frontend components showing tier comparisons."

---

#### **PR #18: Create Invoice Generation and Payment Tracking**
**Priority:** MEDIUM (Operational)
**Effort:** 2-3 days
**Files Modified:** ~8 files
**Type:** Billing Feature

**What This PR Does:**
```
Auto-generate monthly invoices based on:
- Vehicle count per tier
- Blended pricing (BASIC + EV_PREMIUM)
- Usage-based costs (optional overage fees)
- Payment processing and tracking
```

**Changes Required:**

1. **Backend - Billing Service:**
   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/service/InvoiceGenerationService.java`
     ```java
     @Service
     @Scheduled(cron = "0 0 1 * * *") // Run on 1st of each month
     public class InvoiceGenerationService {
         public void generateMonthlyInvoices() {
             List<Company> companies = companyService.getAllActive();
             companies.forEach(company -> {
                 Invoice invoice = generateInvoiceForCompany(company);
                 invoiceRepository.save(invoice);
                 notificationService.sendInvoiceNotification(company, invoice);
             });
         }

         private Invoice generateInvoiceForCompany(Company company) {
             List<Vehicle> vehicles = fleetService.getCompanyVehicles(company.getId());

             // Group by tier
             Map<String, List<Vehicle>> vehiclesByTier = vehicles.stream()
                 .collect(Collectors.groupingBy(v -> getApplicableTier(v)));

             // Calculate line items
             List<InvoiceLineItem> items = new ArrayList<>();
             vehiclesByTier.forEach((tier, vehicleList) -> {
                 PricingTier pricingTier = pricingService.getTier(tier);
                 items.add(InvoiceLineItem.builder()
                     .description(tier + " - " + vehicleList.size() + " vehicles")
                     .quantity(vehicleList.size())
                     .unitPrice(pricingTier.getMonthlyPricePerVehicle())
                     .total(vehicleList.size() * pricingTier.getMonthlyPricePerVehicle())
                     .build());
             });

             // Calculate total
             Double total = items.stream().mapToDouble(InvoiceLineItem::getTotal).sum();

             return Invoice.builder()
                 .companyId(company.getId())
                 .invoiceNumber(generateInvoiceNumber(company))
                 .lineItems(items)
                 .subtotal(total)
                 .tax(total * 0.18) // 18% GST
                 .total(total * 1.18)
                 .dueDate(LocalDate.now().plusDays(30))
                 .status("UNPAID")
                 .createdAt(LocalDateTime.now())
                 .build();
         }
     }
     ```

   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/service/PaymentProcessingService.java`
     ```java
     @Service
     public class PaymentProcessingService {
         public PaymentResult processPayment(Long invoiceId, PaymentMethodDTO method) {
             Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

             // Call payment gateway (Razorpay, Stripe, etc.)
             PaymentGatewayResponse response = paymentGateway.charge(
                 invoice.getTotal(),
                 method
             );

             if (response.isSuccess()) {
                 Payment payment = Payment.builder()
                     .invoiceId(invoiceId)
                     .amount(invoice.getTotal())
                     .status("SUCCESS")
                     .transactionId(response.getTransactionId())
                     .createdAt(LocalDateTime.now())
                     .build();

                 paymentRepository.save(payment);
                 invoice.setStatus("PAID");
                 invoiceRepository.save(invoice);

                 return new PaymentResult(true, "Payment processed successfully");
             } else {
                 return new PaymentResult(false, response.getErrorMessage());
             }
         }
     }
     ```

   - Create: `backend/billing-service/src/main/java/com/evfleet/billing/controller/InvoicingController.java`
     ```java
     @GetMapping("/company/{companyId}/invoices")
     public ResponseEntity<List<InvoiceDTO>> getCompanyInvoices(@PathVariable Long companyId)

     @GetMapping("/invoices/{invoiceId}")
     public ResponseEntity<InvoiceDTO> getInvoiceDetails(@PathVariable Long invoiceId)

     @PostMapping("/invoices/{invoiceId}/pay")
     public ResponseEntity<PaymentResultDTO> payInvoice(
         @PathVariable Long invoiceId,
         @RequestBody PaymentMethodDTO paymentMethod)

     @GetMapping("/company/{companyId}/payment-history")
     public ResponseEntity<List<PaymentDTO>> getPaymentHistory(@PathVariable Long companyId)
     ```

2. **Database:**
   - Create migration with invoice and payment tables

3. **Frontend:**
   - Update: `frontend/src/pages/BillingPage.tsx` (enhance existing)
     ```typescript
     // Add invoice list with generation history
     // Add payment processing UI
     // Add download invoice as PDF
     ```

**Acceptance Criteria:**
- [x] Monthly invoices auto-generated
- [x] Line items grouped by pricing tier
- [x] Tax/GST calculated correctly
- [x] Payment processing integrated
- [x] Invoice history viewable
- [x] Email notifications sent

**Testing:**
- [x] Invoice generation tests
- [x] Bill calculation tests (per tier)
- [x] Payment processing tests
- [x] Notification tests

**Copilot Guidance:**
"Create InvoiceGenerationService with @Scheduled method that runs on 1st of month. For each company, group vehicles by applicable tier (BASIC for ICE, EV_PREMIUM for EV), calculate quantities, and create line items. Create Invoice entity with lineItems list, subtotal, tax (18% GST), total. Create PaymentProcessingService that calls payment gateway and updates invoice status. Add InvoicingController endpoints: GET /billing/company/{id}/invoices, GET /billing/invoices/{id}, POST /billing/invoices/{id}/pay. Update BillingPage to show invoice list and payment history."

---

## PART 3: IMPLEMENTATION TIMELINE & EFFORT ESTIMATION

### Phase-Wise Breakdown

| Phase | PRs | Duration | Effort | Risk |
|-------|-----|----------|--------|------|
| **Phase 1: Data Model** | 1-4 | 2 weeks | 320 hours | Medium |
| **Phase 2: API Enhancements** | 5-8 | 2 weeks | 300 hours | Medium |
| **Phase 3: Charging Service** | 9-10 | 1 week | 150 hours | Low |
| **Phase 4: Maintenance Service** | 11-12 | 1.5 weeks | 180 hours | Medium |
| **Phase 5: Frontend Updates** | 13-16 | 2.5 weeks | 380 hours | High |
| **Phase 6: Billing** | 17-18 | 1 week | 180 hours | Low |
| **Testing & QA** | - | 2 weeks | 200 hours | Medium |
| **Documentation & Cleanup** | - | 0.5 week | 50 hours | Low |
| | | **TOTAL** | **~1,760 hours** | |

### Per-PR Effort Breakdown

| PR | Title | Backend | Frontend | Database | Testing | Total Hours | Days |
|----|-------|---------|----------|----------|---------|-------------|------|
| 1 | Vehicle Fuel Type | 20 | - | 30 | 20 | 70 | 3-4 |
| 2 | Feature Flag System | 40 | - | 20 | 20 | 80 | 4-5 |
| 3 | Telemetry Data Model | 30 | - | 25 | 20 | 75 | 3-4 |
| 4 | Vehicle Queries | 25 | - | 10 | 15 | 50 | 2-3 |
| 5 | Vehicle CRUD APIs | 40 | 30 | 5 | 25 | 100 | 4-5 |
| 6 | Telemetry APIs | 35 | 20 | 5 | 20 | 80 | 3-4 |
| 7 | Trip Analytics | 40 | 30 | 5 | 20 | 95 | 4-5 |
| 8 | Feature Availability | 20 | 20 | - | 15 | 55 | 2-3 |
| 9 | Charging Service Validation | 25 | - | - | 15 | 40 | 2 |
| 10 | Charging Analytics | 20 | - | - | 10 | 30 | 1-2 |
| 11 | ICE Maintenance Schedules | 30 | - | 20 | 20 | 70 | 3-4 |
| 12 | Maintenance Cost Tracking | 20 | - | 10 | 15 | 45 | 2 |
| 13 | Vehicle Form Components | - | 60 | - | 40 | 100 | 4-5 |
| 14 | Vehicle List & Details | - | 80 | - | 50 | 130 | 5-6 |
| 15 | Station Discovery | - | 70 | - | 45 | 115 | 4-5 |
| 16 | Dashboard Overview | - | 60 | - | 40 | 100 | 4-5 |
| 17 | Pricing Tiers | 30 | 40 | 15 | 25 | 110 | 4-5 |
| 18 | Invoice Generation | 40 | 30 | 20 | 25 | 115 | 4-5 |
| | | **475** | **520** | **200** | **425** | **1,620** | **65 days** |

### Timeline with GitHub Copilot

**Assumption:** GitHub Copilot can generate ~60-70% of code with human review/refinement

**Realistic Timeline:**
- **PR Development:** 65 days * 0.4 (review/refinement) = **26 days of focused work**
- **Testing & QA:** 2 weeks
- **Documentation:** 3-4 days
- **Total:** **8-9 weeks with Copilot assistance**

**Without Copilot:** ~10-12 weeks

---

## PART 4: TESTING STRATEGY

### Unit Tests (Per PR)
- Entity model tests
- Service logic tests
- DTO mapping tests
- Validation tests
- Total: ~2-3 tests per PR

### Integration Tests
- API endpoint tests (MockMvc)
- Database persistence tests
- Feature flag integration tests
- Total: ~1-2 tests per PR

### End-to-End Tests
- Multi-fuel vehicle workflow
- Billing calculation flow
- Invoice generation flow
- Frontend component flows

### Performance Tests
- Vehicle query performance (1000+ vehicles)
- Telemetry ingestion rate (1000 msgs/sec)
- Analytics calculation performance
- Dashboard load time

---

## PART 5: RISK MITIGATION

### High-Risk Areas

1. **Database Migration**
   - Risk: Data loss, backward compatibility
   - Mitigation: Comprehensive backup, blue-green deployment, rollback scripts

2. **API Backward Compatibility**
   - Risk: Breaking existing clients
   - Mitigation: API versioning, deprecation period, clear migration guide

3. **Feature Complexity**
   - Risk: Scope creep, incomplete features
   - Mitigation: Strict PR review, feature branch isolation, definition of done

### Rollback Strategy

Each PR should include:
- Rollback migration script
- API contract version
- Feature flag for new functionality (can disable if issues)
- Test data reset scripts

---

## PART 6: GITHUB COPILOT PROMPTING STRATEGY

### Prompt Template for Each PR

```
Task: [PR Title]
Objective: [What this PR achieves]
Files to Create/Modify:
- [List all files]

Requirements:
1. [Specific requirement]
2. [Test coverage requirement]
3. [Documentation requirement]

Acceptance Criteria:
- [ ] [Criteria 1]
- [ ] [Criteria 2]

Technology Stack:
- Backend: Java 17, Spring Boot 3, Spring Data JPA
- Frontend: React 18, TypeScript, Material-UI
- Database: PostgreSQL, Flyway migrations

Related Services/Components:
- [List service dependencies]

Error Handling:
- [Specific error scenarios to handle]

Example Inputs/Outputs:
- [Request/response examples]
```

### Copilot-Specific Tips

1. **Start with DTOs** - Define request/response models clearly
2. **Database migrations first** - Copilot generates SQL better with clear schema
3. **Tests early** - Write test names, Copilot fills implementation
4. **Clear entity relationships** - Show @ManyToOne, @OneToMany relationships
5. **API endpoint patterns** - Provide 1-2 examples, Copilot follows the pattern

---

## PART 7: SUCCESS METRICS

### Completion Metrics
- All 18 PRs merged
- 100% test coverage for new code
- 0 critical bugs in UAT
- Performance benchmarks met

### Business Metrics
- Customer acceptance (target: 80% positive feedback)
- Onboarding speed (target: < 5 min per vehicle)
- Pricing tier adoption (target: 40% on EV Premium)

### Code Quality Metrics
- SonarQube score > 80
- Code coverage > 85%
- Technical debt ratio < 5%

---

## PART 8: ROLLOUT PLAN

### Phase 1: Internal Staging (Week 1-2)
- Deploy all PRs to staging
- Comprehensive QA testing
- Performance testing

### Phase 2: Beta Release (Week 3)
- Release to selected beta customers
- Collect feedback
- Monitor error rates

### Phase 3: General Availability (Week 4)
- Release to all customers
- Monitor adoption metrics
- Support new customers

### Phase 4: Optimization (Weeks 5-6)
- Performance optimization
- Bug fixes
- Feature refinements

---

## CONCLUSION

This migration from **EV-Only to General + EV Excellence** requires:

✅ **18 PRs** across all layers
✅ **~1,620 hours** of development (65 days)
✅ **8-10 weeks** with Copilot assistance
✅ **Comprehensive testing** at each phase
✅ **Clear rollback strategy**

The customer's feedback ("pay more for general which includes EV") validates this approach. By maintaining backward compatibility, using feature flags, and tiered pricing, we can successfully pivot without disrupting existing EV customers while capturing the larger general fleet market.

---

**Next Steps:**
1. Approve this migration plan
2. Start with Copilot on PR #1 (Vehicle Fuel Type Support)
3. Iterate through phases sequentially
4. Collect customer feedback during beta (Phase 2)
5. Adjust plan based on learnings

