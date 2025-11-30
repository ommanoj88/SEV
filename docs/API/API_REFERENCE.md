# SEV Fleet Management - API Reference

**Version:** 2.0.0  
**Last Updated:** November 2025  
**Base URL:** `https://api.sevfleet.com/api/v1`  
**Authentication:** Bearer Token (Firebase Auth)

---

## Table of Contents
1. [Authentication](#authentication)
2. [Vehicles API](#vehicles-api)
3. [Drivers API](#drivers-api)
4. [Charging API](#charging-api)
5. [Maintenance API](#maintenance-api)
6. [Analytics API](#analytics-api)
7. [Billing API](#billing-api)
8. [Telematics API](#telematics-api)
9. [Error Codes](#error-codes)
10. [Rate Limiting](#rate-limiting)

---

## Authentication

All API endpoints require authentication using Firebase JWT tokens.

### Headers Required
```
Authorization: Bearer <firebase_id_token>
X-Company-ID: <company_id>
Content-Type: application/json
```

### Token Refresh
Tokens expire after 1 hour. Use Firebase SDK to refresh automatically.

---

## Vehicles API

### Create Vehicle
`POST /vehicles`

Creates a new vehicle in the fleet.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| companyId | Long | Yes | Company ID |

**Request Body:**
```json
{
  "vehicleNumber": "EV-2024-001",
  "make": "Tesla",
  "model": "Model 3",
  "year": 2024,
  "fuelType": "EV",
  "type": "LCV",
  "batteryCapacity": 82.0,
  "currentBatterySoc": 100.0,
  "defaultChargerType": "CCS2",
  "licensePlate": "MH-01-AB-1234",
  "vin": "5YJ3E1EA1NF123456"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Vehicle created successfully",
  "data": {
    "id": 1,
    "vehicleNumber": "EV-2024-001",
    "make": "Tesla",
    "model": "Model 3",
    "year": 2024,
    "fuelType": "EV",
    "type": "LCV",
    "batteryCapacity": 82.0,
    "currentBatterySoc": 100.0,
    "defaultChargerType": "CCS2",
    "status": "AVAILABLE",
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Get All Vehicles
`GET /vehicles`

Retrieves all vehicles for a company.

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| companyId | Long | Yes | - | Company ID |
| status | String | No | - | Filter by status (AVAILABLE, IN_TRIP, CHARGING, MAINTENANCE) |
| fuelType | String | No | - | Filter by fuel type (EV, ICE, HYBRID) |
| page | Int | No | 0 | Page number |
| size | Int | No | 20 | Page size |

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "vehicleNumber": "EV-2024-001",
      "make": "Tesla",
      "model": "Model 3",
      "fuelType": "EV",
      "status": "AVAILABLE",
      "currentBatterySoc": 85.0
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

### Get Vehicle by ID
`GET /vehicles/{id}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "vehicleNumber": "EV-2024-001",
    "make": "Tesla",
    "model": "Model 3",
    "year": 2024,
    "fuelType": "EV",
    "batteryCapacity": 82.0,
    "currentBatterySoc": 85.0,
    "status": "AVAILABLE",
    "currentDriverId": null,
    "lastLocation": {
      "latitude": 19.0760,
      "longitude": 72.8777
    },
    "lastUpdated": "2024-01-15T10:25:00Z"
  }
}
```

### Update Vehicle
`PUT /vehicles/{id}`

**Request Body:**
```json
{
  "model": "Model Y",
  "year": 2024,
  "status": "MAINTENANCE"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Vehicle updated successfully",
  "data": { ... }
}
```

### Delete Vehicle
`DELETE /vehicles/{id}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Vehicle deleted successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cannot delete vehicle while it is in an active trip",
  "error": "VEHICLE_IN_USE"
}
```

### Update Battery SOC
`PATCH /vehicles/{id}/battery-soc`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| soc | Double | Yes | Battery state of charge (0-100) |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "currentBatterySoc": 92.5
  }
}
```

---

## Drivers API

### Create Driver
`POST /drivers`

**Request Body:**
```json
{
  "name": "Rajesh Kumar",
  "phone": "+919876543210",
  "email": "rajesh.kumar@company.com",
  "licenseNumber": "MH-DL-12345678",
  "licenseExpiry": "2026-12-31",
  "status": "ACTIVE"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Rajesh Kumar",
    "phone": "+919876543210",
    "email": "rajesh.kumar@company.com",
    "licenseNumber": "MH-DL-12345678",
    "licenseExpiry": "2026-12-31",
    "status": "ACTIVE",
    "totalTrips": 0,
    "totalDistance": 0.0,
    "safetyScore": 100.0
  }
}
```

### Get Driver by ID
`GET /drivers/{id}`

### Get Active Drivers
`GET /drivers/active?companyId={companyId}`

### Get Available Drivers
`GET /drivers/available?companyId={companyId}`

Returns drivers who are active and not currently assigned to a vehicle.

### Assign Vehicle to Driver
`POST /drivers/{driverId}/assign-vehicle/{vehicleId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Vehicle assigned to driver successfully",
  "data": {
    "driverId": 1,
    "vehicleId": 10,
    "assignedAt": "2024-01-15T10:30:00Z"
  }
}
```

### Unassign Vehicle
`POST /drivers/{driverId}/unassign-vehicle`

### Get Driver Safety Score
`GET /drivers/{driverId}/safety-score`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "driverId": 1,
    "driverName": "Rajesh Kumar",
    "safetyScore": 87.5,
    "breakdown": {
      "harshBrakingEvents": 3,
      "speedingEvents": 1,
      "rapidAccelerationEvents": 2,
      "idlingMinutes": 45
    },
    "calculatedAt": "2024-01-15T10:30:00Z"
  }
}
```

---

## Charging API

### Start Charging Session
`POST /charging/sessions/start`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | Long | Yes | Vehicle ID |
| stationId | Long | Yes | Charging station ID |
| companyId | Long | Yes | Company ID |
| initialSoc | Double | No | Initial battery SOC |

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "vehicleId": 10,
    "stationId": 5,
    "startTime": "2024-01-15T10:30:00Z",
    "initialSoc": 25.0,
    "status": "ACTIVE"
  }
}
```

**Error Response (400 Bad Request) - 2-Wheeler Restriction:**
```json
{
  "success": false,
  "message": "Charging management requires battery tracking and is available for 4-wheeler EVs only. For 2-wheelers and 3-wheelers, we track GPS location only.",
  "error": "VEHICLE_TYPE_NOT_SUPPORTED"
}
```

### Complete Charging Session
`POST /charging/sessions/{id}/complete`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| energyConsumed | BigDecimal | Yes | Energy in kWh |
| finalSoc | Double | No | Final battery SOC |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "vehicleId": 10,
    "stationId": 5,
    "startTime": "2024-01-15T10:30:00Z",
    "endTime": "2024-01-15T11:45:00Z",
    "duration": 75,
    "initialSoc": 25.0,
    "finalSoc": 95.0,
    "energyConsumed": 52.5,
    "cost": 18.38,
    "status": "COMPLETED"
  }
}
```

### Get Charging Sessions by Vehicle
`GET /charging/sessions/vehicle/{vehicleId}`

### Get Charging Stations
`GET /charging/stations?companyId={companyId}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "name": "Main Office Charger",
      "address": "123 Business Park, Mumbai",
      "latitude": 19.0760,
      "longitude": 72.8777,
      "chargerType": "CCS2",
      "powerOutput": 150.0,
      "pricePerKwh": 0.35,
      "totalSlots": 4,
      "availableSlots": 2,
      "status": "ACTIVE"
    }
  ]
}
```

---

## Maintenance API

### Create Maintenance Record
`POST /maintenance/records`

**Request Body:**
```json
{
  "vehicleId": 10,
  "type": "TIRE_ROTATION",
  "scheduledDate": "2024-02-01",
  "description": "Quarterly tire rotation",
  "estimatedCost": 150.00,
  "serviceProvider": "Quick Service Center"
}
```

**Maintenance Types:**
| Type | Applicable To |
|------|---------------|
| TIRE_ROTATION | All vehicles |
| BRAKE_INSPECTION | All vehicles |
| OIL_CHANGE | ICE, HYBRID only |
| BATTERY_CHECK | EV, HYBRID only |
| BATTERY_REPLACEMENT | EV, HYBRID only |
| MOTOR_SERVICE | EV only |
| ENGINE_TUNE_UP | ICE, HYBRID only |
| COOLANT_FLUSH | All vehicles |

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "vehicleId": 10,
    "type": "TIRE_ROTATION",
    "scheduledDate": "2024-02-01",
    "status": "SCHEDULED",
    "estimatedCost": 150.00
  }
}
```

### Get Upcoming Maintenance
`GET /maintenance/records/upcoming?companyId={companyId}`

### Complete Maintenance
`POST /maintenance/records/{id}/complete`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| actualCost | BigDecimal | Yes | Actual cost incurred |
| notes | String | No | Completion notes |

---

## Analytics API

### Get Fleet Summary
`GET /analytics/fleet-summary?companyId={companyId}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalVehicles": 45,
    "activeVehicles": 38,
    "inMaintenance": 3,
    "charging": 4,
    "byFuelType": {
      "EV": 30,
      "ICE": 10,
      "HYBRID": 5
    },
    "averageBatterySoc": 72.5,
    "fleetUtilization": 84.4
  }
}
```

### Get Energy Analytics
`GET /analytics/energy?companyId={companyId}&startDate={date}&endDate={date}`

### Get Driver Leaderboard
`GET /analytics/driver-leaderboard?companyId={companyId}&limit={10}`

### Get TCO Analysis
`GET /analytics/tco?companyId={companyId}&vehicleId={vehicleId}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "vehicleId": 10,
    "acquisitionCost": 3500000.00,
    "energyCostPerKm": 1.25,
    "maintenanceCostPerKm": 0.45,
    "totalCostOfOwnership": 4250000.00,
    "projectedSavings": {
      "vsICE": 1200000.00,
      "percentageSavings": 22.0
    }
  }
}
```

---

## Billing API

### Create Invoice
`POST /billing/invoices`

### Get Invoices
`GET /billing/invoices?companyId={companyId}`

### Process Payment (Razorpay)
`POST /billing/payments/razorpay/create-order`

**Request Body:**
```json
{
  "invoiceId": 1,
  "amount": 15000.00,
  "currency": "INR"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "orderId": "order_Nxxxxxxxxx",
    "amount": 1500000,
    "currency": "INR",
    "key": "rzp_test_xxxxxxxx",
    "checkoutData": {
      "name": "SEV Fleet Management",
      "description": "Invoice #INV-2024-001",
      "prefill": {
        "email": "billing@company.com",
        "contact": "+919876543210"
      }
    }
  }
}
```

### Verify Payment
`POST /billing/payments/razorpay/verify`

---

## Telematics API

### Get Latest Telemetry Snapshot
`GET /telematics/snapshot/{vehicleId}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "vehicleId": 10,
    "timestamp": "2024-01-15T10:30:00Z",
    "location": {
      "latitude": 19.0760,
      "longitude": 72.8777,
      "altitude": 14.0,
      "heading": 90.0,
      "speed": 45.5
    },
    "battery": {
      "soc": 72.0,
      "voltage": 395.2,
      "temperature": 32.5
    },
    "odometer": 12543.7,
    "engineStatus": "ON"
  }
}
```

### Get Telemetry Alerts
`GET /telematics/alerts?companyId={companyId}`

### Trigger Manual Sync
`POST /telematics/sync?companyId={companyId}`

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| RESOURCE_NOT_FOUND | 404 | Requested resource does not exist |
| VALIDATION_ERROR | 400 | Request validation failed |
| DUPLICATE_RESOURCE | 409 | Resource already exists |
| VEHICLE_IN_USE | 400 | Cannot perform operation on vehicle in use |
| DRIVER_UNAVAILABLE | 400 | Driver is not available for assignment |
| STATION_FULL | 400 | No available charging slots |
| PAYMENT_FAILED | 402 | Payment processing failed |
| UNAUTHORIZED | 401 | Authentication required |
| FORBIDDEN | 403 | Insufficient permissions |
| RATE_LIMITED | 429 | Too many requests |

---

## Rate Limiting

API requests are rate limited per company:

| Endpoint Category | Limit | Window |
|-------------------|-------|--------|
| Read operations | 1000 | 1 minute |
| Write operations | 100 | 1 minute |
| Analytics | 50 | 1 minute |
| Telematics sync | 10 | 1 minute |

**Rate Limit Headers:**
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1705312260
```

---

## API Versioning

The API uses URL path versioning. Current version: `v1`

Future versions will be available at `/api/v2/`, etc.

Deprecated endpoints will return a warning header:
```
X-API-Deprecated: true
X-API-Sunset-Date: 2025-01-01
```

---

*Last Updated: January 2024*
*PR #45: API Documentation*
