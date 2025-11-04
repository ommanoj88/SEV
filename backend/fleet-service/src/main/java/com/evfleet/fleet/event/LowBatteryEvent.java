package com.evfleet.fleet.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when vehicle battery is low
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowBatteryEvent {

    private Long vehicleId;
    private Long companyId;
    private Double batterySoc;
    private LocalDateTime timestamp;
    private String severity; // WARNING, CRITICAL
}
