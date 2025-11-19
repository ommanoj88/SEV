package com.evfleet.routing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Plan Entity
 *
 * Represents a planned route with multiple waypoints for fleet vehicles.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "route_plans", indexes = {
    @Index(name = "idx_route_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_route_company", columnList = "company_id"),
    @Index(name = "idx_route_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "route_name", nullable = false, length = 200)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "optimization_criteria", nullable = false, length = 20)
    private OptimizationCriteria optimizationCriteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RouteStatus status;

    @Column(name = "total_distance")
    private Double totalDistance; // in kilometers

    @Column(name = "estimated_duration")
    private Long estimatedDuration; // in minutes

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<RouteWaypoint> waypoints = new ArrayList<>();

    /**
     * Optimization criteria for route planning
     */
    public enum OptimizationCriteria {
        DISTANCE,  // Shortest distance
        TIME,      // Fastest route
        COST       // Lowest cost (fuel/energy)
    }

    /**
     * Route execution status
     */
    public enum RouteStatus {
        DRAFT,        // Route is being planned
        SCHEDULED,    // Route is scheduled but not started
        IN_PROGRESS,  // Route execution is in progress
        COMPLETED,    // Route has been completed
        CANCELLED     // Route was cancelled
    }

    /**
     * Helper method to add a waypoint to the route
     */
    public void addWaypoint(RouteWaypoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setRoutePlan(this);
    }

    /**
     * Helper method to remove a waypoint from the route
     */
    public void removeWaypoint(RouteWaypoint waypoint) {
        waypoints.remove(waypoint);
        waypoint.setRoutePlan(null);
    }
}
