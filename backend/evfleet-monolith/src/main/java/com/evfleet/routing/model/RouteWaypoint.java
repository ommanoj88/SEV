package com.evfleet.routing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Route Waypoint Entity
 *
 * Represents a stop/waypoint in a route plan.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "route_waypoints", indexes = {
    @Index(name = "idx_waypoint_route", columnList = "route_plan_id"),
    @Index(name = "idx_waypoint_sequence", columnList = "route_plan_id, sequence")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteWaypoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_plan_id", nullable = false)
    private RoutePlan routePlan;

    @Column(name = "sequence", nullable = false)
    private Integer sequence; // Order in the route

    @Enumerated(EnumType.STRING)
    @Column(name = "waypoint_type", nullable = false, length = 20)
    private WaypointType waypointType;

    @Column(name = "location_name", nullable = false, length = 300)
    private String locationName;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WaypointStatus status = WaypointStatus.PENDING;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Type of waypoint/stop
     */
    public enum WaypointType {
        PICKUP,      // Pick up goods/passengers
        DELIVERY,    // Deliver goods/passengers
        SERVICE,     // Service stop
        CHECKPOINT,  // Intermediate checkpoint
        BREAK        // Rest/break stop
    }

    /**
     * Waypoint completion status
     */
    public enum WaypointStatus {
        PENDING,     // Not yet visited
        IN_PROGRESS, // Currently at this waypoint
        COMPLETED,   // Successfully completed
        SKIPPED      // Skipped for some reason
    }

    /**
     * Mark waypoint as arrived
     */
    public void markArrived() {
        this.actualArrival = LocalDateTime.now();
        this.status = WaypointStatus.IN_PROGRESS;
    }

    /**
     * Mark waypoint as departed/completed
     */
    public void markDeparted() {
        this.actualDeparture = LocalDateTime.now();
        this.status = WaypointStatus.COMPLETED;
    }
}
