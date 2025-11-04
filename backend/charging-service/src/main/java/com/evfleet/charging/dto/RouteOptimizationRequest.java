package com.evfleet.charging.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptimizationRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotBlank(message = "Origin is required")
    private String origin;

    private Double originLat;
    private Double originLng;

    @NotBlank(message = "Destination is required")
    private String destination;

    private Double destinationLat;
    private Double destinationLng;

    @NotNull(message = "Current battery level is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal currentBatteryLevel;

    private BigDecimal batteryCapacity; // kWh
    private BigDecimal averageConsumption; // kWh per km
}
