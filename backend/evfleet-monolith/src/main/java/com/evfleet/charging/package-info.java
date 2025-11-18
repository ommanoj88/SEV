/**
 * Charging Management Module
 *
 * Handles EV charging infrastructure and session management.
 *
 * <h2>Module Responsibilities</h2>
 * - Charging station registration and management
 * - Charging session lifecycle management
 * - Real-time charging monitoring
 * - Cost calculation and billing integration
 * - Charging analytics and reporting
 * - Route optimization with charging stops
 *
 * <h2>API Endpoints</h2>
 * - /api/charging/stations - Station CRUD operations
 * - /api/charging/sessions - Session management
 * - /api/charging/analytics - Charging analytics
 *
 * <h2>Events Published</h2>
 * - ChargingSessionStartedEvent - When charging begins
 * - ChargingSessionCompletedEvent - When charging ends
 * - StationOccupiedEvent - When station slot is reserved
 * - StationAvailableEvent - When station slot is released
 *
 * <h2>Events Consumed</h2>
 * - VehicleCreatedEvent - To track chargeable vehicles
 * - BatteryLowEvent - To suggest nearby charging stations
 *
 * <h2>Module Dependencies</h2>
 * - Common module (for exceptions, DTOs, events)
 * - Fleet module (via events only)
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Charging Management Module",
        allowedDependencies = {"common", "fleet::event"}
)
package com.evfleet.charging;
