# COPILOT PR EXECUTION CHECKLIST

**How to Use:** Print this out or keep it open. Check off as you complete each PR.

---

## 🚀 STARTUP (One-Time)

- [ ] Read `MASTER_COPILOT_CONTEXT.md` completely
- [ ] Understand the 3-tier pricing model
- [ ] Know the 18 PR sequence
- [ ] Have file structure reference ready
- [ ] Paste MASTER_COPILOT_CONTEXT into Copilot

**Status: ___________**

---

## PHASE 1: DATABASE & DATA MODEL (Weeks 1-2)

### PR #1: Add Vehicle Fuel Type Support to Database
**Duration:** 3-4 days | **Effort:** 70 hours | **Blockers:** NONE
**Dependency Chain:** Blocks all other PRs

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-1-vehicle-fuel-type`
- [ ] Review database schema in MIGRATION_STRATEGY_GENERAL_EV.md
- [ ] Read "PR #1" section completely

**Copilot Work:**
- [ ] Paste PR #1 prompt from COPILOT_QUICK_START.md
- [ ] Generate Flyway migrations (V2, V3)
- [ ] Generate Vehicle entity updates
- [ ] Generate FuelType enum
- [ ] Generate FuelConsumption entity
- [ ] Review generated code for correctness

**Testing:**
- [ ] Write unit tests for Vehicle model
- [ ] Write migration tests
- [ ] Test backward compatibility (existing EV vehicles)
- [ ] Run: `mvn clean test` (should pass)
- [ ] Database migrations execute without errors
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] No breaking changes
- [ ] Create PR with description from template
- [ ] Wait for code review approval

**Status: ___________** (PENDING → IN PROGRESS → COMPLETE)

---

### PR #2: Create Feature Flag System for EV Features
**Duration:** 2-3 days | **Effort:** 80 hours | **Blockers:** PR #1

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-2-feature-flags`
- [ ] PR #1 must be merged
- [ ] Review feature toggle concept

**Copilot Work:**
- [ ] Paste PR #2 prompt from COPILOT_QUICK_START.md
- [ ] Generate FeatureToggle entity
- [ ] Generate FeatureToggleRepository
- [ ] Generate FeatureToggleService
- [ ] Generate @RequireFeature annotation
- [ ] Generate V4 migration with default features

**Testing:**
- [ ] Unit tests for FeatureToggleService
- [ ] Test isFeatureEnabled() for EV, ICE, HYBRID
- [ ] Test getAvailableFeatures() for each type
- [ ] Integration test with database
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #3: Extend Telemetry Data Model for Multi-Fuel Support
**Duration:** 3-4 days | **Effort:** 75 hours | **Blockers:** PR #1

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-3-telemetry-multi-fuel`
- [ ] PR #1 must be merged
- [ ] Understand ICE vs EV telemetry fields

**Copilot Work:**
- [ ] Paste PR #3 prompt from COPILOT_QUICK_START.md
- [ ] Generate TelemetryType enum
- [ ] Extend TelemetryData entity with fuel fields
- [ ] Generate V5 migration
- [ ] Add new repository query methods

**Testing:**
- [ ] Unit tests for TelemetryData with fuel fields
- [ ] Integration tests for telemetry persistence
- [ ] Test queries for fuel level filtering
- [ ] Test indexes are created
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #4: Update Vehicle Service Repository Queries for Multi-Fuel Filtering
**Duration:** 2-3 days | **Effort:** 50 hours | **Blockers:** PRs #1-3

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-4-vehicle-queries`
- [ ] PRs #1-3 must be merged
- [ ] Review query patterns in existing code

**Copilot Work:**
- [ ] Paste PR #4 prompt from COPILOT_QUICK_START.md
- [ ] Generate new repository methods
- [ ] Generate service methods
- [ ] Generate API endpoints
- [ ] Generate DTOs if needed

**Testing:**
- [ ] Unit tests for all new repository methods
- [ ] Integration tests for service methods
- [ ] API endpoint tests (MockMvc)
- [ ] Performance test for fleet composition (1000 vehicles)
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Query performance acceptable (< 500ms)
- [ ] Create PR

**Status: ___________**

---

## PHASE 2: API ENHANCEMENTS (Weeks 3-4)

### PR #5: Update Vehicle CRUD APIs to Support Fuel Type
**Duration:** 3-4 days | **Effort:** 100 hours | **Blockers:** PR #1

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-5-vehicle-crud-apis`
- [ ] PR #1 must be merged
- [ ] Review DTO patterns

**Copilot Work:**
- [ ] Paste PR #5 prompt from COPILOT_QUICK_START.md
- [ ] Generate VehicleCreateRequestDTO
- [ ] Generate VehicleResponseDTO
- [ ] Generate FuelTypeValidator
- [ ] Update VehicleController endpoints
- [ ] Update Swagger documentation

**Testing:**
- [ ] Test POST with EV (should require batteryCapacity)
- [ ] Test POST with ICE (should require fuelTankCapacity)
- [ ] Test POST with HYBRID (should require both)
- [ ] Test validation errors
- [ ] API endpoint tests
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #6: Extend Telemetry APIs for Multi-Fuel Metrics
**Duration:** 2-3 days | **Effort:** 80 hours | **Blockers:** PR #3

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-6-telemetry-apis`
- [ ] PR #3 must be merged
- [ ] Review telemetry flow

**Copilot Work:**
- [ ] Paste PR #6 prompt from COPILOT_QUICK_START.md
- [ ] Generate TelemetryRequestDTO
- [ ] Generate TelemetryValidator
- [ ] Generate TelemetryProcessingService
- [ ] Update TelemetryController

**Testing:**
- [ ] Test EV telemetry submission (battery fields)
- [ ] Test ICE telemetry submission (fuel fields)
- [ ] Test HYBRID telemetry (both fields)
- [ ] Test validation errors
- [ ] Performance test (1000 msgs/sec)
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #7: Create Multi-Fuel Trip Analytics Endpoints
**Duration:** 2-3 days | **Effort:** 95 hours | **Blockers:** PRs #5-6

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-7-trip-analytics`
- [ ] PRs #5-6 should be merged
- [ ] Review cost calculation requirements

**Copilot Work:**
- [ ] Paste PR #7 prompt from COPILOT_QUICK_START.md
- [ ] Generate EVCostCalculator
- [ ] Generate ICECostCalculator
- [ ] Generate MultiFleetAnalyticsService
- [ ] Add analytics endpoints
- [ ] Update Trip entity

**Testing:**
- [ ] Test EV cost calculation (kWh * ₹/kWh)
- [ ] Test ICE cost calculation (liters * ₹/liter)
- [ ] Test carbon footprint calculations
- [ ] Test analytics endpoints
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #8: Conditional Feature Availability in Trip APIs
**Duration:** 1-2 days | **Effort:** 55 hours | **Blockers:** PR #2

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-8-trip-features`
- [ ] PR #2 must be merged
- [ ] Understand feature flag integration

**Copilot Work:**
- [ ] Paste PR #8 prompt from COPILOT_QUICK_START.md
- [ ] Generate AvailableFeaturesDTO
- [ ] Update TripService
- [ ] Add endpoint to get available features

**Testing:**
- [ ] Test available features for EV vehicle
- [ ] Test available features for ICE vehicle
- [ ] Test available features for HYBRID vehicle
- [ ] Verify feature flags integration
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

## PHASE 3: CHARGING SERVICE (Week 5)

### PR #9: Update Charging Service to Handle EV/Hybrid Only
**Duration:** 2-3 days | **Effort:** 40 hours | **Blockers:** PR #5

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-9-charging-validation`
- [ ] PR #5 must be merged
- [ ] Review ChargingSessionController

**Copilot Work:**
- [ ] Paste PR #9 prompt from COPILOT_QUICK_START.md
- [ ] Generate VehicleTypeValidator
- [ ] Generate NotAnEVVehicleException
- [ ] Update controller methods
- [ ] Add exception handler

**Testing:**
- [ ] Test charging start with EV (should succeed)
- [ ] Test charging start with ICE (should fail with 400)
- [ ] Test error message clarity
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #10: Extend Charging Session Analytics
**Duration:** 1-2 days | **Effort:** 30 hours | **Blockers:** PR #9

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-10-charging-analytics`
- [ ] PR #9 should be merged
- [ ] Review analytics requirements

**Copilot Work:**
- [ ] Paste PR #10 prompt from COPILOT_QUICK_START.md
- [ ] Generate ChargingAnalyticsService
- [ ] Add analytics endpoints

**Testing:**
- [ ] Test analytics calculation
- [ ] Test endpoint returns data
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

## PHASE 4: MAINTENANCE SERVICE (Weeks 5-6)

### PR #11: Extend Maintenance Service for ICE-Specific Services
**Duration:** 3-4 days | **Effort:** 70 hours | **Blockers:** PR #1

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-11-ice-maintenance`
- [ ] PR #1 must be merged
- [ ] Review maintenance types needed

**Copilot Work:**
- [ ] Paste PR #11 prompt from COPILOT_QUICK_START.md
- [ ] Generate MaintenanceType enum
- [ ] Generate MaintenanceScheduleBuilder
- [ ] Generate V migration with ICE schedules

**Testing:**
- [ ] Test schedule generation for EV vehicle
- [ ] Test schedule generation for ICE vehicle
- [ ] Test schedule generation for HYBRID vehicle
- [ ] Verify all schedules created
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #12: Create Multi-Fuel Cost Tracking for Maintenance
**Duration:** 1-2 days | **Effort:** 45 hours | **Blockers:** PR #11

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-12-maintenance-costs`
- [ ] PR #11 should be merged

**Copilot Work:**
- [ ] Paste PR #12 prompt from COPILOT_QUICK_START.md
- [ ] Generate MaintenanceCostAnalyticsService

**Testing:**
- [ ] Test cost tracking per fuel type
- [ ] Test analytics calculations
- [ ] Test coverage > 85%

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

## PHASE 5: FRONTEND UPDATES (Weeks 7-8)

### PR #13: Update Vehicle Form Components for Multi-Fuel Selection
**Duration:** 3-4 days | **Effort:** 100 hours | **Blockers:** PR #5

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-13-vehicle-forms`
- [ ] PR #5 should be merged
- [ ] Understand React form patterns

**Copilot Work:**
- [ ] Paste PR #13 prompt from COPILOT_QUICK_START.md
- [ ] Generate FuelTypeSelector component
- [ ] Update AddVehicle component
- [ ] Generate fuelTypes constant
- [ ] Add form validation

**Testing:**
- [ ] Test rendering for each fuel type
- [ ] Test field visibility toggling
- [ ] Test form validation
- [ ] Test mobile responsiveness
- [ ] Component renders correctly

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #14: Update Vehicle List and Details Pages
**Duration:** 3-4 days | **Effort:** 130 hours | **Blockers:** PR #5

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-14-vehicle-list-details`
- [ ] PR #5 should be merged
- [ ] Understand existing components

**Copilot Work:**
- [ ] Paste PR #14 prompt from COPILOT_QUICK_START.md
- [ ] Generate FuelStatusPanel component
- [ ] Update VehicleList component
- [ ] Update VehicleDetails component
- [ ] Add fuel type filtering

**Testing:**
- [ ] Test list displays for each fuel type
- [ ] Test details pages for each type
- [ ] Test tab visibility based on features
- [ ] Test filtering functionality
- [ ] Test mobile responsiveness

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #15: Create Charging and Fuel Station Discovery
**Duration:** 2-3 days | **Effort:** 115 hours | **Blockers:** PR #14

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-15-station-discovery`
- [ ] PR #14 should be merged
- [ ] Mapbox GL setup verified

**Copilot Work:**
- [ ] Paste PR #15 prompt from COPILOT_QUICK_START.md
- [ ] Generate StationDiscovery component
- [ ] Generate StationMap component
- [ ] Generate StationCard component
- [ ] Generate fuelService

**Testing:**
- [ ] Test EV sees charging stations
- [ ] Test ICE sees fuel stations
- [ ] Test HYBRID can toggle both
- [ ] Test map rendering
- [ ] Test card display

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #16: Create Multi-Fuel Dashboard Overview
**Duration:** 2-3 days | **Effort:** 100 hours | **Blockers:** PR #14

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-16-dashboard`
- [ ] PR #14 should be merged
- [ ] Review Recharts documentation

**Copilot Work:**
- [ ] Paste PR #16 prompt from COPILOT_QUICK_START.md
- [ ] Generate FleetCompositionCard
- [ ] Generate CostBreakdownCard
- [ ] Generate MaintenanceAlertsCard
- [ ] Update DashboardPage

**Testing:**
- [ ] Test fleet composition chart
- [ ] Test cost breakdown display
- [ ] Test maintenance alerts
- [ ] Test mobile responsiveness
- [ ] Test data loading

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

## PHASE 6: BILLING (Week 9)

### PR #17: Implement Multi-Tier Pricing Structure
**Duration:** 2-3 days | **Effort:** 110 hours | **Blockers:** PR #5

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-17-pricing-tiers`
- [ ] PR #5 should be merged
- [ ] Review billing service architecture

**Copilot Work:**
- [ ] Paste PR #17 prompt from COPILOT_QUICK_START.md
- [ ] Generate PricingTier entity
- [ ] Generate PricingService
- [ ] Generate endpoints
- [ ] Generate PricingPage component

**Testing:**
- [ ] Test tier assignment per fuel type
- [ ] Test pricing endpoints
- [ ] Test frontend pricing display
- [ ] Test tier comparison view

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

### PR #18: Create Invoice Generation and Payment Tracking
**Duration:** 2-3 days | **Effort:** 115 hours | **Blockers:** PR #17

**Pre-Work:**
- [ ] Create feature branch: `feature/pr-18-invoicing`
- [ ] PR #17 should be merged
- [ ] Review payment gateway integration

**Copilot Work:**
- [ ] Paste PR #18 prompt from COPILOT_QUICK_START.md
- [ ] Generate InvoiceGenerationService
- [ ] Generate PaymentProcessingService
- [ ] Add invoice endpoints
- [ ] Update BillingPage

**Testing:**
- [ ] Test invoice generation
- [ ] Test bill calculation per tier
- [ ] Test payment processing
- [ ] Test invoice display

**Completion:**
- [ ] All acceptance criteria met
- [ ] Create PR

**Status: ___________**

---

## FINAL PHASE: TESTING & QA (2 Weeks)

### Comprehensive Testing
- [ ] All PRs merged successfully
- [ ] Run full test suite: `mvn clean verify`
- [ ] Code coverage > 85% across codebase
- [ ] SonarQube scan: score > 80
- [ ] No critical security issues
- [ ] No critical bugs logged

### Deployment Testing
- [ ] Deploy to staging environment
- [ ] Test all APIs with Postman/REST client
- [ ] Test all frontend pages
- [ ] Test with real multi-fuel fleet (EV + ICE + Hybrid)
- [ ] Performance testing (load testing)
- [ ] Browser compatibility testing

### Documentation Review
- [ ] API documentation complete
- [ ] Database schema documented
- [ ] Deployment guide written
- [ ] Customer migration guide ready

### Final Checklist
- [ ] Zero critical bugs
- [ ] All acceptance criteria met
- [ ] Team sign-off obtained
- [ ] Stakeholder approval received
- [ ] Release notes written

**Status: ___________** (COMPLETE)

---

## 📊 OVERALL PROGRESS

```
Phase 1: ████░░░░░░ (4/4 PRs)    [40%]
Phase 2: ████░░░░░░ (4/4 PRs)    [40%]
Phase 3: ████░░░░░░ (2/2 PRs)    [100%]
Phase 4: ████░░░░░░ (2/2 PRs)    [100%]
Phase 5: ████░░░░░░ (4/4 PRs)    [40%]
Phase 6: ████░░░░░░ (2/2 PRs)    [40%]
Testing: ████░░░░░░ (0%)

OVERALL: ███░░░░░░░ (0/18 PRs Complete)
```

---

## 🎯 NEXT IMMEDIATE STEPS

1. [ ] Print or bookmark this checklist
2. [ ] Paste `MASTER_COPILOT_CONTEXT.md` into Copilot
3. [ ] Start with **PR #1**
4. [ ] Follow the checklist step-by-step
5. [ ] Track progress in this document
6. [ ] Update `MIGRATION_ROADMAP_VISUAL.md` weekly

---

**ESTIMATED TIMELINE:** 8-10 weeks total
**CURRENT PROGRESS:** Just starting! 🚀

Good luck! You've got this! 💪
