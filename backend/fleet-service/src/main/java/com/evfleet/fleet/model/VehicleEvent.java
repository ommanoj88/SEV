package com.evfleet.fleet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * VehicleEvent Entity
 * Represents a historical event record for a vehicle
 * Used for event sourcing and genealogy tracking
 */
@Entity
@Table(name = "vehicle_events", indexes = {
    @Index(name = "idx_vehicle_events_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_vehicle_events_event_type", columnList = "event_type"),
    @Index(name = "idx_vehicle_events_timestamp", columnList = "event_timestamp"),
    @Index(name = "idx_vehicle_events_company_id", columnList = "company_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "event_subtype", length = 50)
    private String eventSubtype;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private EventSeverity severity;

    // Event details stored as JSON
    @Column(name = "event_data", columnDefinition = "jsonb")
    private String eventData;

    // Location information
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    // Related entities
    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "maintenance_id")
    private Long maintenanceId;

    @Column(name = "charging_session_id")
    private Long chargingSessionId;

    // Event metrics
    @Column(name = "battery_soc")
    private Double batterySoc;

    @Column(name = "fuel_level")
    private Double fuelLevel;

    @Column(name = "odometer")
    private Double odometer;

    @Column(name = "speed")
    private Double speed;

    // Metadata
    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // Additional context
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit trail
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Event Type Enum
     * Defines all possible event types
     */
    public enum EventType {
        // Trip events
        TRIP_STARTED,
        TRIP_ENDED,
        TRIP_PAUSED,
        TRIP_RESUMED,

        // Charging events
        CHARGING_STARTED,
        CHARGING_COMPLETED,
        CHARGING_STOPPED,
        CHARGING_FAILED,

        // Maintenance events
        MAINTENANCE_SCHEDULED,
        MAINTENANCE_STARTED,
        MAINTENANCE_COMPLETED,
        MAINTENANCE_CANCELLED,

        // Alert events
        ALERT_RAISED,
        ALERT_RESOLVED,
        ALERT_ACKNOWLEDGED,

        // Status events
        STATUS_CHANGED,
        VEHICLE_STARTED,
        VEHICLE_STOPPED,
        VEHICLE_IDLE,

        // Battery events
        LOW_BATTERY,
        BATTERY_HEALTH_DEGRADED,
        BATTERY_TEMPERATURE_HIGH,

        // Fuel events (for ICE vehicles)
        LOW_FUEL,
        REFUELING_STARTED,
        REFUELING_COMPLETED,

        // Geofence events
        GEOFENCE_ENTERED,
        GEOFENCE_EXITED,

        // Driver events
        DRIVER_ASSIGNED,
        DRIVER_UNASSIGNED,
        HARSH_BRAKING,
        HARSH_ACCELERATION,
        OVER_SPEEDING,

        // System events
        DEVICE_CONNECTED,
        DEVICE_DISCONNECTED,
        FIRMWARE_UPDATED,
        CONFIGURATION_CHANGED,

        // Other events
        COLLISION_DETECTED,
        EMERGENCY_BUTTON_PRESSED,
        CUSTOM_EVENT
    }

    /**
     * Event Severity Enum
     */
    public enum EventSeverity {
        INFO,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
