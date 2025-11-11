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
 * VehicleCurrentState Entity
 * Maintains the current state snapshot of each vehicle
 * Provides quick access to latest vehicle status without querying historical data
 */
@Entity
@Table(name = "vehicle_current_state", indexes = {
    @Index(name = "idx_vehicle_current_state_status", columnList = "status"),
    @Index(name = "idx_vehicle_current_state_company_id", columnList = "company_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCurrentState {

    @Id
    @Column(name = "vehicle_id")
    private Long vehicleId;

    // Basic status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Vehicle.VehicleStatus status;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // Location
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "heading")
    private Double heading;

    @Column(name = "speed")
    private Double speed;

    // Battery/Fuel status
    @Column(name = "battery_soc")
    private Double batterySoc;

    @Column(name = "battery_health")
    private Double batteryHealth;

    @Column(name = "battery_temperature")
    private Double batteryTemperature;

    @Column(name = "fuel_level")
    private Double fuelLevel;

    // Odometer and usage
    @Column(name = "odometer")
    private Double odometer;

    @Column(name = "total_distance")
    private Double totalDistance = 0.0;

    @Column(name = "total_energy_consumed")
    private Double totalEnergyConsumed = 0.0;

    @Column(name = "total_fuel_consumed")
    private Double totalFuelConsumed = 0.0;

    // Current activity
    @Column(name = "current_driver_id")
    private Long currentDriverId;

    @Column(name = "current_trip_id")
    private Long currentTripId;

    @Column(name = "is_charging")
    private Boolean isCharging = false;

    @Column(name = "is_in_maintenance")
    private Boolean isInMaintenance = false;

    @Column(name = "is_in_trip")
    private Boolean isInTrip = false;

    // Charging info
    @Column(name = "charging_station_id")
    private Long chargingStationId;

    @Column(name = "charging_started_at")
    private LocalDateTime chargingStartedAt;

    @Column(name = "estimated_charging_completion")
    private LocalDateTime estimatedChargingCompletion;

    // Maintenance info
    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_due_date")
    private LocalDateTime nextMaintenanceDueDate;

    @Column(name = "maintenance_status", length = 50)
    private String maintenanceStatus;

    // Alert status
    @Column(name = "active_alerts_count")
    private Integer activeAlertsCount = 0;

    @Column(name = "critical_alerts_count")
    private Integer criticalAlertsCount = 0;

    @Column(name = "last_alert_timestamp")
    private LocalDateTime lastAlertTimestamp;

    // Connection status
    @Column(name = "is_connected")
    private Boolean isConnected = true;

    @Column(name = "last_telemetry_received")
    private LocalDateTime lastTelemetryReceived;

    @Column(name = "signal_strength")
    private Integer signalStrength;

    // Performance metrics
    @Column(name = "average_speed_last_trip")
    private Double averageSpeedLastTrip;

    @Column(name = "efficiency_score")
    private Double efficiencyScore;

    // Company reference
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
