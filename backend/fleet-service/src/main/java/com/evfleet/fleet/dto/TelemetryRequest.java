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
}
