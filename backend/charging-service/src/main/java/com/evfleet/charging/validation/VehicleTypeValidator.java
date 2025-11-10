package com.evfleet.charging.validation;

import com.evfleet.charging.client.FleetServiceClient;
import com.evfleet.charging.dto.VehicleDTO;
import com.evfleet.charging.exception.NotAnEVVehicleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator to ensure only EV and HYBRID vehicles can charge
 * ICE vehicles are not allowed to use charging stations
 * 
 * @since PR-9 (Charging Validation)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleTypeValidator {
    
    private final FleetServiceClient fleetServiceClient;
    
    /**
     * Validates that the vehicle supports charging
     * Only EV and HYBRID vehicles are allowed
     * 
     * @param vehicleId the vehicle ID to validate
     * @throws NotAnEVVehicleException if the vehicle is ICE type
     */
    public void validateVehicleCanCharge(Long vehicleId) {
        log.debug("Validating vehicle {} can charge", vehicleId);
        
        VehicleDTO vehicle = fleetServiceClient.getVehicleById(vehicleId);
        
        if (vehicle == null) {
            log.error("Vehicle {} not found", vehicleId);
            throw new IllegalArgumentException("Vehicle not found with ID: " + vehicleId);
        }
        
        String fuelType = vehicle.getFuelType();
        
        // Allow charging only for EV and HYBRID vehicles
        if (fuelType == null || fuelType.equals("ICE")) {
            log.warn("ICE vehicle {} attempted to charge", vehicleId);
            throw new NotAnEVVehicleException(vehicleId, fuelType != null ? fuelType : "UNKNOWN");
        }
        
        if (!fuelType.equals("EV") && !fuelType.equals("HYBRID")) {
            log.warn("Vehicle {} with unknown fuel type {} attempted to charge", vehicleId, fuelType);
            throw new NotAnEVVehicleException(vehicleId, fuelType);
        }
        
        log.debug("Vehicle {} validated successfully for charging (fuel type: {})", vehicleId, fuelType);
    }
}
