package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.TelemetryData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for telemetry data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryResponse {

    private Long id;
    private Long vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double batterySoc;
    private Double batteryVoltage;
    private Double batteryCurrent;
    private Double batteryTemperature;
    private Double odometer;
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
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double engineLoad; // 0-100%

    /**
     * Total engine operating hours
     * Relevant for: FuelType.ICE, FuelType.HYBRID
     * @since 2.0.0 (Multi-fuel support)
     */
    private Double engineHours; // in hours

    public static TelemetryResponse fromEntity(TelemetryData telemetry) {
        return TelemetryResponse.builder()
                .id(telemetry.getId())
                .vehicleId(telemetry.getVehicleId())
                .latitude(telemetry.getLatitude())
                .longitude(telemetry.getLongitude())
                .speed(telemetry.getSpeed())
                .batterySoc(telemetry.getBatterySoc())
                .batteryVoltage(telemetry.getBatteryVoltage())
                .batteryCurrent(telemetry.getBatteryCurrent())
                .batteryTemperature(telemetry.getBatteryTemperature())
                .odometer(telemetry.getOdometer())
                .timestamp(telemetry.getTimestamp())
                .heading(telemetry.getHeading())
                .altitude(telemetry.getAltitude())
                .powerConsumption(telemetry.getPowerConsumption())
                .regenerativePower(telemetry.getRegenerativePower())
                .motorTemperature(telemetry.getMotorTemperature())
                .controllerTemperature(telemetry.getControllerTemperature())
                .isCharging(telemetry.getIsCharging())
                .isIgnitionOn(telemetry.getIsIgnitionOn())
                .tripId(telemetry.getTripId())
                .errorCodes(telemetry.getErrorCodes())
                .signalStrength(telemetry.getSignalStrength())
                .fuelLevel(telemetry.getFuelLevel())
                .engineRpm(telemetry.getEngineRpm())
                .engineTemperature(telemetry.getEngineTemperature())
                .engineLoad(telemetry.getEngineLoad())
                .engineHours(telemetry.getEngineHours())
                .build();
    }
}
