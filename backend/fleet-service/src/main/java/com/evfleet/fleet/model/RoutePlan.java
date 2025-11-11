package com.evfleet.fleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route name is required")
    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "route_description", columnDefinition = "TEXT")
    private String routeDescription;

    // Assignment
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    // Origin
    @NotNull(message = "Origin latitude is required")
    @Column(name = "origin_lat", nullable = false, precision = 10, scale = 8)
    private BigDecimal originLat;

    @NotNull(message = "Origin longitude is required")
    @Column(name = "origin_lng", nullable = false, precision = 11, scale = 8)
    private BigDecimal originLng;

    @Column(name = "origin_address", columnDefinition = "TEXT")
    private String originAddress;

    // Destination
    @NotNull(message = "Destination latitude is required")
    @Column(name = "destination_lat", nullable = false, precision = 10, scale = 8)
    private BigDecimal destinationLat;

    @NotNull(message = "Destination longitude is required")
    @Column(name = "destination_lng", nullable = false, precision = 11, scale = 8)
    private BigDecimal destinationLng;

    @Column(name = "destination_address", columnDefinition = "TEXT")
    private String destinationAddress;

    // Route Metrics
    @Column(name = "total_distance", precision = 10, scale = 2)
    private BigDecimal totalDistance; // km

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // minutes

    @Column(name = "estimated_fuel_consumption", precision = 10, scale = 2)
    private BigDecimal estimatedFuelConsumption;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    // Time Windows
    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;

    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;

    // Optimization Parameters
    @Column(name = "optimization_criteria", length = 50)
    private String optimizationCriteria; // 'DISTANCE', 'TIME', 'FUEL', 'COST'

    @Column(name = "traffic_considered")
    private Boolean trafficConsidered = true;

    @Column(name = "toll_roads_allowed")
    private Boolean tollRoadsAllowed = true;

    // Status
    @Column(length = 50)
    private String status = "PLANNED"; // 'PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'

    // Actual Performance
    @Column(name = "actual_distance", precision = 10, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "actual_duration")
    private Integer actualDuration;

    @Column(name = "actual_fuel_consumption", precision = 10, scale = 2)
    private BigDecimal actualFuelConsumption;

    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;

    // Waypoints relationship
    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteWaypoint> waypoints = new ArrayList<>();

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Helper methods
    public void addWaypoint(RouteWaypoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setRoutePlan(this);
    }

    public void removeWaypoint(RouteWaypoint waypoint) {
        waypoints.remove(waypoint);
        waypoint.setRoutePlan(null);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    public BigDecimal getDistanceSaved() {
        if (totalDistance != null && actualDistance != null) {
            return totalDistance.subtract(actualDistance);
        }
        return BigDecimal.ZERO;
    }

    public Integer getTimeSaved() {
        if (estimatedDuration != null && actualDuration != null) {
            return estimatedDuration - actualDuration;
        }
        return 0;
    }
}
