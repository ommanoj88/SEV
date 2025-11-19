package com.evfleet.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for Battery Health data
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryHealthRequest {

    private Long vehicleId;
    private Double soh;
    private Integer cycleCount;
    private Double temperature;
    private Double internalResistance;
    private Double voltageDeviation;
    private Double currentSoc;
    private LocalDateTime recordedAt;
    private String notes;
}
