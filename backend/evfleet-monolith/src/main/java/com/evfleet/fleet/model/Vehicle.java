package com.evfleet.fleet.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vehicle Entity
 *
 * Represents a vehicle in the fleet management system.
 * Supports multi-fuel types: EV, ICE, and Hybrid vehicles.
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_vehicle_number", columnList = "vehicle_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_fuel_type", columnList = "fuel_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {

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
    @Column(name = "battery_capacity")
    private Double batteryCapacity; // in kWh

    @Column(name = "current_battery_soc")
    private Double currentBatterySoc; // State of Charge (0-100)

    @Column(name = "default_charger_type", length = 50)
    private String defaultChargerType;

    // ===== ICE-SPECIFIC FIELDS =====
    @Column(name = "fuel_tank_capacity")
    private Double fuelTankCapacity; // in liters

    @Column(name = "fuel_level")
    private Double fuelLevel; // in liters

    @Column(name = "engine_type", length = 50)
    private String engineType;

    // ===== COMMON FIELDS =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

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

    @Column(name = "total_fuel_consumed", columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    private Double totalFuelConsumed; // in liters

    // ===== ENUMS =====
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
