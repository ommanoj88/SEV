package com.evfleet.fleet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TelemetryData Entity
 * Stores real-time telemetry data from vehicles
 */
@Entity
@Table(name = "telemetry_data", indexes = {
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_vehicle_timestamp", columnList = "vehicle_id, timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "speed")
    private Double speed; // in km/h

    @Column(name = "battery_soc")
    private Double batterySoc; // State of Charge (0-100)

    @Column(name = "battery_voltage")
    private Double batteryVoltage; // in Volts

    @Column(name = "battery_current")
    private Double batteryCurrent; // in Amperes

    @Column(name = "battery_temperature")
    private Double batteryTemperature; // in Celsius

    @Column(name = "odometer")
    private Double odometer; // in kilometers

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "heading")
    private Double heading; // Direction in degrees (0-360)

    @Column(name = "altitude")
    private Double altitude; // in meters

    @Column(name = "power_consumption")
    private Double powerConsumption; // in kW

    @Column(name = "regenerative_power")
    private Double regenerativePower; // in kW

    @Column(name = "motor_temperature")
    private Double motorTemperature; // in Celsius

    @Column(name = "controller_temperature")
    private Double controllerTemperature; // in Celsius

    @Column(name = "is_charging")
    private Boolean isCharging;

    @Column(name = "is_ignition_on")
    private Boolean isIgnitionOn;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "error_codes", length = 500)
    private String errorCodes; // Comma-separated error codes

    @Column(name = "signal_strength")
    private Integer signalStrength; // GPS/Network signal strength

    // ===== ICE-SPECIFIC FIELDS (for ICE and HYBRID vehicles) =====
    /**
     * Current fuel level in liters
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "fuel_level")
    private Double fuelLevel; // in liters

    /**
     * Engine RPM (Revolutions Per Minute)
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "engine_rpm")
    private Integer engineRpm;

    /**
     * Engine temperature in Celsius
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "engine_temperature")
    private Double engineTemperature; // in Celsius

    /**
     * Engine load percentage (0-100)
     * Indicates how hard the engine is working
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "engine_load")
    private Double engineLoad; // 0-100%

    /**
     * Total engine operating hours
     * Used for maintenance scheduling
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    @Column(name = "engine_hours")
    private Double engineHours; // in hours
}
