package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Vehicle;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a vehicle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is required")
    private Vehicle.VehicleType type;

    // PR 1: Multi-fuel support - fuel type field
    private com.evfleet.fleet.model.FuelType fuelType;

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer year;

    // EV-specific fields (optional for ICE vehicles)
    @Positive(message = "Battery capacity must be positive")
    private Double batteryCapacity;

    @NotNull(message = "Status is required")
    private Vehicle.VehicleStatus status;
    
    @Size(max = 50, message = "Charger type must not exceed 50 characters")
    private String defaultChargerType;

    // ICE-specific fields (PR 1: Multi-fuel support - optional for EV vehicles)
    @Positive(message = "Fuel tank capacity must be positive")
    private Double fuelTankCapacity;

    @PositiveOrZero(message = "Fuel level must be zero or positive")
    private Double fuelLevel;

    @Size(max = 50, message = "Engine type must not exceed 50 characters")
    private String engineType;

    // Common fields
    @Size(max = 17, message = "VIN must not exceed 17 characters")
    private String vin;

    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Min(value = 0, message = "Battery SOC must be at least 0")
    @Max(value = 100, message = "Battery SOC must not exceed 100")
    private Double currentBatterySoc;
}
