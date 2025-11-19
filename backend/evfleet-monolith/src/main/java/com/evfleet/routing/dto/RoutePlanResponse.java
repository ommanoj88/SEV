package com.evfleet.routing.dto;

import com.evfleet.routing.model.RoutePlan;
import com.evfleet.routing.model.RouteWaypoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for route plan
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanResponse {

    private Long id;
    private Long companyId;
    private Long vehicleId;
    private Long driverId;
    private String routeName;
    private RoutePlan.OptimizationCriteria optimizationCriteria;
    private RoutePlan.RouteStatus status;
    private Double totalDistance;
    private Long estimatedDuration;
    private Double estimatedCost;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String notes;
    private List<WaypointResponse> waypoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static RoutePlanResponse from(RoutePlan route) {
        return RoutePlanResponse.builder()
            .id(route.getId())
            .companyId(route.getCompanyId())
            .vehicleId(route.getVehicleId())
            .driverId(route.getDriverId())
            .routeName(route.getRouteName())
            .optimizationCriteria(route.getOptimizationCriteria())
            .status(route.getStatus())
            .totalDistance(route.getTotalDistance())
            .estimatedDuration(route.getEstimatedDuration())
            .estimatedCost(route.getEstimatedCost())
            .startedAt(route.getStartedAt())
            .completedAt(route.getCompletedAt())
            .notes(route.getNotes())
            .waypoints(route.getWaypoints().stream()
                .map(WaypointResponse::from)
                .collect(Collectors.toList()))
            .createdAt(route.getCreatedAt())
            .updatedAt(route.getUpdatedAt())
            .build();
    }

    /**
     * Convert entity to response DTO without waypoints (for list views)
     */
    public static RoutePlanResponse fromWithoutWaypoints(RoutePlan route) {
        return RoutePlanResponse.builder()
            .id(route.getId())
            .companyId(route.getCompanyId())
            .vehicleId(route.getVehicleId())
            .driverId(route.getDriverId())
            .routeName(route.getRouteName())
            .optimizationCriteria(route.getOptimizationCriteria())
            .status(route.getStatus())
            .totalDistance(route.getTotalDistance())
            .estimatedDuration(route.getEstimatedDuration())
            .estimatedCost(route.getEstimatedCost())
            .startedAt(route.getStartedAt())
            .completedAt(route.getCompletedAt())
            .notes(route.getNotes())
            .createdAt(route.getCreatedAt())
            .updatedAt(route.getUpdatedAt())
            .build();
    }
}
