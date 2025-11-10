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
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_fuel_type", columnList = "fuel_type")
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

    /**
     * Fuel/Power type of the vehicle (ICE, EV, or HYBRID)
     * Default is EV for backward compatibility with existing vehicles
     * @since 2.0.0 (Multi-fuel support)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column(name = "make", nullable = false, length = 100)
    private String make;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "\"year\"", nullable = false)
    private Integer year;

    // ===== EV-SPECIFIC FIELDS =====
    /**
     * Battery capacity in kWh (for EV and HYBRID vehicles)
     * Required for: FuelType.EV, FuelType.HYBRID
     * Optional for: FuelType.ICE (should be null)
     */
    @Column(name = "battery_capacity")
    private Double batteryCapacity; // in kWh

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VehicleStatus status;

    /**
     * Current battery State of Charge (0-100) for EV and HYBRID vehicles
     * Required for: FuelType.EV, FuelType.HYBRID
     * Optional for: FuelType.ICE (should be null)
     */
    @Column(name = "current_battery_soc")
    private Double currentBatterySoc; // State of Charge (0-100)

    /**
     * Default charger type for EV vehicles (e.g., CCS, CHAdeMO, Type 2)
     * Relevant for: FuelType.EV, FuelType.HYBRID
     */
    @Column(name = "default_charger_type", length = 50)
    private String defaultChargerType;

    // ===== ICE-SPECIFIC FIELDS =====
    /**
     * Fuel tank capacity in liters (for ICE and HYBRID vehicles)
     * Required for: FuelType.ICE, FuelType.HYBRID
     * Optional for: FuelType.EV (should be null)
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "fuel_tank_capacity")
    private Double fuelTankCapacity; // in liters

    /**
     * Current fuel level in liters (for ICE and HYBRID vehicles)
     * Required for: FuelType.ICE, FuelType.HYBRID
     * Optional for: FuelType.EV (should be null)
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "fuel_level")
    private Double fuelLevel; // in liters

    /**
     * Type of fuel used (Petrol, Diesel, CNG, etc.)
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "engine_type", length = 50)
    private String engineType;

    // ===== COMMON FIELDS ====

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

    /**
     * Total fuel consumed by the vehicle (for ICE and HYBRID)
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "total_fuel_consumed", columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    private Double totalFuelConsumed; // in liters

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
