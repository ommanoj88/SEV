package com.evfleet.telematics.model;

import com.evfleet.fleet.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing historical telemetry snapshots.
 * Used for analytics, replay, and audit purposes.
 * 
 * Retention Policy: Records older than 90 days are auto-deleted.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "telemetry_snapshots", indexes = {
    @Index(name = "idx_telemetry_vehicle_timestamp", columnList = "vehicle_id, timestamp"),
    @Index(name = "idx_telemetry_timestamp", columnList = "timestamp"),
    @Index(name = "idx_telemetry_company", columnList = "company_id, timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== IDENTIFICATION =====
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "device_id", length = 50)
    private String deviceId; // IMEI or OEM vehicle ID

    @Enumerated(EnumType.STRING)
    @Column(name = "telemetry_source", length = 20)
    private Vehicle.TelemetrySource source;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    // ===== TIMESTAMP =====
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_quality", length = 20)
    private Vehicle.TelemetryDataQuality dataQuality;

    // ===== LOCATION =====
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "altitude")
    private Double altitude; // in meters

    @Column(name = "heading")
    private Double heading; // in degrees (0-360)

    @Column(name = "speed")
    private Double speed; // in km/h

    @Column(name = "satellites")
    private Integer satellites; // GPS satellite count

    // ===== ODOMETER & DISTANCE =====
    @Column(name = "odometer")
    private Double odometer; // Total distance in km

    @Column(name = "trip_distance")
    private Double tripDistance; // Current trip distance in km

    // ===== EV BATTERY DATA =====
    @Column(name = "battery_soc")
    private Double batterySoc; // State of Charge (0-100%)

    @Column(name = "battery_soh")
    private Double batterySoh; // State of Health (0-100%)

    @Column(name = "battery_voltage")
    private Double batteryVoltage; // in Volts

    @Column(name = "battery_current")
    private Double batteryCurrent; // in Amperes

    @Column(name = "battery_temperature")
    private Double batteryTemperature; // in Celsius

    @Column(name = "estimated_range")
    private Double estimatedRange; // in km

    @Column(name = "is_charging")
    private Boolean isCharging;

    @Column(name = "charging_status", length = 20)
    private String chargingStatus;

    // ===== ICE/HYBRID FUEL DATA =====
    @Column(name = "fuel_level")
    private Double fuelLevel; // in liters

    @Column(name = "fuel_percentage")
    private Double fuelPercentage; // 0-100%

    // ===== VEHICLE STATUS =====
    @Column(name = "ignition_on")
    private Boolean ignitionOn;

    @Column(name = "is_moving")
    private Boolean isMoving;

    @Column(name = "engine_rpm")
    private Double engineRpm;

    @Column(name = "vehicle_status", length = 20)
    private String vehicleStatus;

    // ===== DIAGNOSTICS =====
    @Column(name = "check_engine_light")
    private Boolean checkEngineLight;

    @Column(name = "dtc_count")
    private Integer diagnosticTroubleCodes;

    // ===== DRIVER BEHAVIOR =====
    @Column(name = "acceleration_x")
    private Double accelerationX;

    @Column(name = "acceleration_y")
    private Double accelerationY;

    @Column(name = "acceleration_z")
    private Double accelerationZ;

    // ===== METADATA =====
    @Column(name = "is_estimated")
    private Boolean isEstimated;

    @Column(name = "signal_strength")
    private Integer signalStrength;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Create a TelemetrySnapshot from VehicleTelemetryData DTO
     */
    public static TelemetrySnapshot fromTelemetryData(
            com.evfleet.telematics.dto.VehicleTelemetryData data, 
            Long companyId) {
        return TelemetrySnapshot.builder()
            .vehicleId(data.getVehicleId())
            .companyId(companyId)
            .deviceId(data.getDeviceId())
            .source(data.getSource())
            .providerName(data.getProviderName())
            .timestamp(data.getTimestamp())
            .dataQuality(data.getDataQuality())
            .latitude(data.getLatitude())
            .longitude(data.getLongitude())
            .altitude(data.getAltitude())
            .heading(data.getHeading())
            .speed(data.getSpeed())
            .satellites(data.getSatellites())
            .odometer(data.getOdometer())
            .tripDistance(data.getTripDistance())
            .batterySoc(data.getBatterySoc())
            .batterySoh(data.getBatterySoh())
            .batteryVoltage(data.getBatteryVoltage())
            .batteryCurrent(data.getBatteryCurrent())
            .batteryTemperature(data.getBatteryTemperature())
            .estimatedRange(data.getEstimatedRange())
            .isCharging(data.getIsCharging())
            .chargingStatus(data.getChargingStatus())
            .fuelLevel(data.getFuelLevel())
            .fuelPercentage(data.getFuelPercentage())
            .ignitionOn(data.getIgnitionOn())
            .isMoving(data.getIsMoving())
            .engineRpm(data.getEngineRpm())
            .vehicleStatus(data.getVehicleStatus())
            .checkEngineLight(data.getCheckEngineLight())
            .diagnosticTroubleCodes(data.getDiagnosticTroubleCodes())
            .accelerationX(data.getAccelerationX())
            .accelerationY(data.getAccelerationY())
            .accelerationZ(data.getAccelerationZ())
            .isEstimated(data.getIsEstimated())
            .signalStrength(data.getSignalStrength())
            .notes(data.getNotes())
            .build();
    }
}
