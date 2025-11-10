package com.evfleet.charging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Vehicle information from fleet-service
 * Contains minimal fields needed for charging validation
 * 
 * @since PR-9 (Charging Validation)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String vehicleNumber;
    private String fuelType;
    private Double batteryCapacity;
    private Double currentBatterySoc;
    private String status;
}
