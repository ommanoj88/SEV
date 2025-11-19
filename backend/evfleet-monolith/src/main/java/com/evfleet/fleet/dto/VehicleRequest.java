package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String vehicleNumber;

    @NotNull
    private Vehicle.VehicleType type;

    private FuelType fuelType;

    @NotBlank
    private String make;

    @NotBlank
    private String model;

    @NotNull
    private Integer year;

    private Double batteryCapacity;
    private Double currentBatterySoc;
    private String defaultChargerType;
    private Double fuelTankCapacity;
    private Double fuelLevel;
    private String engineType;
    
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", 
             message = "VIN must be exactly 17 characters (excluding I, O, Q)")
    private String vin;
    
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$", 
             message = "License plate must follow Indian format (e.g., KA01AB1234)")
    private String licensePlate;
    
    private String color;
}
