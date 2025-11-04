package com.evfleet.fleet.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when vehicle location is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLocationEvent {

    private Long vehicleId;
    private Long companyId;
    private Double latitude;
    private Double longitude;
    private Double batterySoc;
    private LocalDateTime timestamp;
}
