package com.evfleet.fleet.validation;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.model.FuelType;
import org.springframework.stereotype.Component;

/**
 * Validator for fuel type specific validation rules
 * Ensures that vehicles have the required fields based on their fuel type
 * 
 * Validation Rules:
 * - EV vehicles: Must have batteryCapacity
 * - HYBRID vehicles: Must have both batteryCapacity AND fuelTankCapacity
 * - ICE vehicles: Must have fuelTankCapacity
 * 
 * @since 2.0.0 (PR 5: Vehicle CRUD API Updates)
 */
@Component
public class FuelTypeValidator {

    /**
     * Validates that a vehicle request has all required fields for its fuel type
     * 
     * @param request The vehicle request to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateVehicleRequest(VehicleRequest request) {
        if (request.getFuelType() == null) {
            // If no fuel type specified, default to EV for backward compatibility
            return;
        }

        FuelType fuelType = request.getFuelType();

        switch (fuelType) {
            case EV:
                validateEVRequirements(request);
                break;
            case ICE:
                validateICERequirements(request);
                break;
            case HYBRID:
                validateHybridRequirements(request);
                break;
            default:
                throw new IllegalArgumentException("Invalid fuel type: " + fuelType);
        }
    }

    /**
     * Validates requirements for EV vehicles
     * EV vehicles must have battery capacity
     */
    private void validateEVRequirements(VehicleRequest request) {
        if (request.getBatteryCapacity() == null || request.getBatteryCapacity() <= 0) {
            throw new IllegalArgumentException(
                "Battery capacity is required and must be positive for EV vehicles"
            );
        }
    }

    /**
     * Validates requirements for ICE vehicles
     * ICE vehicles must have fuel tank capacity
     */
    private void validateICERequirements(VehicleRequest request) {
        if (request.getFuelTankCapacity() == null || request.getFuelTankCapacity() <= 0) {
            throw new IllegalArgumentException(
                "Fuel tank capacity is required and must be positive for ICE vehicles"
            );
        }
    }

    /**
     * Validates requirements for Hybrid vehicles
     * Hybrid vehicles must have both battery capacity AND fuel tank capacity
     */
    private void validateHybridRequirements(VehicleRequest request) {
        if (request.getBatteryCapacity() == null || request.getBatteryCapacity() <= 0) {
            throw new IllegalArgumentException(
                "Battery capacity is required and must be positive for HYBRID vehicles"
            );
        }
        if (request.getFuelTankCapacity() == null || request.getFuelTankCapacity() <= 0) {
            throw new IllegalArgumentException(
                "Fuel tank capacity is required and must be positive for HYBRID vehicles"
            );
        }
    }

    /**
     * Validates that optional fields are not set for incompatible fuel types
     * For example, ICE vehicles should not have battery-related fields
     * 
     * @param request The vehicle request to validate
     */
    public void validateOptionalFieldsConsistency(VehicleRequest request) {
        if (request.getFuelType() == null) {
            return;
        }

        FuelType fuelType = request.getFuelType();

        // ICE vehicles should not have battery-related fields
        if (fuelType == FuelType.ICE) {
            if (request.getBatteryCapacity() != null && request.getBatteryCapacity() > 0) {
                throw new IllegalArgumentException(
                    "Battery capacity should not be set for ICE vehicles"
                );
            }
            if (request.getDefaultChargerType() != null && !request.getDefaultChargerType().isEmpty()) {
                throw new IllegalArgumentException(
                    "Charger type should not be set for ICE vehicles"
                );
            }
        }

        // EV vehicles should not have fuel-related fields
        if (fuelType == FuelType.EV) {
            if (request.getFuelTankCapacity() != null && request.getFuelTankCapacity() > 0) {
                throw new IllegalArgumentException(
                    "Fuel tank capacity should not be set for EV vehicles"
                );
            }
            if (request.getEngineType() != null && !request.getEngineType().isEmpty()) {
                throw new IllegalArgumentException(
                    "Engine type should not be set for EV vehicles"
                );
            }
        }
    }
}
