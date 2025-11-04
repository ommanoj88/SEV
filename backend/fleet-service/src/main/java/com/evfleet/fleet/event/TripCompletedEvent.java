package com.evfleet.fleet.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a trip is completed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripCompletedEvent {

    private Long tripId;
    private Long vehicleId;
    private Long driverId;
    private Long companyId;
    private Double distance;
    private Double energyConsumed;
    private Integer durationMinutes;
    private Double efficiencyScore;
    private LocalDateTime completedAt;
}
