package com.evfleet.fleet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for submitting telemetry data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speed;
    private Double batterySoc;
    private Double batteryVoltage;
    private Double batteryCurrent;
    private Double batteryTemperature;
    private Double odometer;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    private Double heading;
    private Double altitude;
    private Double powerConsumption;
    private Double regenerativePower;
    private Double motorTemperature;
    private Double controllerTemperature;
    private Boolean isCharging;
    private Boolean isIgnitionOn;
    private Long tripId;
    private String errorCodes;
    private Integer signalStrength;

    // ===== ICE-SPECIFIC FIELDS (for ICE and HYBRID vehicles) =====
    /**
     * Current fuel level in liters
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double fuelLevel; // in liters

    /**
     * Engine RPM (Revolutions Per Minute)
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Integer engineRpm;

    /**
     * Engine temperature in Celsius
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double engineTemperature; // in Celsius

    /**
     * Engine load percentage (0-100)
     * Indicates how hard the engine is working
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double engineLoad; // 0-100%

    /**
     * Total engine operating hours
     * Used for maintenance scheduling
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double engineHours; // in hours
}
