package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.BatteryHealth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Battery Health data
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryHealthResponse {

    private Long id;
    private Long vehicleId;
    private Double soh;
    private Integer cycleCount;
    private Double temperature;
    private Double internalResistance;
    private Double voltageDeviation;
    private Double currentSoc;
    private LocalDateTime recordedAt;
    private String notes;

    public static BatteryHealthResponse fromEntity(BatteryHealth entity) {
        return BatteryHealthResponse.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .soh(entity.getSoh())
                .cycleCount(entity.getCycleCount())
                .temperature(entity.getTemperature())
                .internalResistance(entity.getInternalResistance())
                .voltageDeviation(entity.getVoltageDeviation())
                .currentSoc(entity.getCurrentSoc())
                .recordedAt(entity.getRecordedAt())
                .notes(entity.getNotes())
                .build();
    }
}
