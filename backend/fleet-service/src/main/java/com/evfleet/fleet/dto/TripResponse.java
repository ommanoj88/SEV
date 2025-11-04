package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for trip data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    private Long id;
    private Long vehicleId;
    private Long driverId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startLocation;
    private String endLocation;
    private Double distance;
    private Double energyConsumed;
    private Double averageSpeed;
    private Double maxSpeed;
    private Trip.TripStatus status;
    private Double startBatterySoc;
    private Double endBatterySoc;
    private Double startOdometer;
    private Double endOdometer;
    private Integer durationMinutes;
    private Integer idleTimeMinutes;
    private Integer harshAccelerationCount;
    private Integer harshBrakingCount;
    private Integer overspeedingCount;
    private Double efficiencyScore;
    private String routePolyline;
    private String waypoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long companyId;
    private String purpose;
    private String notes;

    public static TripResponse fromEntity(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .vehicleId(trip.getVehicleId())
                .driverId(trip.getDriverId())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .startLocation(trip.getStartLocation())
                .endLocation(trip.getEndLocation())
                .distance(trip.getDistance())
                .energyConsumed(trip.getEnergyConsumed())
                .averageSpeed(trip.getAverageSpeed())
                .maxSpeed(trip.getMaxSpeed())
                .status(trip.getStatus())
                .startBatterySoc(trip.getStartBatterySoc())
                .endBatterySoc(trip.getEndBatterySoc())
                .startOdometer(trip.getStartOdometer())
                .endOdometer(trip.getEndOdometer())
                .durationMinutes(trip.getDurationMinutes())
                .idleTimeMinutes(trip.getIdleTimeMinutes())
                .harshAccelerationCount(trip.getHarshAccelerationCount())
                .harshBrakingCount(trip.getHarshBrakingCount())
                .overspeedingCount(trip.getOverspeedingCount())
                .efficiencyScore(trip.getEfficiencyScore())
                .routePolyline(trip.getRoutePolyline())
                .waypoints(trip.getWaypoints())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .companyId(trip.getCompanyId())
                .purpose(trip.getPurpose())
                .notes(trip.getNotes())
                .build();
    }
}
