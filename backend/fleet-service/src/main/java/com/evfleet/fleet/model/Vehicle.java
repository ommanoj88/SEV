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
 * Vehicle Entity
 * Represents an electric vehicle in the fleet management system
 */
@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_vehicle_number", columnList = "vehicle_number"),
    @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private VehicleType type;

    @Column(name = "make", nullable = false, length = 100)
    private String make;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "battery_capacity", nullable = false)
    private Double batteryCapacity; // in kWh

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "current_battery_soc")
    private Double currentBatterySoc; // State of Charge (0-100)

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "vin", length = 17)
    private String vin; // Vehicle Identification Number

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "current_driver_id")
    private Long currentDriverId;

    @Column(name = "total_distance", columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    private Double totalDistance; // in kilometers

    @Column(name = "total_energy_consumed", columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    private Double totalEnergyConsumed; // in kWh

    public enum VehicleType {
        TWO_WHEELER,
        THREE_WHEELER,
        LCV
    }

    public enum VehicleStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        IN_TRIP,
        CHARGING
    }
}
