# Database Schema Documentation

## Overview

This document describes the database schema for the Fleet Management Platform, including the new event tracking and state management tables.

---

## Vehicle Events Table

**Table Name**: `vehicle_events`

**Purpose**: Tracks all production events for vehicles (historical record). Implements comprehensive event sourcing for vehicle operations.

### Columns

| Column Name | Data Type | Nullable | Description |
|------------|-----------|----------|-------------|
| id | BIGSERIAL | NO | Primary key |
| vehicle_id | BIGINT | NO | Foreign key to vehicles table |
| event_type | VARCHAR(50) | NO | Type of event (see Event Types below) |
| event_subtype | VARCHAR(50) | YES | Subtype for additional classification |
| event_timestamp | TIMESTAMP | NO | When the event occurred |
| severity | VARCHAR(20) | YES | Event severity: INFO, LOW, MEDIUM, HIGH, CRITICAL |
| event_data | JSONB | YES | JSON object with event-specific data |
| latitude | DOUBLE PRECISION | YES | Location latitude |
| longitude | DOUBLE PRECISION | YES | Location longitude |
| location_name | VARCHAR(255) | YES | Human-readable location name |
| driver_id | BIGINT | YES | Associated driver ID |
| trip_id | BIGINT | YES | Associated trip ID |
| maintenance_id | BIGINT | YES | Associated maintenance record ID |
| charging_session_id | BIGINT | YES | Associated charging session ID |
| battery_soc | DOUBLE PRECISION | YES | Battery state of charge at event time |
| fuel_level | DOUBLE PRECISION | YES | Fuel level at event time |
| odometer | DOUBLE PRECISION | YES | Odometer reading |
| speed | DOUBLE PRECISION | YES | Vehicle speed |
| source | VARCHAR(50) | YES | Event source: TELEMETRY, MANUAL, SYSTEM, EXTERNAL |
| user_id | BIGINT | YES | User who triggered the event (if manual) |
| company_id | BIGINT | NO | Company that owns the vehicle |
| description | TEXT | YES | Event description |
| notes | TEXT | YES | Additional notes |
| created_at | TIMESTAMP | NO | Record creation timestamp |

### Event Types

- **Trip Events**: TRIP_STARTED, TRIP_ENDED, TRIP_PAUSED, TRIP_RESUMED
- **Charging Events**: CHARGING_STARTED, CHARGING_COMPLETED, CHARGING_STOPPED, CHARGING_FAILED
- **Maintenance Events**: MAINTENANCE_SCHEDULED, MAINTENANCE_STARTED, MAINTENANCE_COMPLETED, MAINTENANCE_CANCELLED
- **Alert Events**: ALERT_RAISED, ALERT_RESOLVED, ALERT_ACKNOWLEDGED
- **Status Events**: STATUS_CHANGED, VEHICLE_STARTED, VEHICLE_STOPPED, VEHICLE_IDLE
- **Battery Events**: LOW_BATTERY, BATTERY_HEALTH_DEGRADED, BATTERY_TEMPERATURE_HIGH
- **Fuel Events**: LOW_FUEL, REFUELING_STARTED, REFUELING_COMPLETED
- **Geofence Events**: GEOFENCE_ENTERED, GEOFENCE_EXITED
- **Driver Events**: DRIVER_ASSIGNED, DRIVER_UNASSIGNED, HARSH_BRAKING, HARSH_ACCELERATION, OVER_SPEEDING
- **System Events**: DEVICE_CONNECTED, DEVICE_DISCONNECTED, FIRMWARE_UPDATED, CONFIGURATION_CHANGED
- **Other Events**: COLLISION_DETECTED, EMERGENCY_BUTTON_PRESSED, CUSTOM_EVENT

### Indexes

- `idx_vehicle_events_vehicle_id`: On vehicle_id
- `idx_vehicle_events_event_type`: On event_type
- `idx_vehicle_events_timestamp`: On event_timestamp DESC
- `idx_vehicle_events_vehicle_timestamp`: On (vehicle_id, event_timestamp DESC)
- `idx_vehicle_events_company_id`: On company_id
- `idx_vehicle_events_severity`: On severity (partial index for CRITICAL and HIGH)
- `idx_vehicle_events_event_data`: GIN index on event_data JSONB

### Usage Examples

```sql
-- Get all events for a vehicle
SELECT * FROM vehicle_events 
WHERE vehicle_id = 123 
ORDER BY event_timestamp DESC;

-- Get critical events
SELECT * FROM vehicle_events 
WHERE vehicle_id = 123 
AND severity IN ('HIGH', 'CRITICAL')
ORDER BY event_timestamp DESC;

-- Get events within a time range
SELECT * FROM vehicle_events 
WHERE vehicle_id = 123 
AND event_timestamp BETWEEN '2024-01-01' AND '2024-01-31'
ORDER BY event_timestamp DESC;

-- Get trip events
SELECT * FROM vehicle_events 
WHERE vehicle_id = 123 
AND event_type IN ('TRIP_STARTED', 'TRIP_ENDED')
ORDER BY event_timestamp DESC;
```

---

## Vehicle Current State Table

**Table Name**: `vehicle_current_state`

**Purpose**: Maintains the most recent state of each vehicle (snapshot). Provides quick access to latest vehicle status without querying historical data.

### Columns

| Column Name | Data Type | Nullable | Description |
|------------|-----------|----------|-------------|
| vehicle_id | BIGINT | NO | Primary key, foreign key to vehicles |
| status | VARCHAR(20) | NO | Current vehicle status |
| last_updated | TIMESTAMP | NO | Last update timestamp |
| latitude | DOUBLE PRECISION | YES | Current latitude |
| longitude | DOUBLE PRECISION | YES | Current longitude |
| location_name | VARCHAR(255) | YES | Current location name |
| heading | DOUBLE PRECISION | YES | Current heading/direction |
| speed | DOUBLE PRECISION | YES | Current speed |
| battery_soc | DOUBLE PRECISION | YES | Current battery state of charge |
| battery_health | DOUBLE PRECISION | YES | Current battery health percentage |
| battery_temperature | DOUBLE PRECISION | YES | Current battery temperature |
| fuel_level | DOUBLE PRECISION | YES | Current fuel level (for ICE/Hybrid) |
| odometer | DOUBLE PRECISION | YES | Current odometer reading |
| total_distance | DOUBLE PRECISION | YES | Total distance traveled |
| total_energy_consumed | DOUBLE PRECISION | YES | Total energy consumed (kWh) |
| total_fuel_consumed | DOUBLE PRECISION | YES | Total fuel consumed (liters) |
| current_driver_id | BIGINT | YES | Currently assigned driver |
| current_trip_id | BIGINT | YES | Current active trip ID |
| is_charging | BOOLEAN | YES | Is vehicle currently charging |
| is_in_maintenance | BOOLEAN | YES | Is vehicle in maintenance |
| is_in_trip | BOOLEAN | YES | Is vehicle in an active trip |
| charging_station_id | BIGINT | YES | Current charging station |
| charging_started_at | TIMESTAMP | YES | Charging start time |
| estimated_charging_completion | TIMESTAMP | YES | Estimated charging completion |
| last_maintenance_date | TIMESTAMP | YES | Last maintenance date |
| next_maintenance_due_date | TIMESTAMP | YES | Next maintenance due date |
| maintenance_status | VARCHAR(50) | YES | Maintenance status |
| active_alerts_count | INTEGER | YES | Number of active alerts |
| critical_alerts_count | INTEGER | YES | Number of critical alerts |
| last_alert_timestamp | TIMESTAMP | YES | Last alert timestamp |
| is_connected | BOOLEAN | YES | Is vehicle connected/online |
| last_telemetry_received | TIMESTAMP | YES | Last telemetry received time |
| signal_strength | INTEGER | YES | Signal strength |
| average_speed_last_trip | DOUBLE PRECISION | YES | Average speed of last trip |
| efficiency_score | DOUBLE PRECISION | YES | Calculated efficiency score (0-100) |
| company_id | BIGINT | NO | Company that owns the vehicle |
| created_at | TIMESTAMP | NO | Record creation timestamp |
| updated_at | TIMESTAMP | NO | Record update timestamp |

### Indexes

- `idx_vehicle_current_state_status`: On status
- `idx_vehicle_current_state_company_id`: On company_id
- `idx_vehicle_current_state_is_charging`: On is_charging (partial index for TRUE)
- `idx_vehicle_current_state_is_in_maintenance`: On is_in_maintenance (partial index for TRUE)
- `idx_vehicle_current_state_is_in_trip`: On is_in_trip (partial index for TRUE)
- `idx_vehicle_current_state_active_alerts`: On active_alerts_count (partial index for > 0)

### Triggers

- `trigger_update_vehicle_current_state_timestamp`: Automatically updates updated_at timestamp on updates

### Usage Examples

```sql
-- Get current state of a vehicle
SELECT * FROM vehicle_current_state WHERE vehicle_id = 123;

-- Get all vehicles currently charging
SELECT * FROM vehicle_current_state WHERE is_charging = TRUE;

-- Get vehicles with active alerts
SELECT * FROM vehicle_current_state WHERE active_alerts_count > 0;

-- Get disconnected vehicles
SELECT * FROM vehicle_current_state WHERE is_connected = FALSE;

-- Get vehicles in maintenance
SELECT * FROM vehicle_current_state WHERE is_in_maintenance = TRUE;
```

---

## API Endpoints

### Vehicle Events

**Base URL**: `/api/v1/vehicles`

#### Get Vehicle Events
```
GET /api/v1/vehicles/{vehicleId}/events
```
Query Parameters: `page`, `size`

#### Get Events by Type
```
GET /api/v1/vehicles/{vehicleId}/events/type/{eventType}
```

#### Get Events by Time Range
```
GET /api/v1/vehicles/{vehicleId}/events/range?startTime={ISO_DATE}&endTime={ISO_DATE}
```

#### Get Critical Events
```
GET /api/v1/vehicles/{vehicleId}/events/critical
```

#### Get Recent Events
```
GET /api/v1/vehicles/{vehicleId}/events/recent?days={N}
```

#### Record Event
```
POST /api/v1/vehicles/{vehicleId}/events
```

### Vehicle Current State

#### Get Vehicle Current State
```
GET /api/v1/vehicles/{vehicleId}/current-state
```

#### Get Company Vehicle States
```
GET /api/v1/vehicles/company/{companyId}/current-states
```

#### Get Charging Vehicles
```
GET /api/v1/vehicles/current-states/charging
```

#### Get Vehicles in Maintenance
```
GET /api/v1/vehicles/current-states/maintenance
```

#### Get Vehicles with Alerts
```
GET /api/v1/vehicles/current-states/with-alerts
```

---

## Report Generation

### Vehicle Reports (v.report)

**Base URL**: `/api/v1/analytics/reports`

#### Generate Comprehensive Vehicle Report
```
POST /api/v1/analytics/reports/vehicle
```

Request Body:
```json
{
  "vehicleId": 123,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "includeVehicleInfo": true,
  "includeEventHistory": true,
  "includeTripHistory": true,
  "includeMaintenanceHistory": true,
  "includeChargingHistory": true,
  "includeAlertHistory": true,
  "includePerformanceMetrics": true,
  "includeCostAnalysis": true
}
```

Response: PDF file (application/pdf)

#### Generate Genealogy Report
```
GET /api/v1/analytics/reports/vehicle/{vehicleId}/genealogy?startDate={ISO_DATE}&endDate={ISO_DATE}
```

Response: PDF file (application/pdf)

---

## Migration Files

- **V6__create_vehicle_events_and_current_state.sql**: Creates vehicle_events and vehicle_current_state tables

## Best Practices

1. **Event Recording**: Always record events through the VehicleEventService to ensure consistency
2. **State Updates**: Update vehicle_current_state whenever significant state changes occur
3. **Performance**: Use indexes for efficient querying of large event tables
4. **Data Retention**: Consider archiving old events based on company policy
5. **Monitoring**: Monitor the size of vehicle_events table and implement partitioning if needed

---

Last Updated: November 2024
