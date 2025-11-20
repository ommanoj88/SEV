package com.evfleet.telematics.dto;

import com.evfleet.fleet.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Normalized vehicle telemetry data from any source
 * This is the unified format that all providers must produce
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTelemetryData {

    // ===== IDENTIFICATION =====
    private Long vehicleId;
    private String deviceId; // IMEI or OEM vehicle ID
    private Vehicle.TelemetrySource source;
    private String providerName; // "tata_fleetedge", "teltonika_fmc003", etc.

    // ===== TIMESTAMP =====
    private LocalDateTime timestamp;
    private Vehicle.TelemetryDataQuality dataQuality;

    // ===== LOCATION =====
    private Double latitude;
    private Double longitude;
    private Double altitude; // in meters
    private Double heading; // in degrees (0-360)
    private Double speed; // in km/h
    private Integer satellites; // GPS satellite count

    // ===== ODOMETER & DISTANCE =====
    private Double odometer; // Total distance in km
    private Double tripDistance; // Current trip distance in km

    // ===== EV BATTERY DATA =====
    private Double batterySoc; // State of Charge (0-100%)
    private Double batterySoh; // State of Health (0-100%)
    private Double batteryVoltage; // in Volts
    private Double batteryCurrent; // in Amperes
    private Double batteryTemperature; // in Celsius
    private Double estimatedRange; // in km
    private Boolean isCharging;
    private String chargingStatus; // "NOT_CHARGING", "AC_CHARGING", "DC_CHARGING", "COMPLETE"

    // ===== ICE/HYBRID FUEL DATA =====
    private Double fuelLevel; // in liters
    private Double fuelPercentage; // 0-100%

    // ===== VEHICLE STATUS =====
    private Boolean ignitionOn;
    private Boolean isMoving;
    private Double engineRpm; // For ICE/Hybrid
    private String vehicleStatus; // "PARKED", "DRIVING", "IDLE", "CHARGING"

    // ===== DIAGNOSTICS =====
    private Boolean checkEngineLight;
    private Integer diagnosticTroubleCodes; // Count of active DTCs
    private String[] activeFaults; // Array of fault codes

    // ===== DRIVER BEHAVIOR (from accelerometer) =====
    private Double accelerationX; // Lateral G-force
    private Double accelerationY; // Longitudinal G-force
    private Double accelerationZ; // Vertical G-force

    // ===== METADATA =====
    private Boolean isEstimated; // True if values are calculated, not real
    private String notes; // Provider-specific notes or warnings
    private Integer signalStrength; // GSM/LTE signal strength (0-100)

    /**
     * Checks if this data is fresh enough to be considered real-time
     */
    public boolean isRealTime() {
        if (timestamp == null) return false;
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return timestamp.isAfter(fiveMinutesAgo);
    }

    /**
     * Checks if battery data is available
     */
    public boolean hasBatteryData() {
        return batterySoc != null || batteryVoltage != null;
    }

    /**
     * Checks if location data is valid
     */
    public boolean hasValidLocation() {
        return latitude != null && longitude != null &&
               latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180;
    }
}
