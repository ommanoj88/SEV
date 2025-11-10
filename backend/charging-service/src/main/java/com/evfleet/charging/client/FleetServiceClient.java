package com.evfleet.charging.client;

import com.evfleet.charging.dto.VehicleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with fleet-service
 * Used to fetch vehicle information for charging validation
 * 
 * @since PR-9 (Charging Validation)
 */
@FeignClient(name = "fleet-service", path = "/api/v1/vehicles")
public interface FleetServiceClient {
    
    /**
     * Get vehicle details by ID
     * 
     * @param vehicleId the vehicle ID
     * @return VehicleDTO with vehicle information
     */
    @GetMapping("/{id}")
    VehicleDTO getVehicleById(@PathVariable("id") Long vehicleId);
}
