package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Geofence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for geofence data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeofenceResponse {

    private Long id;
    private Long companyId;
    private String name;
    private Geofence.GeofenceType type;
    private String coordinates;
    private Double radius;
    private Boolean active;
    private String description;
    private Double centerLatitude;
    private Double centerLongitude;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public static GeofenceResponse fromEntity(Geofence geofence) {
        return GeofenceResponse.builder()
                .id(geofence.getId())
                .companyId(geofence.getCompanyId())
                .name(geofence.getName())
                .type(geofence.getType())
                .coordinates(geofence.getCoordinates())
                .radius(geofence.getRadius())
                .active(geofence.getActive())
                .description(geofence.getDescription())
                .centerLatitude(geofence.getCenterLatitude())
                .centerLongitude(geofence.getCenterLongitude())
                .shape(geofence.getShape())
                .color(geofence.getColor())
                .alertOnEntry(geofence.getAlertOnEntry())
                .alertOnExit(geofence.getAlertOnExit())
                .speedLimit(geofence.getSpeedLimit())
                .allowedVehicles(geofence.getAllowedVehicles())
                .restrictedVehicles(geofence.getRestrictedVehicles())
                .scheduleStartTime(geofence.getScheduleStartTime())
                .scheduleEndTime(geofence.getScheduleEndTime())
                .scheduleDays(geofence.getScheduleDays())
                .createdAt(geofence.getCreatedAt())
                .updatedAt(geofence.getUpdatedAt())
                .createdBy(geofence.getCreatedBy())
                .build();
    }
}
