package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for vehicle data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private Long id;
    private Long companyId;
    private String vehicleNumber;
    private Vehicle.VehicleType type;
    
    // PR 1: Multi-fuel support fields
    private com.evfleet.fleet.model.FuelType fuelType;
    
    private String make;
    private String model;
    private Integer year;
    
    // EV-specific fields
    private Double batteryCapacity;
    private Vehicle.VehicleStatus status;
    private Double currentBatterySoc;
    private String defaultChargerType;
    
    // ICE-specific fields (PR 1: Multi-fuel support)
    private Double fuelTankCapacity;
    private Double fuelLevel;
    private String engineType;
    
    // Common fields
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String vin;
    private String licensePlate;
    private String color;
    private Long currentDriverId;
    private Double totalDistance;
    private Double totalEnergyConsumed;
    private Double totalFuelConsumed;

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .companyId(vehicle.getCompanyId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .type(vehicle.getType())
                .fuelType(vehicle.getFuelType())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .batteryCapacity(vehicle.getBatteryCapacity())
                .status(vehicle.getStatus())
                .currentBatterySoc(vehicle.getCurrentBatterySoc())
                .defaultChargerType(vehicle.getDefaultChargerType())
                .fuelTankCapacity(vehicle.getFuelTankCapacity())
                .fuelLevel(vehicle.getFuelLevel())
                .engineType(vehicle.getEngineType())
                .latitude(vehicle.getLatitude())
                .longitude(vehicle.getLongitude())
                .lastUpdated(vehicle.getLastUpdated())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .vin(vehicle.getVin())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .currentDriverId(vehicle.getCurrentDriverId())
                .totalDistance(vehicle.getTotalDistance())
                .totalEnergyConsumed(vehicle.getTotalEnergyConsumed())
                .totalFuelConsumed(vehicle.getTotalFuelConsumed())
                .build();
    }
}
