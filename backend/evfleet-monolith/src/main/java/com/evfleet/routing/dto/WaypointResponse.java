package com.evfleet.routing.dto;

import com.evfleet.routing.model.RouteWaypoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for route waypoint
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaypointResponse {

    private Long id;
    private Long routePlanId;
    private Integer sequence;
    private RouteWaypoint.WaypointType waypointType;
    private String locationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private RouteWaypoint.WaypointStatus status;
    private String contactName;
    private String contactPhone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static WaypointResponse from(RouteWaypoint waypoint) {
        return WaypointResponse.builder()
            .id(waypoint.getId())
            .routePlanId(waypoint.getRoutePlan() != null ? waypoint.getRoutePlan().getId() : null)
            .sequence(waypoint.getSequence())
            .waypointType(waypoint.getWaypointType())
            .locationName(waypoint.getLocationName())
            .address(waypoint.getAddress())
            .latitude(waypoint.getLatitude())
            .longitude(waypoint.getLongitude())
            .scheduledArrival(waypoint.getScheduledArrival())
            .actualArrival(waypoint.getActualArrival())
            .scheduledDeparture(waypoint.getScheduledDeparture())
            .actualDeparture(waypoint.getActualDeparture())
            .status(waypoint.getStatus())
            .contactName(waypoint.getContactName())
            .contactPhone(waypoint.getContactPhone())
            .notes(waypoint.getNotes())
            .createdAt(waypoint.getCreatedAt())
            .updatedAt(waypoint.getUpdatedAt())
            .build();
    }
}
