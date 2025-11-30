# SEV Platform - 50 PR Work Breakdown Plan

**Created:** November 29, 2025  
**Purpose:** Comprehensive work breakdown for 50 PRs to enhance SEV EV Fleet Management Platform  
**Parallel Execution:** PRs grouped in batches of 4 for conflict-free parallel execution  
**Estimated Time Per PR:** 40+ minutes

---

## Executive Summary

This document outlines 50 PRs organized into 13 batches. Each batch contains 4 PRs that can be executed in parallel without merge conflicts. The work covers:

1. **Research & Validation PRs (1-4)**: Validate AI-generated documentation
2. **Backend Feature Implementation (5-24)**: Implement missing backend features
3. **Frontend Enhancement PRs (25-40)**: UI improvements and missing features
4. **Testing & Quality PRs (41-48)**: Unit tests, integration tests, documentation
5. **Infrastructure & DevOps PRs (49-50)**: Docker, CI/CD setup

---

## Status Tracking Convention

After completion, Copilot Coding Agent should update each PR entry with:
```
✅ PR #X DONE - [Completion Date]
```

---

## BATCH 1: Research & Documentation Validation (PRs 1-4)
*These PRs validate AI-generated content and establish ground truth*
*Can run in parallel - no file conflicts*

### PR #1: Validate Core Feature Analysis Documents (1-9)
✅ **PR #1 DONE - November 29, 2025**

**Files to Work On:** `docs/ANALYSIS/CORE_FEATURES/*.md`  
**Scope:** Review and validate 9 core feature analysis documents against actual implementation
**Tasks:**
1. ✅ Read each analysis document (1.MULTI_FUEL_ANALYSIS.md through 9.CUSTOMER_MANAGEMENT_ANALYSIS.md)
2. ✅ Verify claims against actual Java service/controller files
3. ✅ Add "VERIFIED" or "NEEDS_FIX" tags to each section
4. ✅ Create summary table at end of each document with:
   - Claim | Actual Status | Action Required
5. ✅ Update DOCUMENTATION_FEATURE_VERIFICATION_2025-11-25.md with findings

**Success Criteria:** ✅ All 9 documents have verification tags and summary tables

**Verification Results:**
| Document | Status |
|----------|--------|
| 1. Multi-Fuel Analysis | VERIFIED - validation now implemented |
| 2. Vehicle Registration | VERIFIED - security fixes applied |
| 3. Trip Management | VERIFIED - all validations exist |
| 4. Real-Time Tracking | VERIFIED (Ghost Feature - WebSocket missing) |
| 5. Fuel Consumption | VERIFIED - multi-fuel supported |
| 6. Document Management | VERIFIED - complete CRUD |
| 7. Route Planning | VERIFIED - backend complete |
| 8. Geofencing | VERIFIED - spatial checks work |
| 9. Customer Management | VERIFIED - full implementation |

---

### PR #2: Validate Charging Analysis Documents (B1-B6)
✅ **PR #2 DONE - January 27, 2025**

**Files to Work On:** `docs/ANALYSIS/CHARGING/*.md`  
**Scope:** Review and validate 6 charging feature analysis documents
**Tasks:**
1. ✅ Read each analysis document (B1 through B6)
2. ✅ Verify claims against `backend/evfleet-monolith/src/main/java/com/evfleet/charging/` code
3. ✅ Specifically verify B4.PAYMENT_PROCESSING_ANALYSIS.md - confirm Razorpay NOT integrated
4. ✅ Add verification status to each document
5. ✅ Document what IS implemented vs what is CLAIMED

**Success Criteria:** ✅ All 6 charging documents verified with actual implementation status

**Verification Results:**
| Document | Status | Key Findings |
|----------|--------|--------------|
| B1. Station Management | ✅ VERIFIED | Atomic operations confirmed, @Version exists, BigDecimal pricePerKwh |
| B2. Session Tracking | ✅ VERIFIED | Thread-safe slot reservation, SOC validation, vehicle type checks |
| B3. Cost Calculation | ✅ VERIFIED | BigDecimal cost calculation; Expense module CONFIRMED MISSING |
| B4. Payment Processing | ✅ VERIFIED | Mock only - NO real gateway integration (RAZORPAY/STRIPE enums exist but unused) |
| B5. Station Discovery | ✅ VERIFIED | Haversine on-the-fly confirmed; chargerType/powerOutput exist but API filters missing |
| B6. Concurrency | ✅ VERIFIED | @Version on ChargingStation, atomic decrementAvailableSlots/incrementAvailableSlots |

---

### PR #3: Validate Maintenance & Driver Analysis Documents (C1-C6, D1-D5)
✅ **PR #3 DONE - January 27, 2025**

**Files to Work On:** `docs/ANALYSIS/MAINTENANCE/*.md`, `docs/ANALYSIS/DRIVER/*.md`  
**Scope:** Review and validate 11 maintenance and driver analysis documents
**Tasks:**
1. ✅ Read all maintenance analysis documents (C1-C6)
2. ✅ Read all driver analysis documents (D1-D5)
3. ✅ Verify D4.BEHAVIOR_MONITORING_ANALYSIS.md confirms "Ghost Feature" status
4. ✅ Check D5.LICENSE_MANAGEMENT_ANALYSIS.md for actual license tracking implementation
5. ✅ Add verification tags and create summary tables

**Success Criteria:** ✅ All 11 documents verified with Section 10 Verification Summary tables

**Verification Results:**
| Document | Status | Key Findings |
|----------|--------|--------------|
| C1. Maintenance Scheduling | ✅ VERIFIED | MaintenancePolicy with mileageIntervalKm, shouldTriggerByMileage() exists |
| C2. Service History | ✅ VERIFIED | MaintenanceLineItem with PART/LABOR/TAX breakdown, auto cost calculation |
| C3. Battery Health | ✅ VERIFIED | BatteryHealthController/Service fully implemented with CRUD endpoints |
| C4. Preventive Alerts | ⚠️ PARTIAL | MaintenanceAlertResponse exists; scheduled job not fully verified |
| C5. Cost Analytics | ✅ VERIFIED | AnalyticsService.updateMaintenanceCost() integration on maintenance complete |
| C6. Multi-Fuel Maintenance | ✅ VERIFIED (Doc Outdated) | MaintenanceType enum NOW has fuel-specific types (OIL_CHANGE, BATTERY_CHECK, etc.) |
| D1. Driver Registration | ✅ VERIFIED (Doc Outdated) | License validation NOW exists with isValidLicenseNumber() regex |
| D2. Driver Assignment | ✅ VERIFIED (Doc Outdated) | DriverService NOW validates before assignment (checks currentVehicleId) |
| D3. Performance Tracking | ⚠️ PARTIAL (Doc Outdated) | Fields EXIST (safetyScore, fuelEfficiency) but telematics ingestion missing |
| D4. Behavior Monitoring | ⚠️ PARTIAL | Aggregate fields exist in Driver.java; event ingestion system missing |
| D5. License Management | ✅ VERIFIED (Doc Outdated) | LicenseExpiryJob.java runs daily @9AM with 30/15/7 day notifications! |

**Key Discovery:** Several documents were written BEFORE fixes were applied. The backend NOW has:
- ✅ Fuel-type specific maintenance types
- ✅ Driver assignment validation
- ✅ Automated license expiry notifications
- ✅ Performance metrics fields in Driver entity

---

### PR #4: Validate Analytics Documents & Create Gap Analysis Report
✅ **PR #4 DONE - November 30, 2025**

**Files to Work On:** `docs/ANALYSIS/ANALYTICS/*.md`, `docs/VERIFICATION/`  
**Scope:** Validate E1-E7 analytics documents and create master gap analysis
**Tasks:**
1. ✅ Read all analytics analysis documents (E1-E7)
2. ✅ Verify against `backend/evfleet-monolith/src/main/java/com/evfleet/analytics/` code
3. ✅ Create new file: `docs/VERIFICATION/MASTER_GAP_ANALYSIS.md`
4. ✅ Document all gaps found across all analysis documents
5. ✅ Prioritize gaps by: Critical, High, Medium, Low

**Success Criteria:** ✅ E1-E7 verified, MASTER_GAP_ANALYSIS.md created with prioritized gap list

**Verification Results:**
| Document | Status | Key Findings |
|----------|--------|--------------|
| E1. Fleet Summary | ✅ VERIFIED (Doc Outdated) | `/fleet-analytics` endpoint NOW EXISTS with comprehensive data! |
| E2. Utilization Reports | ✅ VERIFIED (Doc Outdated) | `/utilization-reports` endpoint NOW EXISTS and works! |
| E3. Cost Analytics | ✅ VERIFIED (Doc Outdated) | `/cost-analytics` and `/tco-analysis` endpoints NOW EXIST! |
| E4. TCO Analysis | ✅ VERIFIED (Doc Outdated) | TCOAnalysisService.java (365 lines) FULLY IMPLEMENTED! |
| E5. Energy Tracking | ✅ VERIFIED (Doc Outdated) | EnergyAnalyticsService.java (325 lines) FULLY IMPLEMENTED! |
| E6. PDF Generation | ✅ VERIFIED (Doc Outdated) | ReportGenerationService.java (556 lines) using Apache PDFBox! |
| E7. Historical Data | ⚠️ PARTIAL | Trend endpoints exist; ML forecasting not implemented |

**Major Discovery:** ALL 7 analytics documents claimed features were "missing" or "404 errors" - but the backend is FULLY IMPLEMENTED with comprehensive services!

**MASTER_GAP_ANALYSIS.md Summary:**
- 33 documents reviewed
- 15 outdated claims corrected
- Only 4 actual gaps remain:
  1. HIGH: Real payment gateway (mock only)
  2. HIGH: Telematics ingestion endpoint
  3. HIGH: WebSocket real-time tracking
  4. MEDIUM: ML forecasting

---

## BATCH 2: Backend Data Validation (PRs 5-8)
*Implement missing backend validation logic*
*Can run in parallel - each touches different service files*

### PR #5: Implement Multi-Fuel Validation in VehicleService
✅ **PR #5 DONE - November 30, 2025 (ALREADY IMPLEMENTED)**

**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/fleet/service/VehicleService.java`  
**Scope:** Fix the validation gap identified in 1.MULTI_FUEL_ANALYSIS.md
**Tasks:**
1. ✅ Add conditional validation for EV vehicles (require batteryCapacity, defaultChargerType)
2. ✅ Add conditional validation for ICE vehicles (require fuelTankCapacity)
3. ✅ Add conditional validation for HYBRID vehicles (require BOTH sets of fields)
4. ✅ Add SOC validation (0-100 range)
5. ✅ Add fuel level validation (cannot exceed tank capacity)
6. ✅ Create proper error messages for each validation failure
7. ✅ Add unit tests in VehicleServiceTest.java

**Success Criteria:** ✅ Vehicle creation properly validates fuel-type-specific fields

**Verification Results:**
| Feature | Status | Evidence |
|---------|--------|----------|
| EV validation | ✅ IMPLEMENTED | `VehicleService.java` lines 88-98: `validateEVFields()` |
| ICE validation | ✅ IMPLEMENTED | `VehicleService.java` lines 103-112: `validateICEFields()` |
| HYBRID validation | ✅ IMPLEMENTED | `VehicleService.java` lines 117-132: `validateHybridFields()` |
| SOC range (0-100) | ✅ IMPLEMENTED | `VehicleService.java` lines 145-150: `validateBatterySoc()` |
| Fuel level validation | ✅ IMPLEMENTED | `VehicleService.java` lines 155-164: `validateFuelLevel()` |
| Unit tests | ✅ COMPLETE | `VehicleServiceValidationTest.java` (295 lines, 15 test cases) |

**Note:** This PR was ALREADY COMPLETE - validation was implemented previously.

---

### PR #6: Implement Trip Validation & Teleportation Prevention
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/fleet/service/TripService.java`  
**Scope:** Prevent impossible trips (teleportation)
**Tasks:**
1. Add geospatial validation for trip start/end locations
2. Calculate maximum possible speed between location updates
3. Reject updates that imply speeds > 200 km/h (impossible)
4. Add trip distance calculation using Haversine formula
5. Store trip path history for replay
6. Add validation tests

**Success Criteria:** Impossible location jumps are rejected with proper error messages

---

### PR #7: Implement Charging Session Validation for 2-Wheelers
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/charging/service/ChargingSessionService.java`  
**Scope:** Implement 2-wheeler charging restriction per strategy document
**Tasks:**
1. Add vehicle type check before allowing charging session start
2. Reject charging sessions for 2-wheelers (GPS-only tracking per strategy)
3. Add proper error message explaining 2-wheeler limitation
4. Document the business rule in code comments
5. Add unit tests for the restriction
6. Update API error response to be user-friendly

**Success Criteria:** 2-wheelers cannot start charging sessions, proper error returned

---

### PR #8: Implement Driver Assignment Validation
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/driver/service/DriverService.java`  
**Scope:** Strengthen driver assignment validation
**Tasks:**
1. Verify double-assignment prevention is working
2. Add license expiry check before assignment
3. Add vehicle compatibility check (can driver's license class operate vehicle?)
4. Add shift overlap prevention
5. Create audit log entry for each assignment
6. Add comprehensive unit tests

**Success Criteria:** Driver assignments validate license, prevent overlaps, and audit changes

---

## BATCH 3: Backend Feature Implementation - Driver Module (PRs 9-12)
*Implement missing driver features*
*Can run in parallel - each creates new entity/service files*

### PR #9: Create DrivingEvent Entity and Repository
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/driver/model/`, `backend/evfleet-monolith/src/main/java/com/evfleet/driver/repository/`  
**Scope:** Create foundation for Driver Behavior Monitoring (currently Ghost Feature)
**Tasks:**
1. Create DrivingEvent.java entity with fields:
   - id, driverId, tripId, vehicleId, eventType (HARSH_BRAKING, SPEEDING, RAPID_ACCELERATION, IDLING)
   - timestamp, latitude, longitude, severity (1-10), gForce, speed
2. Create DrivingEventRepository.java with methods:
   - findByDriverIdAndTimestampBetween()
   - findByTripId()
   - countByDriverIdAndEventType()
3. Add Flyway migration for driving_events table
4. Add @Entity annotations and JPA mappings

**Success Criteria:** DrivingEvent entity created with proper JPA mappings and database migration

---

### PR #10: Create DrivingEventService for Behavior Analysis
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/driver/service/DrivingEventService.java`  
**Scope:** Implement business logic for driving event processing
**Tasks:**
1. Create DrivingEventService.java with methods:
   - recordEvent(DrivingEventRequest)
   - getEventsByTrip(Long tripId)
   - getEventsByDriver(Long driverId, LocalDateTime start, LocalDateTime end)
   - calculateDriverSafetyScore(Long driverId)
2. Implement safety score algorithm:
   - 100 base score
   - -5 per harsh braking event
   - -10 per speeding event
   - -3 per rapid acceleration
   - Minimum score: 0
3. Add service tests

**Success Criteria:** DrivingEventService calculates real safety scores from actual events

---

### PR #11: Create DrivingEventController REST API
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/driver/controller/DrivingEventController.java`  
**Scope:** Expose driving event endpoints
**Tasks:**
1. Create DrivingEventController.java with endpoints:
   - POST /api/v1/driving-events (record new event from telematics)
   - GET /api/v1/driving-events/trip/{tripId}
   - GET /api/v1/driving-events/driver/{driverId}
   - GET /api/v1/drivers/{driverId}/safety-score
2. Create DrivingEventRequest.java and DrivingEventResponse.java DTOs
3. Add Swagger documentation annotations
4. Add authentication requirements
5. Add validation (@Valid annotations)

**Success Criteria:** REST API for driving events is accessible and documented in Swagger

---

### PR #12: Create Driver License Alert Scheduler
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/driver/scheduler/LicenseExpiryScheduler.java`  
**Scope:** Implement automated license expiry alerts (D5 gap)
**Tasks:**
1. Create LicenseExpiryScheduler.java with @Scheduled annotation
2. Run daily at 6 AM to check for:
   - Licenses expiring in 30 days (LOW priority alert)
   - Licenses expiring in 7 days (MEDIUM priority alert)
   - Expired licenses (HIGH priority alert)
3. Create alerts via NotificationService
4. Add config properties for thresholds
5. Add tests with mocked clock

**Success Criteria:** Automated daily check creates alerts for expiring licenses

---

## BATCH 4: Backend Feature Implementation - Telematics (PRs 13-16)
*Implement real-time telemetry features*
*Can run in parallel - each touches different telematics files*

### PR #13: Implement Flespi Telemetry Integration
✅ **PR #13 DONE - November 30, 2025**

**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/telematics/provider/FlespiTelematicsProvider.java`  
**Scope:** Complete flespi integration that is currently ready but not tested
**Tasks:**
1. ✅ Implement getLatestTelemetry() method with actual flespi API calls
2. ✅ Add error handling for API failures
3. ✅ Add retry logic with exponential backoff
4. ✅ Add health check endpoint for flespi connection
5. ✅ Create integration test with mock flespi responses
6. ✅ Add configuration validation on startup

**Success Criteria:** ✅ Flespi provider successfully fetches telemetry from flespi API

**Implementation Details:**
| Feature | Status | Evidence |
|---------|--------|----------|
| @PostConstruct validation | ✅ IMPLEMENTED | Validates token and API URL on startup |
| @Retryable exponential backoff | ✅ IMPLEMENTED | 3 retries with 1s, 2s, 4s delays |
| Health monitoring | ✅ IMPLEMENTED | AtomicLong counters for calls/failures |
| Historical data fetching | ✅ IMPLEMENTED | Unix timestamp conversion for date ranges |
| FlespiHealthStatus DTO | ✅ IMPLEMENTED | Detailed health info with success rate |
| Health endpoint | ✅ IMPLEMENTED | GET /api/v1/telematics/health |
| Provider listing | ✅ IMPLEMENTED | GET /api/v1/telematics/providers |
| Connection test | ✅ IMPLEMENTED | POST /api/v1/telematics/providers/{id}/test |
| Unit tests | ✅ COMPLETE | FlespiTelematicsProviderTest.java (256 lines, 12 tests) |

---

### PR #14: Create Telemetry History Storage
✅ **PR #14 DONE - November 30, 2025**

**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/telematics/model/TelemetrySnapshot.java`, `backend/evfleet-monolith/src/main/java/com/evfleet/telematics/repository/TelemetrySnapshotRepository.java`  
**Scope:** Store historical telemetry data for analytics
**Tasks:**
1. ✅ Create TelemetrySnapshot.java entity with 40+ fields
2. ✅ Create TelemetrySnapshotRepository.java with 15+ query methods
3. ✅ Add Flyway migration for telemetry_snapshots table
4. ✅ Add indexes for vehicleId + timestamp queries (5 indexes)
5. ✅ Add retention policy via TelemetryRetentionScheduler (90-day auto-delete)

**Success Criteria:** ✅ Telemetry history stored and queryable with proper retention

**Implementation Details:**
| Component | Lines | Key Features |
|-----------|-------|--------------|
| TelemetrySnapshot.java | 196 | 40+ fields, fromTelemetryData() factory |
| TelemetrySnapshotRepository.java | 187 | Analytics, geospatial, retention queries |
| TelemetryRetentionScheduler.java | 128 | Daily 2 AM cleanup, batch deletion |
| V1__create_telemetry_snapshots.sql | 98 | 5 performance indexes, partial index |
| TelemetrySnapshotRepositoryTest.java | 225 | 10 test cases |

---

### PR #15: Implement Telemetry Background Sync
✅ **PR #15 DONE - November 30, 2025**

**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/telematics/scheduler/TelemetrySyncScheduler.java`  
**Scope:** Automatically fetch and store telemetry
**Tasks:**
1. ✅ Create TelemetrySyncScheduler.java with @Scheduled annotation
2. ✅ Fetch telemetry for all vehicles with DEVICE or OEM_API source
3. ✅ Store snapshots in TelemetrySnapshotRepository
4. ✅ Update Vehicle's lastTelemetryUpdate timestamp
5. ✅ Add configurable sync interval (default: 60 seconds)
6. ✅ Add metrics for sync success/failure rates
7. ✅ Add error handling with per-vehicle isolation

**Success Criteria:** ✅ Telemetry auto-syncs every minute for configured vehicles

**Implementation Details:**
| Component | Lines | Key Features |
|-----------|-------|--------------|
| TelemetrySyncScheduler.java | 380 | @Scheduled 60s, per-vehicle sync, exponential backoff |
| TelematicsController.java | +40 | 4 new endpoints: /sync, /sync/vehicle/{id}, /sync/stats |
| TelemetrySyncSchedulerTest.java | 340 | 13 test cases covering all scenarios |

| Feature | Status |
|---------|--------|
| Auto-sync every 60s | ✅ @Scheduled(fixedRateString = "${telematics.sync.interval-ms:60000}") |
| Per-vehicle error isolation | ✅ Try-catch per vehicle, failures don't affect others |
| Exponential backoff | ✅ 1m → 2m → 4m → 8m → max 30m for failing vehicles |
| Micrometer metrics | ✅ success/failure counters, duration timer, snapshots saved |
| Manual sync trigger | ✅ POST /api/v1/telematics/sync |
| Vehicle-specific sync | ✅ POST /api/v1/telematics/sync/vehicle/{id} |
| Sync statistics | ✅ GET /api/v1/telematics/sync/stats |
| Backoff reset | ✅ POST /api/v1/telematics/sync/vehicle/{id}/reset-backoff |

---

### PR #16: Create Telemetry Alert System
✅ **PR #16 DONE - November 30, 2025**

**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/telematics/service/TelemetryAlertService.java`  
**Scope:** Generate alerts from telemetry data
**Tasks:**
1. ✅ Create TelemetryAlertService.java (460+ lines)
2. ✅ Implement alert rules:
   - Low battery (< 20%) - MEDIUM alert
   - Critical battery (< 10%) - HIGH alert
   - Geofence breach - HIGH alert
   - Excessive speed (> 120 km/h) - MEDIUM alert
   - Connection lost (no update in 1 hour) - LOW alert
3. ✅ Integrate with NotificationService
4. ✅ Add configurable thresholds via @Value properties
5. ✅ Add alert deduplication (30 minute cooldown between duplicate alerts)

**Success Criteria:** ✅ Telemetry-based alerts generated and sent to users

**Implementation Details:**
| Component | Lines | Key Features |
|-----------|-------|--------------|
| TelemetryAlert.java | 125 | Entity with AlertType, AlertPriority, AlertStatus enums |
| TelemetryAlertRepository.java | 85 | 15+ queries: findByVehicle, checkDuplicate, countByCompany |
| TelemetryAlertService.java | 460 | Alert rules, deduplication, NotificationService integration |
| TelemetryAlertController.java | 145 | GET /alerts, acknowledge, resolve endpoints |
| V2__create_telemetry_alerts.sql | 50 | Migration with indexes on vehicle_id, company_id, status |
| TelemetrySyncScheduler.java | +15 | Integration to call checkAndGenerateAlerts() |
| TelemetryAlertServiceTest.java | 300 | 12 test cases |

| Feature | Status | Details |
|---------|--------|---------|
| Alert types | ✅ | LOW_BATTERY, CRITICAL_BATTERY, GEOFENCE_BREACH, EXCESSIVE_SPEED, CONNECTION_LOST |
| Alert priorities | ✅ | LOW, MEDIUM, HIGH, CRITICAL |
| Alert statuses | ✅ | ACTIVE, ACKNOWLEDGED, RESOLVED, DISMISSED |
| Deduplication | ✅ | 30-minute cooldown between duplicate alerts |
| Threshold config | ✅ | @Value("${telematics.alert.low-battery-threshold:20}") |
| Auto-resolve | ✅ | Alerts auto-resolve when condition clears |
| Sync integration | ✅ | TelemetrySyncScheduler calls checkAndGenerateAlerts() |

---

## BATCH 5: Backend Feature Implementation - Billing (PRs 17-20)
*Implement payment and billing features*
*Can run in parallel - each touches different billing files*

### PR #17: Create Razorpay Integration Service
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/billing/service/RazorpayPaymentService.java`  
**Scope:** Implement actual Razorpay integration (currently mock)
**Tasks:**
1. Create RazorpayPaymentService.java with:
   - createOrder(amount, currency, invoiceId)
   - verifyPayment(razorpayPaymentId, razorpayOrderId, signature)
   - refundPayment(paymentId, amount)
2. Add Razorpay SDK dependency to pom.xml
3. Add configuration properties for API keys
4. Add signature verification for webhook callbacks
5. Create integration tests with Razorpay test mode
6. Add proper exception handling

**Note:** This PR creates infrastructure. Actual integration requires Razorpay credentials.

**Success Criteria:** Razorpay service ready for production with test mode validation

---

### PR #18: Create Invoice Payment Workflow
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/billing/service/InvoicePaymentService.java`  
**Scope:** Complete invoice-to-payment workflow
**Tasks:**
1. Create InvoicePaymentService.java with:
   - initiatePayment(invoiceId) - creates Razorpay order
   - handlePaymentSuccess(webhookPayload)
   - handlePaymentFailure(webhookPayload)
2. Add payment status tracking (PENDING, PROCESSING, COMPLETED, FAILED)
3. Add email notification on payment success
4. Create payment receipt generation
5. Add partial payment support
6. Add unit tests

**Success Criteria:** Complete payment workflow from invoice to receipt

---

### PR #19: Create Webhook Controller for Payment Callbacks
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/billing/controller/PaymentWebhookController.java`  
**Scope:** Handle payment gateway callbacks
**Tasks:**
1. Create PaymentWebhookController.java with:
   - POST /api/v1/webhooks/razorpay
   - Signature verification before processing
   - Idempotent handling (same webhook = same result)
2. Add webhook event logging for audit
3. Add retry logic for transient failures
4. Create secure endpoint (IP whitelist option)
5. Add tests with mock webhook payloads

**Success Criteria:** Payment webhooks processed securely with audit trail

---

### PR #20: Implement Subscription Auto-Renewal
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/billing/scheduler/SubscriptionRenewalScheduler.java`  
**Scope:** Automate subscription renewals
**Tasks:**
1. Create SubscriptionRenewalScheduler.java
2. Daily check for subscriptions expiring in 7 days
3. Send renewal reminder email
4. On expiry date: generate new invoice
5. Grace period handling (3 days after expiry)
6. Service suspension after grace period
7. Add tests with mocked dates

**Success Criteria:** Subscriptions auto-generate invoices and handle grace periods

---

## BATCH 6: Backend Feature Implementation - Analytics (PRs 21-24)
*Enhance analytics and reporting*
*Can run in parallel - each touches different analytics files*

### PR #21: Create Historical Data Aggregation Service
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/analytics/service/HistoricalDataService.java`  
**Scope:** Implement E7 Historical Data Analysis
**Tasks:**
1. Create HistoricalDataService.java with:
   - aggregateDailyMetrics(date) - fleet summary for a day
   - aggregateMonthlyMetrics(yearMonth)
   - calculateTrends(metricType, period)
2. Store aggregated data in new historical_metrics table
3. Add trend calculation (% change from previous period)
4. Add data retention policy (5 years)
5. Create unit tests

**Success Criteria:** Historical data aggregated and trends calculated

---

### PR #22: Enhance TCO Analysis with Multi-Fuel Support
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/analytics/service/TCOAnalysisService.java`  
**Scope:** Improve TCO calculations for different fuel types
**Tasks:**
1. Add fuel-type-specific cost calculations:
   - EV: electricity cost per kWh, battery depreciation
   - ICE: fuel cost per liter, oil change costs
   - Hybrid: combined calculations
2. Add regional cost variations (fuel prices by city)
3. Add carbon cost calculations (for ESG reporting)
4. Add comparison mode (ICE vs EV equivalent)
5. Add projection calculations (5-year TCO forecast)
6. Update tests

**Success Criteria:** TCO analysis accounts for all fuel types with projections

---

### PR #23: Create ESG Reporting Module
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/analytics/service/ESGReportService.java`  
**Scope:** Implement sustainability reporting (market requirement)
**Tasks:**
1. Create ESGReportService.java with:
   - calculateCarbonFootprint(fleetId, period)
   - generateComplianceReport(fleetId, reportType)
   - trackEmissionsTrend()
2. Calculate CO2 savings from EV vs ICE baseline
3. Add government compliance report formats
4. Add export to CSV/PDF
5. Add scheduled monthly ESG report generation
6. Add tests

**Success Criteria:** ESG reports generated with carbon footprint tracking

---

### PR #24: Create Real-Time Dashboard Metrics Endpoint
**Files to Work On:** `backend/evfleet-monolith/src/main/java/com/evfleet/analytics/controller/DashboardMetricsController.java`  
**Scope:** Optimize dashboard data loading
**Tasks:**
1. Create DashboardMetricsController.java with:
   - GET /api/v1/dashboard/summary - cached summary metrics
   - GET /api/v1/dashboard/live - real-time vehicle positions
2. Add Redis caching for summary metrics (5-minute TTL)
3. Add response compression
4. Add ETag support for conditional requests
5. Create lightweight DTOs (minimize payload size)
6. Add performance tests

**Success Criteria:** Dashboard loads in < 500ms with cached metrics

---

## BATCH 7: Frontend Enhancement - Driver Module (PRs 25-28)
*Implement missing driver UI features*
*Can run in parallel - each creates new component files*

### PR #25: Create Driver Behavior Dashboard Component
**Files to Work On:** `frontend/src/components/drivers/DriverBehaviorDashboard.tsx`  
**Scope:** UI for driver behavior monitoring (currently Ghost Feature)
**Tasks:**
1. Create DriverBehaviorDashboard.tsx component
2. Display safety score with visual gauge (0-100)
3. Show recent driving events table (last 7 days)
4. Add event type breakdown chart (pie chart)
5. Add trend chart (safety score over time)
6. Add filtering by date range
7. Add export to CSV option
8. Create tests with React Testing Library

**Success Criteria:** Driver behavior visible in UI with charts and tables

---

### PR #26: Create Driver Safety Leaderboard Component
**Files to Work On:** `frontend/src/components/drivers/SafetyLeaderboard.tsx`  
**Scope:** Gamification - show driver rankings
**Tasks:**
1. Create SafetyLeaderboard.tsx component
2. Display ranked list of drivers by safety score
3. Show badges/achievements:
   - 🏆 Safe Driver (90+ score)
   - ⚡ Speed Demon (most speeding events - warning badge)
   - 🌟 Perfect Week (no incidents in 7 days)
4. Add filtering by time period (week/month/quarter)
5. Add click to view driver details
6. Add animations for rank changes
7. Create tests

**Success Criteria:** Leaderboard encourages safe driving through gamification

---

### PR #27: Create Driver License Management UI
**Files to Work On:** `frontend/src/components/drivers/LicenseManagement.tsx`  
**Scope:** UI for license tracking and alerts
**Tasks:**
1. Create LicenseManagement.tsx component
2. Display license details (number, class, expiry)
3. Show expiry countdown with color coding:
   - Green: > 30 days
   - Yellow: 7-30 days
   - Red: < 7 days or expired
4. Add license upload functionality (image)
5. Add renewal reminder button
6. Add license verification status
7. Create tests

**Success Criteria:** License management UI with expiry tracking

---

### PR #28: Create Driver Assignment Calendar View
**Files to Work On:** `frontend/src/components/drivers/AssignmentCalendar.tsx`  
**Scope:** Calendar view for driver assignments
**Tasks:**
1. Create AssignmentCalendar.tsx using FullCalendar library
2. Show driver-vehicle assignments by date
3. Color code by vehicle type
4. Add drag-and-drop to reassign
5. Add conflict detection (highlight overlaps)
6. Add quick-add assignment modal
7. Add week/month view toggle
8. Create tests

**Success Criteria:** Visual calendar for managing driver schedules

---

## BATCH 8: Frontend Enhancement - Charging Module (PRs 29-32)
*Improve charging station discovery and management*
*Can run in parallel - each creates new component files*

### PR #29: Create Charging Station Map with Real-Time Status
**Files to Work On:** `frontend/src/components/charging/ChargingStationMap.tsx`  
**Scope:** Interactive map for charging stations
**Tasks:**
1. Create ChargingStationMap.tsx using Mapbox/Leaflet
2. Display all charging stations with markers
3. Color code by availability (green=available, red=occupied, gray=offline)
4. Add clustering for many stations
5. Add click to view station details
6. Add "Route to Station" button
7. Add current vehicle location (if available)
8. Create tests

**Success Criteria:** Interactive map shows station availability visually

---

### PR #30: Create Charging Cost Comparison Component
**Files to Work On:** `frontend/src/components/charging/CostComparison.tsx`  
**Scope:** Help users find cheapest charging option
**Tasks:**
1. Create CostComparison.tsx component
2. Display nearby stations with pricing
3. Sort by: price, distance, availability
4. Show price per kWh and estimated total cost
5. Add time-of-use pricing indicator
6. Add "Cheapest Now" highlight
7. Add historical price chart
8. Create tests

**Success Criteria:** Users can easily compare charging costs

---

### PR #31: Create Charging Session History Component
**Files to Work On:** `frontend/src/components/charging/SessionHistory.tsx`  
**Scope:** View past charging sessions
**Tasks:**
1. Create SessionHistory.tsx component
2. Display sessions in table with:
   - Date, Station, Duration, Energy (kWh), Cost
3. Add filtering by date range, vehicle, station
4. Add summary stats (total cost, total energy)
5. Add export to CSV
6. Add click to view session details
7. Add chart for energy consumption trend
8. Create tests

**Success Criteria:** Users can review and analyze charging history

---

### PR #32: Create Payment Method Management UI
**Files to Work On:** `frontend/src/components/billing/PaymentMethods.tsx`  
**Scope:** Manage payment methods for charging
**Tasks:**
1. Create PaymentMethods.tsx component
2. List saved payment methods (cards)
3. Add new card (Razorpay SDK integration)
4. Set default payment method
5. Delete payment method with confirmation
6. Show last 4 digits only (security)
7. Add card type icons (Visa, Mastercard, etc.)
8. Create tests

**Success Criteria:** Users can manage payment methods securely

---

## BATCH 9: Frontend Enhancement - Analytics (PRs 33-36)
*Enhance analytics dashboard*
*Can run in parallel - each creates new component files*

### PR #33: Create Interactive Fleet Summary Dashboard
**Files to Work On:** `frontend/src/components/analytics/FleetSummaryDashboard.tsx`  
**Scope:** Enhanced fleet overview with drill-down
**Tasks:**
1. Create FleetSummaryDashboard.tsx component
2. Add clickable KPI cards:
   - Total Vehicles → Vehicle List
   - Active Now → Map View
   - On Charge → Charging Sessions
   - In Maintenance → Maintenance Schedule
3. Add real-time updates (polling/WebSocket)
4. Add date range selector
5. Add comparison mode (vs previous period)
6. Add export options (PDF, PNG, CSV)
7. Create tests

**Success Criteria:** Interactive dashboard with drill-down capabilities

---

### PR #34: Create Vehicle Utilization Heatmap
**Files to Work On:** `frontend/src/components/analytics/UtilizationHeatmap.tsx`  
**Scope:** Visual representation of fleet utilization
**Tasks:**
1. Create UtilizationHeatmap.tsx using D3.js or Recharts
2. Show utilization by:
   - Hour of day (x-axis) vs Day of week (y-axis)
   - Color intensity = usage level
3. Add vehicle filter
4. Add click on cell to see details
5. Add threshold indicators (underutilized, optimal, overutilized)
6. Add export as image
7. Create tests

**Success Criteria:** Utilization patterns visible at a glance

---

### PR #35: Create Cost Analytics Dashboard
**Files to Work On:** `frontend/src/components/analytics/CostAnalyticsDashboard.tsx`  
**Scope:** Comprehensive cost visualization
**Tasks:**
1. Create CostAnalyticsDashboard.tsx component
2. Add cost breakdown pie chart:
   - Energy, Maintenance, Insurance, Driver, Depreciation
3. Add cost trend line chart (by month)
4. Add cost per km/mile calculation
5. Add vehicle comparison table
6. Add budget vs actual tracking
7. Add anomaly detection highlighting
8. Create tests

**Success Criteria:** All fleet costs visible with trend analysis

---

### PR #36: Create ESG Report Generator UI
**Files to Work On:** `frontend/src/components/analytics/ESGReportGenerator.tsx`  
**Scope:** UI for generating ESG reports
**Tasks:**
1. Create ESGReportGenerator.tsx component
2. Add report type selector:
   - Carbon Footprint Report
   - Emissions Comparison Report
   - Sustainability Summary
3. Add date range picker
4. Add vehicle/fleet selection
5. Add preview mode
6. Add download as PDF
7. Add schedule recurring reports
8. Create tests

**Success Criteria:** Users can generate and download ESG reports

---

## BATCH 10: Frontend Enhancement - Maintenance (PRs 37-40)
*Improve maintenance management UI*
*Can run in parallel - each creates new component files*

### PR #37: Create Maintenance Calendar with Alerts
**Files to Work On:** `frontend/src/components/maintenance/MaintenanceCalendar.tsx`  
**Scope:** Calendar view for maintenance scheduling
**Tasks:**
1. Create MaintenanceCalendar.tsx using FullCalendar
2. Display scheduled maintenance items
3. Color code by type (routine, emergency, inspection)
4. Color code by priority (overdue=red, due soon=yellow)
5. Add drag-and-drop to reschedule
6. Add quick-add maintenance modal
7. Add today's tasks highlight
8. Create tests

**Success Criteria:** Visual calendar for maintenance planning

---

### PR #38: Create Predictive Maintenance Alerts Component
**Files to Work On:** `frontend/src/components/maintenance/PredictiveAlerts.tsx`  
**Scope:** Show AI-predicted maintenance needs
**Tasks:**
1. Create PredictiveAlerts.tsx component
2. Display predicted maintenance needs:
   - Based on mileage/time intervals
   - Based on battery health trends
   - Based on usage patterns
3. Show confidence level (%)
4. Show estimated cost
5. Add "Schedule Now" action
6. Add dismiss/snooze options
7. Create tests

**Success Criteria:** Proactive maintenance alerts before failures

---

### PR #39: Create Battery Health Dashboard
**Files to Work On:** `frontend/src/components/maintenance/BatteryHealthDashboard.tsx`  
**Scope:** Comprehensive battery monitoring UI
**Tasks:**
1. Create BatteryHealthDashboard.tsx component
2. Show for each EV vehicle:
   - State of Health (SOH) %
   - Charge cycles count
   - Temperature history
   - Degradation trend chart
3. Add fleet-wide battery health comparison
4. Add low SOH vehicle highlight
5. Add battery replacement cost estimate
6. Add export report
7. Create tests

**Success Criteria:** Battery health visible for entire EV fleet

---

### PR #40: Create Service History Timeline
**Files to Work On:** `frontend/src/components/maintenance/ServiceHistoryTimeline.tsx`  
**Scope:** Visual timeline of vehicle service history
**Tasks:**
1. Create ServiceHistoryTimeline.tsx component
2. Display service events on timeline:
   - Date, Type, Cost, Provider, Notes
3. Add icons for service types
4. Add click to expand details
5. Add filtering by type, date range
6. Add cost summary at bottom
7. Add export to PDF
8. Create tests

**Success Criteria:** Complete vehicle service history in timeline format

---

## BATCH 11: Testing & Quality (PRs 41-44)
*Add comprehensive testing*
*Can run in parallel - each touches different test files*

### PR #41: Create Backend Controller Unit Tests
**Files to Work On:** `backend/evfleet-monolith/src/test/java/com/evfleet/*/controller/*Test.java`  
**Scope:** Unit tests for all controllers
**Tasks:**
1. Create tests for VehicleController (8+ tests)
2. Create tests for DriverController (6+ tests)
3. Create tests for ChargingSessionController (6+ tests)
4. Create tests for MaintenanceController (6+ tests)
5. Use @WebMvcTest annotation
6. Mock service layer
7. Test all endpoints (GET, POST, PUT, DELETE)
8. Test error cases (404, 400, 401)

**Success Criteria:** 80%+ controller test coverage

---

### PR #42: Create Backend Service Unit Tests
**Files to Work On:** `backend/evfleet-monolith/src/test/java/com/evfleet/*/service/*Test.java`  
**Scope:** Unit tests for all services
**Tasks:**
1. Create tests for VehicleService (10+ tests)
2. Create tests for DriverService (8+ tests)
3. Create tests for ChargingSessionService (8+ tests)
4. Create tests for MaintenanceService (8+ tests)
5. Mock repository layer
6. Test business logic scenarios
7. Test edge cases (empty lists, null values)
8. Test validation logic

**Success Criteria:** 80%+ service test coverage

---

### PR #43: Create Frontend Component Tests
**Files to Work On:** `frontend/src/**/*.test.tsx`  
**Scope:** Unit tests for React components
**Tasks:**
1. Create tests for DashboardPage (5+ tests)
2. Create tests for FleetManagementPage (5+ tests)
3. Create tests for ChargingPage (5+ tests)
4. Create tests for MaintenancePage (5+ tests)
5. Use React Testing Library
6. Test component rendering
7. Test user interactions
8. Test error states

**Success Criteria:** 60%+ frontend test coverage

---

### PR #44: Create Integration Tests
**Files to Work On:** `backend/evfleet-monolith/src/test/java/com/evfleet/integration/*IT.java`  
**Scope:** End-to-end API tests
**Tasks:**
1. Create VehicleIntegrationTest (CRUD flow)
2. Create DriverIntegrationTest (assignment flow)
3. Create ChargingIntegrationTest (session flow)
4. Create AuthIntegrationTest (login flow)
5. Use @SpringBootTest
6. Use TestContainers for database
7. Test realistic scenarios
8. Test error handling

**Success Criteria:** Critical user flows have integration tests

---

## BATCH 12: Testing & Documentation (PRs 45-48)
*Complete testing and documentation*
*Can run in parallel - each touches different files*

### PR #45: Create API Documentation with Examples
**Files to Work On:** `docs/API/`, Swagger annotations  
**Scope:** Comprehensive API documentation
**Tasks:**
1. Create `docs/API/API_REFERENCE.md` with all endpoints
2. Add request/response examples for each endpoint
3. Add authentication documentation
4. Add error code reference
5. Add rate limiting documentation
6. Add Postman collection export
7. Update Swagger annotations in controllers
8. Add API versioning documentation

**Success Criteria:** Complete API reference with examples

---

### PR #46: Create User Guide Documentation
**Files to Work On:** `docs/GUIDES/USER_GUIDE.md`  
**Scope:** End-user documentation
**Tasks:**
1. Create comprehensive user guide
2. Add Getting Started section
3. Add Fleet Management walkthrough
4. Add Charging Management walkthrough
5. Add Maintenance Management walkthrough
6. Add Analytics walkthrough
7. Add screenshots for each feature
8. Add FAQ section

**Success Criteria:** Non-technical users can follow guide

---

### PR #47: Create Developer Onboarding Guide
**Files to Work On:** `docs/GUIDES/DEVELOPER_GUIDE.md`  
**Scope:** Developer documentation
**Tasks:**
1. Create developer onboarding guide
2. Add local development setup
3. Add code style guidelines
4. Add PR process documentation
5. Add testing guidelines
6. Add debugging tips
7. Add architecture decision records
8. Add contribution guidelines

**Success Criteria:** New developers can onboard quickly

---

### PR #48: Create Runbook for Operations
**Files to Work On:** `docs/GUIDES/RUNBOOK.md`  
**Scope:** Operations documentation
**Tasks:**
1. Create operations runbook
2. Add deployment procedures
3. Add rollback procedures
4. Add monitoring setup
5. Add alerting rules
6. Add incident response procedures
7. Add backup/restore procedures
8. Add troubleshooting guide

**Success Criteria:** Operations team can handle incidents

---

## BATCH 13: Infrastructure & DevOps (PRs 49-50)
*Set up production-ready infrastructure*
*Can run in parallel - different configuration files*

### PR #49: Create Docker Production Configuration
**Files to Work On:** `docker/docker-compose.prod.yml`, `docker/Dockerfile.*`  
**Scope:** Production-ready Docker setup
**Tasks:**
1. Create docker-compose.prod.yml
2. Add health checks for all services
3. Add resource limits (CPU, memory)
4. Add logging configuration (JSON format)
5. Add secrets management
6. Add reverse proxy (nginx)
7. Add SSL/TLS configuration
8. Add database backup volume

**Success Criteria:** Production deployment ready with Docker

---

### PR #50: Create GitHub Actions CI/CD Pipeline
**Files to Work On:** `.github/workflows/ci.yml`, `.github/workflows/cd.yml`  
**Scope:** Automated CI/CD pipeline
**Tasks:**
1. Create CI workflow:
   - Lint (ESLint, Checkstyle)
   - Build (Maven, npm)
   - Test (JUnit, Jest)
   - Code coverage report
2. Create CD workflow:
   - Build Docker images
   - Push to registry
   - Deploy to staging
3. Add branch protection rules documentation
4. Add status badges to README

**Success Criteria:** PRs automatically built and tested

---

## Summary Table

| Batch | PRs | Focus Area | Files Changed | Dependencies |
|-------|-----|------------|---------------|--------------|
| 1 | 1-4 | Documentation Validation | docs/*.md | None |
| 2 | 5-8 | Backend Validation | service/*.java | None |
| 3 | 9-12 | Driver Module Backend | driver/**/*.java | Batch 2 ideally complete |
| 4 | 13-16 | Telematics Backend | telematics/**/*.java | None |
| 5 | 17-20 | Billing Backend | billing/**/*.java | None |
| 6 | 21-24 | Analytics Backend | analytics/**/*.java | None |
| 7 | 25-28 | Driver Module Frontend | components/drivers/*.tsx | Batch 3 ideally complete |
| 8 | 29-32 | Charging Frontend | components/charging/*.tsx | None |
| 9 | 33-36 | Analytics Frontend | components/analytics/*.tsx | Batch 6 ideally complete |
| 10 | 37-40 | Maintenance Frontend | components/maintenance/*.tsx | None |
| 11 | 41-44 | Testing | **/*Test.java, *.test.tsx | Batches 2-6 complete |
| 12 | 45-48 | Documentation | docs/**/*.md | Most PRs complete |
| 13 | 49-50 | Infrastructure | docker/*, .github/* | None |

---

## Execution Guidelines

### For Parallel Execution
1. Start with Batch 1 (PRs 1-4) - all can run in parallel
2. After Batch 1, start Batches 2-6 in parallel (backend work)
3. After backend batches, start Batches 7-10 in parallel (frontend work)
4. Batches 11-13 can run after most code is complete

### For Each PR
1. Copilot Coding Agent should:
   - Read this document for PR scope
   - Follow existing code patterns
   - Add tests for new code
   - Update documentation
   - Mark PR as done in this document

### Conflict Prevention
- PRs in same batch modify different files
- Frontend PRs create new component files
- Backend PRs modify different service/controller files
- Documentation PRs touch different .md files

---

## Notes on AI-Generated Content

Based on analysis, the following documentation areas need verification:

1. **Claims that need validation:**
   - Multi-fuel support "90% complete" - actually missing validation
   - Driver behavior "backend ready" - actually Ghost Feature
   - Real-time tracking "implemented" - WebSocket not configured

2. **Content that appears accurate:**
   - Maintenance module (C1-C6) - well implemented
   - Analytics module (E1-E7) - mostly implemented
   - Customer management - fully implemented

3. **Content that cannot be implemented without external dependencies:**
   - Razorpay payment processing - requires API keys
   - Flespi telemetry - requires account setup
   - OEM APIs - requires partnerships

---

**Document Maintained By:** GitHub Copilot Coding Agent  
**Last Updated:** November 29, 2025
