package com.evfleet.routing.dto;

import com.evfleet.routing.model.RouteWaypoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for adding a waypoint to a route
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaypointRequest {

    @NotNull(message = "Sequence is required")
    private Integer sequence;

    @NotNull(message = "Waypoint type is required")
    private RouteWaypoint.WaypointType waypointType;

    @NotBlank(message = "Location name is required")
    private String locationName;

    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private LocalDateTime scheduledArrival;

    private LocalDateTime scheduledDeparture;

    private String contactName;

    private String contactPhone;

    private String notes;
}
