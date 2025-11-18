/**
 * Fleet Management Module
 *
 * Core module for vehicle and fleet management operations.
 * Handles vehicle tracking, telemetry, trips, and geofencing.
 *
 * <h2>Module Responsibilities</h2>
 * - Vehicle registration and management
 * - Real-time vehicle tracking and telemetry
 * - Trip management and analytics
 * - Geofence management
 * - Route planning and optimization
 * - Document management (insurance, RC, etc.)
 * - Fuel consumption tracking (EV, ICE, Hybrid)
 *
 * <h2>API Endpoints</h2>
 * - /api/fleet/vehicles - Vehicle CRUD operations
 * - /api/fleet/trips - Trip management
 * - /api/fleet/telemetry - Telemetry data ingestion
 * - /api/fleet/geofences - Geofence management
 * - /api/fleet/documents - Vehicle document management
 *
 * <h2>Events Published</h2>
 * - VehicleCreatedEvent - When a new vehicle is registered
 * - VehicleLocationUpdatedEvent - When vehicle location changes
 * - TripStartedEvent - When a trip begins
 * - TripCompletedEvent - When a trip ends
 * - BatteryLowEvent - When battery drops below threshold
 * - GeofenceViolationEvent - When vehicle exits geofence
 *
 * <h2>Events Consumed</h2>
 * - UserRegisteredEvent - To initialize fleet for new companies
 *
 * <h2>Module Dependencies</h2>
 * - Common module (for exceptions, DTOs, events)
 * - Auth module (for user/company information) - via events only
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Fleet Management Module",
        allowedDependencies = {"common", "auth::event"}
)
package com.evfleet.fleet;
