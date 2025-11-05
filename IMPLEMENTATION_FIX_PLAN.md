# Implementation Fix Plan for API Mismatches

**Date:** November 5, 2025  
**Status:** In Progress  
**Priority:** Critical

---

## Fix Strategy

Given the scope of 74 API issues, this plan prioritizes high-impact, low-effort fixes first.

---

## Phase 1: Path & Method Fixes (Quick Wins)

### 1.1 Fix Base Path Mismatches - HIGHEST PRIORITY ⚡

**Effort:** Low (5 minutes each)  
**Impact:** HIGH (Unlocks 20+ endpoints)

#### Trip Service
- **Issue:** Frontend expects `/api/v1/fleet/trips`, Backend has `/api/fleet/trips`
- **Fix:** Update `@RequestMapping` in TripController
- **Files:** `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/TripController.java`

#### Geofence Service
- **Issue:** Frontend expects `/api/v1/fleet/geofences`, Backend has `/api/fleet/geofences`
- **Fix:** Update `@RequestMapping` in GeofenceController
- **Files:** `backend/fleet-service/src/main/java/com/evfleet/fleet/controller/GeofenceController.java`

**Status:** ✅ TO FIX IN THIS PR

---

### 1.2 Fix HTTP Method Mismatches

**Effort:** Low (2 minutes each)  
**Impact:** Medium (Fixes specific operations)

#### Charging Service - End Session
- **Issue:** Frontend POST, Backend PATCH
- **Fix:** Change `@PatchMapping` to `@PostMapping` OR add both
- **File:** `ChargingSessionController.java`

#### Trip Service - End/Pause/Resume
- **Issue:** Frontend POST, Backend PUT
- **Fix:** Change `@PutMapping` to `@PostMapping` for these operations
- **File:** `TripController.java`

**Status:** ✅ TO FIX IN THIS PR

---

## Phase 2: Add Missing Simple Endpoints

### 2.1 Driver Service Enhancements

**Effort:** Medium (30 minutes each)

#### Add Missing Endpoints:

```java
// GET /api/v1/drivers/status/{status}
@GetMapping("/status/{status}")
public ResponseEntity<List<DriverResponse>> getDriversByStatus(@PathVariable String status)

// GET /api/v1/drivers/leaderboard
@GetMapping("/leaderboard")
public ResponseEntity<List<DriverLeaderboard>> getLeaderboard(@RequestParam(required = false) Integer limit)

// GET /api/v1/drivers/{driverId}/performance-score
@GetMapping("/{driverId}/performance-score")
public ResponseEntity<PerformanceScoreResponse> getPerformanceScore(@PathVariable String driverId)

// PUT /api/v1/drivers/assignments/{assignmentId}
@PutMapping("/assignments/{assignmentId}")
public ResponseEntity<DriverAssignmentResponse> updateAssignment(@PathVariable String assignmentId, @RequestBody DriverAssignmentRequest request)

// Fix: Change /assign to /assign-vehicle
@PostMapping("/{driverId}/assign-vehicle") // was /assign
```

**Status:** ✅ TO FIX IN THIS PR

---

### 2.2 Vehicle Service Enhancements

**Effort:** Low-Medium (20 minutes each)

#### Add Missing Endpoints:

```java
// GET /api/v1/vehicles/status/{status}
@GetMapping("/status/{status}")
public ResponseEntity<List<VehicleResponse>> getVehiclesByStatus(@PathVariable String status)

// GET /api/v1/vehicles/low-battery
@GetMapping("/low-battery")
public ResponseEntity<List<VehicleResponse>> getLowBatteryVehicles(@RequestParam(defaultValue = "20") Double threshold)
```

#### Fix Parameter Mismatches:

```java
// PATCH /api/v1/vehicles/{id}/location - Accept body instead of query params
@PatchMapping("/{id}/location")
public ResponseEntity<Void> updateVehicleLocation(@PathVariable Long id, @RequestBody LocationUpdate request)

// PATCH /api/v1/vehicles/{id}/battery - Accept body instead of query param
@PatchMapping("/{id}/battery")
public ResponseEntity<Void> updateBatterySoc(@PathVariable Long id, @RequestBody BatteryUpdate request)
```

**Status:** ✅ TO FIX IN THIS PR

---

### 2.3 Charging Service Enhancements

**Effort:** Medium (20 minutes each)

#### Add Missing Endpoints:

```java
// GET /api/v1/charging/sessions - Get all sessions
@GetMapping
public ResponseEntity<List<ChargingSessionResponse>> getAllSessions(@RequestParam(required = false) Map<String, String> params)

// POST /api/v1/charging/sessions/start - Alias for POST /sessions
@PostMapping("/start")
public ResponseEntity<ChargingSessionResponse> startSessionAlias(@Valid @RequestBody ChargingSessionRequest request)

// POST /api/v1/charging/sessions/{id}/cancel - Add cancel endpoint
@PostMapping("/{id}/cancel")
public ResponseEntity<ChargingSessionResponse> cancelSessionPost(@PathVariable Long id, @RequestBody(required = false) CancelRequest request)

// POST /api/v1/charging/stations/{stationId}/reserve
@PostMapping("/stations/{stationId}/reserve")
public ResponseEntity<ReservationResponse> reserveSlot(@PathVariable Long stationId, @RequestBody ReservationRequest request)

// POST /api/v1/charging/stations/{stationId}/release
@PostMapping("/stations/{stationId}/release")
public ResponseEntity<Void> releaseSlot(@PathVariable Long stationId, @RequestBody ReleaseRequest request)
```

**Status:** ⚠️ PARTIAL - Add critical endpoints

---

## Phase 3: Notification & Analytics Services

### 3.1 Notification Service Enhancements

**Effort:** Medium (1 hour)

#### Add Missing Endpoints:

```java
// GET /api/v1/notifications - Get all for current user
@GetMapping
public ResponseEntity<List<NotificationResponse>> getAllNotifications()

// GET /api/v1/notifications/{id}
@GetMapping("/{id}")
public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id)

// GET /api/v1/notifications/unread-count
@GetMapping("/unread-count")
public ResponseEntity<UnreadCountResponse> getUnreadCount()

// GET /api/v1/notifications/alerts/{priority}
@GetMapping("/alerts/{priority}")
public ResponseEntity<List<NotificationResponse>> getAlertsByPriority(@PathVariable String priority)

// POST /api/v1/notifications/read-all
@PostMapping("/read-all")
public ResponseEntity<Void> markAllAsRead()

// DELETE /api/v1/notifications/{id}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteNotification(@PathVariable String id)

// DELETE /api/v1/notifications/all
@DeleteMapping("/all")
public ResponseEntity<Void> deleteAllNotifications()

// Preferences
@GetMapping("/preferences")
@PutMapping("/preferences")
```

**Status:** ✅ TO FIX IN THIS PR (high value endpoints)

---

### 3.2 Analytics Service Enhancements

**Effort:** Medium-High (2 hours - needs service layer logic)

#### Quick Fixes (Path Changes):

```java
// Fix: /tco to /tco-analysis
@GetMapping("/tco-analysis/{vehicleId}")

// Fix: /cost to /cost-analytics
@GetMapping("/cost-analytics/{companyId}")

// Add: /fleet without companyId (aggregate all)
@GetMapping("/fleet")
public ResponseEntity<FleetSummaryResponse> getFleetSummaryAll()
```

#### New Endpoints (Requires Business Logic):

```java
// These need service implementation
@GetMapping("/utilization-reports")
@GetMapping("/energy-consumption")
@GetMapping("/carbon-footprint")
@GetMapping("/battery")
@PostMapping("/export")
```

**Status:** ⚠️ PARTIAL - Fix paths, defer complex endpoints

---

## Phase 4: Maintenance Service (Major Refactor)

**Effort:** High (3-4 hours)  
**Impact:** High

This service needs significant work. Current controller only has 4 endpoints, needs 16.

### Strategy:
1. Add CRUD for maintenance records
2. Add CRUD for schedules (separate from records)
3. Add battery health endpoints
4. Add service history and reminders

**Status:** ⚠️ OUT OF SCOPE - Too complex for this PR, needs separate effort

**Workaround:** Document as known limitation, frontend will use mock data

---

## Phase 5: Billing Service (Complete Implementation)

**Effort:** Very High (8-10 hours)  
**Impact:** Critical but complex

### Challenge:
- No controller exists
- Only domain events present
- Needs full REST layer + service layer
- 17 endpoints required

### Strategy:
Create minimal BillingController with stub responses that return appropriate empty data structures. This allows frontend to not crash while proper implementation is completed later.

**Status:** ❌ OUT OF SCOPE - Too large for this PR

**Workaround:** Create stub controller with 501 Not Implemented responses OR document to use mock data

---

## What Will Be Fixed In This PR

### ✅ Phase 1: Path & Method Fixes (100%)
- Fix Trip and Geofence base paths
- Fix HTTP method mismatches in Charging and Trip services

### ✅ Phase 2: Simple Endpoint Additions (80%)
- Add missing Driver endpoints (leaderboard, performance, status filter)
- Add missing Vehicle endpoints (status filter, low-battery)
- Fix Vehicle parameter mismatches
- Add key Charging endpoints (reserve, release, cancel)

### ✅ Phase 3: Notification Service (70%)
- Add most missing notification endpoints
- Skip preferences for now

### ⚠️ Phase 3: Analytics Service (30%)
- Fix path mismatches only
- Complex endpoints deferred

### ❌ Phase 4: Maintenance Service (0%)
- Too complex, needs separate PR
- Frontend will continue using mock data

### ❌ Phase 5: Billing Service (0%)
- Too complex, needs separate PR
- Frontend will continue using mock data

---

## Estimated Impact

### Before Fixes:
- **Working Endpoints:** 41/115 (36%)
- **Broken Features:** Most backend integration features

### After This PR:
- **Working Endpoints:** ~75/115 (65%)
- **Improvement:** +30%
- **Fully Working Services:** Driver, Vehicle, Charging, Notification, Trip, Geofence
- **Partially Working:** Analytics
- **Still Broken:** Maintenance, Billing

### Remaining Work:
- Maintenance Service: Full CRUD implementation (~4 hours)
- Billing Service: Complete REST API (~10 hours)
- Analytics Service: Complex report endpoints (~3 hours)
- Total: ~17 hours additional work

---

## Testing Strategy

After each fix:
1. Build the affected service
2. Test the endpoint with curl/Postman
3. Verify frontend can call it
4. Check response format matches TypeScript types

---

## Next PR Recommendations

### PR #2: Maintenance Service CRUD
- Implement all maintenance record operations
- Implement schedule management
- Add battery health tracking
- Add service history and reminders
- **Estimate:** 4-6 hours

### PR #3: Billing Service REST API
- Create complete BillingController
- Implement subscription management
- Implement invoice operations
- Implement payment processing
- **Estimate:** 8-12 hours

### PR #4: Analytics Service Advanced Features
- Implement energy consumption tracking
- Implement carbon footprint calculation
- Implement battery analytics aggregation
- Implement report export
- **Estimate:** 4-6 hours

---

## Success Criteria for This PR

- [ ] All base path mismatches fixed
- [ ] All HTTP method mismatches fixed
- [ ] Driver service 90%+ functional
- [ ] Vehicle service 90%+ functional
- [ ] Charging service 80%+ functional
- [ ] Notification service 70%+ functional
- [ ] Trip service 90%+ functional
- [ ] Geofence service 90%+ functional
- [ ] Build and compilation successful
- [ ] At least 30% improvement in working endpoints

---

**Plan Created:** November 5, 2025  
**Execution Start:** Immediately  
**Estimated Duration:** 3-4 hours
