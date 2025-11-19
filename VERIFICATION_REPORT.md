# Fleet & Charging Module Verification Report (Deep Dive)

**Date:** 2025-11-19
**Status:** ⚠️ **Significant Gaps & Technical Risks Identified**

This report provides a detailed technical analysis of the `fleet` and `charging` modules in `backend/evfleet-monolith`.

## 1. Functional Gap Analysis

### Fleet Management (`fleet`)
| Feature | Claimed | Actual Status | Technical Detail |
| :--- | :--- | :--- | :--- |
| **Multi-fuel support** | ✅ | ✅ **Verified** | `Vehicle` entity correctly models EV (`batteryCapacity`) vs ICE (`fuelTankCapacity`) fields. |
| **Vehicle Registration** | ✅ | ✅ **Verified** | `VehicleController` & `VehicleService` handle CRUD operations correctly. |
| **Trip Management** | ✅ | ✅ **Verified** | `Trip` entity tracks start/end locations, distance, and duration. |
| **Real-time Tracking** | ✅ | ⚠️ **Basic** | `updateLocation` endpoint exists but **overwrites** the previous location. **No history/audit trail** of location changes exists. You cannot replay a trip. |
| **Fuel Consumption** | ✅ | ⚠️ **Partial** | `Trip` entity has `fuelConsumed` field, but there is no dedicated logic to calculate this based on vehicle efficiency. It relies on the client sending the value. |
| **Document Management** | ✅ | ❌ **MISSING** | No `Document` entity, repository, or service. No file storage integration (S3/MinIO). |
| **Route Planning** | ✅ | ❌ **MISSING** | No `RoutePlan` entity. No integration with Google Maps/OSRM for optimization. |
| **Geofencing** | ✅ | ❌ **MISSING** | No `Geofence` entity. No point-in-polygon logic to detect geofence entry/exit. |
| **Customer Management** | ✅ | ❌ **MISSING** | No `Customer` entity. The system assumes internal fleet use only. |

### Charging Management (`charging`)
| Feature | Claimed | Actual Status | Technical Detail |
| :--- | :--- | :--- | :--- |
| **Station Management** | ✅ | ✅ **Verified** | `ChargingStation` entity tracks slots, location, and pricing. |
| **Session Tracking** | ✅ | ✅ **Verified** | `ChargingSession` tracks energy, cost, and status. |
| **Cost Calculation** | ✅ | ✅ **Verified** | Simple logic: `energy * price`. No support for time-based pricing or tiered rates. |
| **Route Optimization** | ⚠️ | ❌ **MISSING** | No `RouteOptimization` entity or algorithm found in `charging` package. |
| **Concurrency Handling** | N/A | ❌ **RISK** | **Race Condition Detected:** `reserveSlot()` checks `availableSlots > 0` then decrements. Without `@Version` (Optimistic Locking) or `PESSIMISTIC_WRITE` lock, two concurrent requests could reserve the last slot, leading to negative availability. |

---

## 2. Code Quality & Technical Debt

### 🔴 Critical Issues
1.  **Race Conditions in Charging:**
    *   **Location:** `ChargingSessionService.startSession` -> `station.reserveSlot()`
    *   **Issue:** The check-then-act pattern is not atomic.
    *   **Impact:** Overbooking of charging stations under high load.
    *   **Fix Required:** Add `@Version` to `ChargingStation` for optimistic locking or use a database-level lock.

2.  **Missing Input Validation:**
    *   **Location:** `TripController`, `ChargingSessionController`
    *   **Issue:** No validation for negative values (e.g., negative distance, negative energy consumed).
    *   **Impact:** Data integrity corruption (e.g., a trip with -50km distance).

3.  **No Location History:**
    *   **Location:** `VehicleService.updateVehicleLocation`
    *   **Issue:** It performs a destructive update (`setLatitude`, `setLongitude`).
    *   **Impact:** Impossible to generate trip history lines or analyze driver routes later.

### 🟡 Improvements Needed
1.  **Hardcoded Currencies/Units:**
    *   Cost is assumed to be in a single currency. No multi-currency support.
    *   Distance is assumed to be km.

2.  **No Pagination for "Get All":**
    *   `getAllVehicles` and `getAllSessions` return lists. This will crash the server once the database grows to thousands of records.

## 3. Missing Entities List
The following Java classes/Database tables are completely absent:
*   `Document.java` / `documents` table
*   `Geofence.java` / `geofences` table
*   `RoutePlan.java` / `route_plans` table
*   `Customer.java` / `customers` table
*   `MaintenanceRecord.java` (Implied by "Fleet Management" but not found)

## 4. Recommendation
1.  **Immediate Fix:** Implement Optimistic Locking (`@Version`) on `ChargingStation`.
2.  **Feature Build:** You must build the **Document** and **Geofencing** modules from scratch as they are non-existent.
3.  **Refactor:** Change `VehicleLocation` to be a separate time-series table (e.g., `VehicleLocationHistory`) to enable actual tracking.
