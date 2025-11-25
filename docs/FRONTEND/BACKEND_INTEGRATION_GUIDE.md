# Backend Integration Guide

## Current Status

The frontend has been developed with mock data fallbacks to handle backend API failures gracefully. This document outlines the integration points and what needs to be fixed.

## API Integration Issues

### 1. Vehicle Management
**Endpoint:** `GET http://localhost:8080/api/v1/vehicles`
**Status:** ✅ **FIXED** - Mock data fallback added
**Implementation:**
- vehicleSlice.ts provides 3 mock vehicles with complete battery data
- FleetManagementPage.tsx has null/undefined safety checks
- Fallback activated when API returns 500 or connection fails

### 2. Charging Stations & Sessions
**Endpoints:**
- `GET /api/v1/charging/stations` (500 Internal Server Error)
- `GET /api/v1/charging/sessions` (405 Method Not Allowed)

**Status:** ✅ **FIXED** - Mock data fallback added
**Implementation:**
- chargingSlice.ts provides mock stations and sessions
- Two test stations with different charging types
- Two test sessions (one completed, one active)
- Fallback to mock data when API fails

### 3. Notifications & Alerts
**Endpoint:** `GET /api/v1/notifications/alerts` (404 Not Found)
**Status:** ⚠️ **NEEDS FIX** - Mock data fallback needed
**File:** `src/redux/slices/notificationSlice.ts`
**Action Required:** Add MOCK_ALERTS and MOCK_NOTIFICATIONS arrays

### 4. Drivers
**Endpoint:** `GET /api/v1/drivers` (404 Not Found)
**Status:** ⚠️ **NEEDS FIX** - Mock data fallback needed
**File:** `src/redux/slices/driverSlice.ts`
**Action Required:** Add MOCK_DRIVERS array

### 5. Maintenance Records
**Endpoint:** `GET /api/v1/maintenance/records` (404 Not Found)
**Status:** ⚠️ **NEEDS FIX** - Mock data fallback needed
**File:** `src/redux/slices/maintenanceSlice.ts`
**Action Required:** Add MOCK_MAINTENANCE_RECORDS array

### 6. Analytics
**Endpoint:** `GET /api/v1/analytics/fleet` (500 Internal Server Error)
**Status:** ⚠️ **NEEDS FIX** - Mock data fallback needed
**File:** `src/redux/slices/analyticsSlice.ts`
**Action Required:** Add MOCK_ANALYTICS data

### 7. WebSocket Connection
**Endpoint:** `ws://localhost:8080/socket.io/`
**Status:** ❌ **NOT AVAILABLE** - Backend not running
**Impact:** Real-time features disabled (non-critical for MVP)
**File:** `src/services/websocket.ts`
**Note:** Real-time updates will work once backend WebSocket server is running

## Quick Integration Steps

### For Each Failing API:

1. **Add Mock Data** in Redux slice:
```typescript
const MOCK_DATA: Type[] = [
  { /* sample data */ },
];
```

2. **Update Rejection Handler**:
```typescript
.addCase(fetchData.rejected, (state, action) => {
  state.loading = false;
  state.error = action.error.message || 'Failed to fetch data';
  state.data = MOCK_DATA; // ← Add this line
});
```

3. **Add Safety Checks** in Components:
```typescript
const safeValue = data?.property ?? defaultValue;
```

## Type Safety

All API response types are defined in `/src/types/`:
- `vehicle.ts` - Vehicle, BatteryInfo, Location types
- `charging.ts` - ChargingStation, ChargingSession types
- `driver.ts` - Driver types
- `maintenance.ts` - Maintenance types
- `notification.ts` - Notification, Alert types
- `analytics.ts` - Analytics types
- `billing.ts` - Invoice, Payment types

## API Endpoint Reference

### Vehicle Service
```
GET    /api/v1/vehicles
GET    /api/v1/vehicles/{id}
POST   /api/v1/vehicles
PUT    /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}
GET    /api/v1/vehicles/{id}/trips
GET    /api/v1/vehicles/{id}/location
GET    /api/v1/vehicles/filtered?status=ACTIVE&type=TWO_WHEELER
PATCH  /api/v1/vehicles/{id}/battery
PATCH  /api/v1/vehicles/{id}/assignment
```

### Trip Service
```
GET    /api/v1/trips
POST   /api/v1/trips/start
POST   /api/v1/trips/{id}/end
POST   /api/v1/trips/{id}/pause
POST   /api/v1/trips/{id}/resume
POST   /api/v1/trips/{id}/cancel
GET    /api/v1/trips/analytics
```

### Charging Service
```
GET    /api/v1/charging/stations
GET    /api/v1/charging/stations/{id}
GET    /api/v1/charging/stations/available
GET    /api/v1/charging/stations/nearest?lat=28.6139&lng=77.2090&limit=5
GET    /api/v1/charging/sessions
GET    /api/v1/charging/sessions/{id}
POST   /api/v1/charging/sessions/start
POST   /api/v1/charging/sessions/{id}/end
POST   /api/v1/charging/sessions/{id}/cancel
POST   /api/v1/charging/route/optimize
```

### Driver Service
```
GET    /api/v1/drivers
GET    /api/v1/drivers/{id}
POST   /api/v1/drivers
PUT    /api/v1/drivers/{id}
DELETE /api/v1/drivers/{id}
GET    /api/v1/drivers/performance
GET    /api/v1/drivers/leaderboard
PATCH  /api/v1/drivers/{id}/status
```

### Maintenance Service
```
GET    /api/v1/maintenance/schedules
GET    /api/v1/maintenance/records
POST   /api/v1/maintenance/schedules
PUT    /api/v1/maintenance/schedules/{id}
DELETE /api/v1/maintenance/schedules/{id}
POST   /api/v1/maintenance/records
GET    /api/v1/maintenance/battery-health
```

### Analytics Service
```
GET    /api/v1/analytics/fleet
GET    /api/v1/analytics/utilization
GET    /api/v1/analytics/cost
GET    /api/v1/analytics/energy
GET    /api/v1/analytics/carbon
GET    /api/v1/analytics/tco/{vehicleId}
GET    /api/v1/analytics/export?type=fleet&format=pdf
```

### Notification Service
```
GET    /api/v1/notifications
GET    /api/v1/notifications/alerts
POST   /api/v1/notifications/mark-read/{id}
GET    /api/v1/notifications/unread-count
PUT    /api/v1/notifications/preferences
```

### Billing Service
```
GET    /api/v1/billing/invoices
GET    /api/v1/billing/invoices/{id}
GET    /api/v1/billing/invoices/{id}/download
GET    /api/v1/billing/payments
POST   /api/v1/billing/payments/process
GET    /api/v1/billing/address
PUT    /api/v1/billing/address
GET    /api/v1/billing/payment-methods
POST   /api/v1/billing/payment-methods
```

## Common Response Structure

### Success Response (200)
```json
{
  "data": { /* entity or array of entities */ },
  "message": "Success"
}
```

### Error Response (4xx, 5xx)
```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "status": 400
}
```

## Development Recommendations

1. **Use Mock Data:** For features not yet implemented on backend
2. **Test Error States:** All pages handle null/undefined gracefully
3. **Loading States:** All async operations show loading indicators
4. **Error Messages:** User-friendly error messages in alerts
5. **Retry Logic:** Consider implementing retry mechanism for failed requests

## Testing Checklist

- [ ] All pages render without console errors
- [ ] Mock data displays correctly when API fails
- [ ] Null/undefined safety checks prevent crashes
- [ ] Loading states show during API calls
- [ ] Error messages display when API fails
- [ ] Navigation works between pages
- [ ] Forms can submit data (even if backend not available)
- [ ] Responsive design works on mobile/tablet/desktop
- [ ] Real-time features work when backend available
- [ ] Performance is acceptable with mock data

## Backend Server Setup

To connect to real backend:

1. Start backend server on `http://localhost:8080`
2. Ensure CORS is enabled for `http://localhost:3000`
3. WebSocket should be available at `ws://localhost:8080/socket.io/`
4. Check network tab in browser DevTools for API response formats
5. Verify response data matches type definitions in `/src/types/`

## Environment Configuration

File: `src/services/api.ts`

```typescript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';
```

Set environment variable:
```bash
REACT_APP_API_BASE_URL=http://your-backend-url/api/v1
```

---

**Last Updated:** November 2, 2025
**Status:** Development with Mock Data Fallbacks
