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
    private String make;
    private String model;
    private Integer year;
    private Double batteryCapacity;
    private Vehicle.VehicleStatus status;
    private Double currentBatterySoc;
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

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .companyId(vehicle.getCompanyId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .type(vehicle.getType())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .batteryCapacity(vehicle.getBatteryCapacity())
                .status(vehicle.getStatus())
                .currentBatterySoc(vehicle.getCurrentBatterySoc())
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
                .build();
    }
}
