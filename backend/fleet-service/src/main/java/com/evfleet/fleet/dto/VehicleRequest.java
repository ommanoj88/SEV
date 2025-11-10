package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Vehicle creation/update request")
public class VehicleRequest {

    @NotNull(message = "Company ID is required")
    @Schema(description = "Company ID that owns the vehicle", example = "1")
    private Long companyId;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
    @Schema(description = "Unique vehicle registration number", example = "KA01AB1234")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is required")
    @Schema(description = "Type of vehicle", example = "LCV")
    private Vehicle.VehicleType type;

    // PR 1: Multi-fuel support - fuel type field
    @Schema(description = "Fuel/Power type of the vehicle. Determines required fields and available features.", 
            example = "EV",
            allowableValues = {"EV", "ICE", "HYBRID"})
    private com.evfleet.fleet.model.FuelType fuelType;

    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    @Schema(description = "Vehicle manufacturer", example = "Tesla")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    @Schema(description = "Vehicle model", example = "Model 3")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year must not exceed 2100")
    @Schema(description = "Manufacturing year", example = "2024")
    private Integer year;

    // EV-specific fields (optional for ICE vehicles)
    @Positive(message = "Battery capacity must be positive")
    @Schema(description = "Battery capacity in kWh. Required for EV and HYBRID vehicles.", example = "75.0")
    private Double batteryCapacity;

    @NotNull(message = "Status is required")
    @Schema(description = "Current operational status", example = "ACTIVE")
    private Vehicle.VehicleStatus status;
    
    @Size(max = 50, message = "Charger type must not exceed 50 characters")
    @Schema(description = "Default charger type for EV/HYBRID vehicles", example = "CCS")
    private String defaultChargerType;

    // ICE-specific fields (PR 1: Multi-fuel support - optional for EV vehicles)
    @Positive(message = "Fuel tank capacity must be positive")
    @Schema(description = "Fuel tank capacity in liters. Required for ICE and HYBRID vehicles.", example = "60.0")
    private Double fuelTankCapacity;

    @PositiveOrZero(message = "Fuel level must be zero or positive")
    @Schema(description = "Current fuel level in liters", example = "45.0")
    private Double fuelLevel;

    @Size(max = 50, message = "Engine type must not exceed 50 characters")
    @Schema(description = "Type of engine for ICE/HYBRID vehicles", example = "Petrol")
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
