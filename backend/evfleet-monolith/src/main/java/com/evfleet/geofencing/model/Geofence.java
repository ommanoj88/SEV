package com.evfleet.geofencing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Geofence Entity
 *
 * Represents a geographical boundary/zone for fleet management.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "geofences", indexes = {
    @Index(name = "idx_geofence_company", columnList = "company_id"),
    @Index(name = "idx_geofence_type", columnList = "geofence_type"),
    @Index(name = "idx_geofence_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Geofence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "geofence_type", nullable = false, length = 30)
    private GeofenceType geofenceType;

    @Column(name = "description", length = 500)
    private String description;

    // Center coordinates
    @Column(name = "center_latitude", nullable = false)
    private Double centerLatitude;

    @Column(name = "center_longitude", nullable = false)
    private Double centerLongitude;

    // Radius in meters (for circular geofences)
    @Column(name = "radius", nullable = false)
    private Double radius;

    // Optional speed limit in km/h
    @Column(name = "speed_limit")
    private Double speedLimit;

    // Alert settings
    @Column(name = "alert_on_entry")
    @Builder.Default
    private Boolean alertOnEntry = false;

    @Column(name = "alert_on_exit")
    @Builder.Default
    private Boolean alertOnExit = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "color", length = 20)
    private String color; // For UI visualization

    /**
     * Types of geofences
     */
    public enum GeofenceType {
        CHARGING_ZONE,     // Charging station area
        DEPOT,             // Vehicle depot/parking
        NO_GO_ZONE,        // Restricted area
        SERVICE_AREA,      // Customer service area
        DELIVERY_ZONE,     // Delivery area
        MAINTENANCE_ZONE,  // Maintenance facility
        CUSTOM             // Custom zone
    }

    /**
     * Check if a point is inside this geofence
     * Using simple circular distance calculation
     */
    public boolean containsPoint(double latitude, double longitude) {
        double distance = calculateDistance(centerLatitude, centerLongitude, latitude, longitude);
        return distance <= radius;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
