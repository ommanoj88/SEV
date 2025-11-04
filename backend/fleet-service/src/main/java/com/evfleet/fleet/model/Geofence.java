package com.evfleet.fleet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Geofence Entity
 * Represents a geographic boundary for monitoring vehicle entry/exit
 */
@Entity
@Table(name = "geofences", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private GeofenceType type;

    @Column(name = "coordinates", nullable = false, columnDefinition = "TEXT")
    private String coordinates; // JSON array of lat/lng points for polygon or single point for circle

    @Column(name = "radius")
    private Double radius; // in meters (for circular geofences)

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "center_latitude")
    private Double centerLatitude;

    @Column(name = "center_longitude")
    private Double centerLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "shape", nullable = false, length = 20)
    private GeofenceShape shape;

    @Column(name = "color", length = 20)
    private String color; // Hex color code for UI display

    @Column(name = "alert_on_entry")
    private Boolean alertOnEntry = false;

    @Column(name = "alert_on_exit")
    private Boolean alertOnExit = false;

    @Column(name = "speed_limit")
    private Double speedLimit; // in km/h

    @Column(name = "allowed_vehicles", columnDefinition = "TEXT")
    private String allowedVehicles; // JSON array of vehicle IDs

    @Column(name = "restricted_vehicles", columnDefinition = "TEXT")
    private String restrictedVehicles; // JSON array of vehicle IDs

    @Column(name = "schedule_start_time")
    private String scheduleStartTime; // HH:MM format

    @Column(name = "schedule_end_time")
    private String scheduleEndTime; // HH:MM format

    @Column(name = "schedule_days", length = 100)
    private String scheduleDays; // Comma-separated days (MON,TUE,WED,etc.)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    public enum GeofenceType {
        CHARGING_ZONE,
        DEPOT,
        RESTRICTED,
        CUSTOMER_LOCATION,
        SERVICE_AREA,
        NO_GO_ZONE,
        PARKING_AREA
    }

    public enum GeofenceShape {
        CIRCLE,
        POLYGON,
        RECTANGLE
    }
}
