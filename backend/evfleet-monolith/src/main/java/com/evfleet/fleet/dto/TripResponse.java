package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private Long vehicleId;
    private Long driverId;
    private Long companyId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Double distance;
    private Long duration;
    private BigDecimal energyConsumed;
    private BigDecimal fuelConsumed;
    private Trip.TripStatus status;
    private String notes;
    private LocalDateTime createdAt;

    public static TripResponse from(Trip trip) {
        return TripResponse.builder()
            .id(trip.getId())
            .vehicleId(trip.getVehicleId())
            .driverId(trip.getDriverId())
            .companyId(trip.getCompanyId())
            .startTime(trip.getStartTime())
            .endTime(trip.getEndTime())
            .startLatitude(trip.getStartLatitude())
            .startLongitude(trip.getStartLongitude())
            .endLatitude(trip.getEndLatitude())
            .endLongitude(trip.getEndLongitude())
            .distance(trip.getDistance())
            .duration(trip.getDuration())
            .energyConsumed(trip.getEnergyConsumed())
            .fuelConsumed(trip.getFuelConsumed())
            .status(trip.getStatus())
            .notes(trip.getNotes())
            .createdAt(trip.getCreatedAt())
            .build();
    }
}
