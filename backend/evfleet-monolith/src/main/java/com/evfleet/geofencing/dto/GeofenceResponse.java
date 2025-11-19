package com.evfleet.geofencing.dto;

import com.evfleet.geofencing.model.Geofence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for geofence
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceResponse {

    private Long id;
    private Long companyId;
    private String name;
    private Geofence.GeofenceType geofenceType;
    private String description;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radius;
    private Double speedLimit;
    private Boolean alertOnEntry;
    private Boolean alertOnExit;
    private Boolean isActive;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static GeofenceResponse from(Geofence geofence) {
        return GeofenceResponse.builder()
            .id(geofence.getId())
            .companyId(geofence.getCompanyId())
            .name(geofence.getName())
            .geofenceType(geofence.getGeofenceType())
            .description(geofence.getDescription())
            .centerLatitude(geofence.getCenterLatitude())
            .centerLongitude(geofence.getCenterLongitude())
            .radius(geofence.getRadius())
            .speedLimit(geofence.getSpeedLimit())
            .alertOnEntry(geofence.getAlertOnEntry())
            .alertOnExit(geofence.getAlertOnExit())
            .isActive(geofence.getIsActive())
            .color(geofence.getColor())
            .createdAt(geofence.getCreatedAt())
            .updatedAt(geofence.getUpdatedAt())
            .build();
    }
}
