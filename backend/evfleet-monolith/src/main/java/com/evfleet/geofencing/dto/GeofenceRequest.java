package com.evfleet.geofencing.dto;

import com.evfleet.geofencing.model.Geofence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a geofence
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Geofence name is required")
    private String name;

    @NotNull(message = "Geofence type is required")
    private Geofence.GeofenceType geofenceType;

    private String description;

    @NotNull(message = "Center latitude is required")
    private Double centerLatitude;

    @NotNull(message = "Center longitude is required")
    private Double centerLongitude;

    @NotNull(message = "Radius is required")
    private Double radius; // in meters

    private Double speedLimit; // in km/h

    private Boolean alertOnEntry;

    private Boolean alertOnExit;

    private Boolean isActive;

    private String color;
}
