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
                .build();
    }
}
