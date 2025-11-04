package com.evfleet.fleet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a trip
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private Long driverId;

    @NotNull(message = "Start location is required")
    private String startLocation; // JSON format

    private String purpose;
    private String notes;
}
