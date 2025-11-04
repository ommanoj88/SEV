package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Geofence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a geofence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Type is required")
    private Geofence.GeofenceType type;

    @NotBlank(message = "Coordinates are required")
    private String coordinates;

    private Double radius;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Shape is required")
    private Geofence.GeofenceShape shape;

    private String color;
    private Boolean alertOnEntry;
    private Boolean alertOnExit;
    private Double speedLimit;
    private String allowedVehicles;
    private String restrictedVehicles;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private String scheduleDays;
}
