package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String vin;
    private String licensePlate;
    private String color;
}
