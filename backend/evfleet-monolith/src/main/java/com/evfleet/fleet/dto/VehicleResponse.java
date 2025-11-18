package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private Long companyId;
    private String vehicleNumber;
    private Vehicle.VehicleType type;
    private FuelType fuelType;
    private String make;
    private String model;
    private Integer year;
    private Double batteryCapacity;
    private Double currentBatterySoc;
    private String defaultChargerType;
    private Double fuelTankCapacity;
    private Double fuelLevel;
    private String engineType;
    private Vehicle.VehicleStatus status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;
    private String vin;
    private String licensePlate;
    private String color;
    private Long currentDriverId;
    private String assignedDriverName;  // Driver's name for UI display
    private Double totalDistance;
    private Double totalEnergyConsumed;
    private Double totalFuelConsumed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleResponse from(Vehicle vehicle) {
        return from(vehicle, null);
    }

    public static VehicleResponse from(Vehicle vehicle, String driverName) {
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
            .currentBatterySoc(vehicle.getCurrentBatterySoc())
            .defaultChargerType(vehicle.getDefaultChargerType())
            .fuelTankCapacity(vehicle.getFuelTankCapacity())
            .fuelLevel(vehicle.getFuelLevel())
            .engineType(vehicle.getEngineType())
            .status(vehicle.getStatus())
            .latitude(vehicle.getLatitude())
            .longitude(vehicle.getLongitude())
            .lastUpdated(vehicle.getLastUpdated())
            .vin(vehicle.getVin())
            .licensePlate(vehicle.getLicensePlate())
            .color(vehicle.getColor())
            .currentDriverId(vehicle.getCurrentDriverId())
            .assignedDriverName(driverName)  // Set the driver name if provided
            .totalDistance(vehicle.getTotalDistance())
            .totalEnergyConsumed(vehicle.getTotalEnergyConsumed())
            .totalFuelConsumed(vehicle.getTotalFuelConsumed())
            .createdAt(vehicle.getCreatedAt())
            .updatedAt(vehicle.getUpdatedAt())
            .build();
    }
}
