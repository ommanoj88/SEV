package com.evfleet.charging.dto;

import com.evfleet.charging.entity.RouteOptimization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptimizationResponse {
    private Long id;
    private Long vehicleId;
    private String origin;
    private String destination;
    private String chargingStops;
    private Integer numberOfStops;
    private Long estimatedTimeMinutes;
    private BigDecimal estimatedCost;
    private BigDecimal totalDistance;
    private BigDecimal estimatedEnergyRequired;
    private BigDecimal currentBatteryLevel;
    private BigDecimal estimatedFinalBatteryLevel;
    private String status;
    private String routeDetails;
    private LocalDateTime createdAt;

    public static RouteOptimizationResponse from(RouteOptimization route) {
        RouteOptimizationResponse response = new RouteOptimizationResponse();
        response.setId(route.getId());
        response.setVehicleId(route.getVehicleId());
        response.setOrigin(route.getOrigin());
        response.setDestination(route.getDestination());
        response.setChargingStops(route.getChargingStops());
        response.setNumberOfStops(route.getNumberOfStops());
        response.setEstimatedTimeMinutes(route.getEstimatedTimeMinutes());
        response.setEstimatedCost(route.getEstimatedCost());
        response.setTotalDistance(route.getTotalDistance());
        response.setEstimatedEnergyRequired(route.getEstimatedEnergyRequired());
        response.setCurrentBatteryLevel(route.getCurrentBatteryLevel());
        response.setEstimatedFinalBatteryLevel(route.getEstimatedFinalBatteryLevel());
        response.setStatus(route.getStatus().name());
        response.setRouteDetails(route.getRouteDetails());
        response.setCreatedAt(route.getCreatedAt());
        return response;
    }
}
