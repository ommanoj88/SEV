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
 * Trip Entity
 * Represents a trip made by a vehicle
 */
@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_driver_id", columnList = "driver_id"),
    @Index(name = "idx_start_time", columnList = "start_time"),
    @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_location", nullable = false, length = 500)
    private String startLocation; // JSON format: {"lat": x, "lng": y, "address": "..."}

    @Column(name = "end_location", length = 500)
    private String endLocation; // JSON format: {"lat": x, "lng": y, "address": "..."}

    @Column(name = "distance")
    private Double distance; // in kilometers

    @Column(name = "energy_consumed")
    private Double energyConsumed; // in kWh

    @Column(name = "average_speed")
    private Double averageSpeed; // in km/h

    @Column(name = "max_speed")
    private Double maxSpeed; // in km/h

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "start_battery_soc")
    private Double startBatterySoc;

    @Column(name = "end_battery_soc")
    private Double endBatterySoc;

    @Column(name = "start_odometer")
    private Double startOdometer;

    @Column(name = "end_odometer")
    private Double endOdometer;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "idle_time_minutes")
    private Integer idleTimeMinutes;

    @Column(name = "harsh_acceleration_count")
    private Integer harshAccelerationCount;

    @Column(name = "harsh_braking_count")
    private Integer harshBrakingCount;

    @Column(name = "overspeeding_count")
    private Integer overspeedingCount;

    @Column(name = "efficiency_score")
    private Double efficiencyScore; // 0-100

    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline; // Encoded polyline of the route

    @Column(name = "waypoints", columnDefinition = "TEXT")
    private String waypoints; // JSON array of waypoints

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum TripStatus {
        ONGOING,
        COMPLETED,
        CANCELLED,
        PAUSED
    }
}
