package com.evfleet.fleet.validation;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for telemetry data based on vehicle fuel type.
 * Ensures that fuel-type-specific telemetry fields are provided correctly.
 * 
 * Validation Rules:
 * - EV vehicles: Should provide battery-related metrics (batterySoc, batteryVoltage, etc.)
 * - ICE vehicles: Should provide engine-related metrics (fuelLevel, engineRpm, etc.)
 * - HYBRID vehicles: Should provide both battery and engine metrics
 * 
 * @since 2.0.0 (Multi-fuel support - PR 6)
 */
@Component
@Slf4j
public class TelemetryValidator {

    /**
     * Validates telemetry data against the vehicle's fuel type.
     * 
     * @param request Telemetry data to validate
     * @param vehicle Vehicle associated with the telemetry
     * @throws IllegalArgumentException if validation fails
     */
    public void validateTelemetryForVehicle(TelemetryRequest request, Vehicle vehicle) {
        FuelType fuelType = vehicle.getFuelType();
        
        // Default to EV if fuel type is null (backward compatibility)
        if (fuelType == null) {
            fuelType = FuelType.EV;
        }

        log.debug("Validating telemetry for vehicle {} with fuel type {}", vehicle.getId(), fuelType);

        switch (fuelType) {
            case EV:
                validateEVTelemetry(request, vehicle.getId());
                break;
            case ICE:
                validateICETelemetry(request, vehicle.getId());
                break;
            case HYBRID:
                validateHybridTelemetry(request, vehicle.getId());
                break;
        }
    }

    /**
     * Validates telemetry for EV vehicles.
     * EV vehicles should provide battery-related metrics.
     */
    private void validateEVTelemetry(TelemetryRequest request, Long vehicleId) {
        // Battery SOC is the most critical metric for EVs
        if (request.getBatterySoc() != null && (request.getBatterySoc() < 0 || request.getBatterySoc() > 100)) {
            throw new IllegalArgumentException(
                String.format("Invalid battery SOC for EV vehicle %d: %.2f. Must be between 0 and 100", 
                    vehicleId, request.getBatterySoc())
            );
        }

        // Warn if EV telemetry contains ICE-specific fields (but don't fail)
        if (hasICESpecificData(request)) {
            log.warn("EV vehicle {} received telemetry with ICE-specific fields. These will be ignored.", vehicleId);
        }
    }

    /**
     * Validates telemetry for ICE vehicles.
     * ICE vehicles should provide engine-related metrics.
     */
    private void validateICETelemetry(TelemetryRequest request, Long vehicleId) {
        // Fuel level validation
        if (request.getFuelLevel() != null && request.getFuelLevel() < 0) {
            throw new IllegalArgumentException(
                String.format("Invalid fuel level for ICE vehicle %d: %.2f. Must be non-negative", 
                    vehicleId, request.getFuelLevel())
            );
        }

        // Engine RPM validation
        if (request.getEngineRpm() != null && (request.getEngineRpm() < 0 || request.getEngineRpm() > 10000)) {
            throw new IllegalArgumentException(
                String.format("Invalid engine RPM for ICE vehicle %d: %d. Must be between 0 and 10000", 
                    vehicleId, request.getEngineRpm())
            );
        }

        // Engine load validation
        if (request.getEngineLoad() != null && (request.getEngineLoad() < 0 || request.getEngineLoad() > 100)) {
            throw new IllegalArgumentException(
                String.format("Invalid engine load for ICE vehicle %d: %.2f. Must be between 0 and 100", 
                    vehicleId, request.getEngineLoad())
            );
        }

        // Engine hours validation
        if (request.getEngineHours() != null && request.getEngineHours() < 0) {
            throw new IllegalArgumentException(
                String.format("Invalid engine hours for ICE vehicle %d: %.2f. Must be non-negative", 
                    vehicleId, request.getEngineHours())
            );
        }

        // Warn if ICE telemetry contains EV-specific charging fields
        if (request.getIsCharging() != null && request.getIsCharging()) {
            log.warn("ICE vehicle {} received telemetry with isCharging=true. This will be ignored.", vehicleId);
        }
    }

    /**
     * Validates telemetry for HYBRID vehicles.
     * HYBRID vehicles should provide both battery and engine metrics.
     */
    private void validateHybridTelemetry(TelemetryRequest request, Long vehicleId) {
        // Validate both EV and ICE metrics for hybrid vehicles
        validateEVTelemetry(request, vehicleId);
        validateICETelemetry(request, vehicleId);
        
        // For hybrid vehicles, having both battery and fuel data is normal
        log.debug("Validated hybrid telemetry for vehicle {}", vehicleId);
    }

    /**
     * Checks if telemetry contains ICE-specific data.
     */
    private boolean hasICESpecificData(TelemetryRequest request) {
        return request.getFuelLevel() != null 
            || request.getEngineRpm() != null 
            || request.getEngineTemperature() != null 
            || request.getEngineLoad() != null 
            || request.getEngineHours() != null;
    }
}
