package com.evfleet.charging.dto;

import com.evfleet.charging.model.ChargingSession;
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
public class ChargingSessionResponse {
    private Long id;
    private Long vehicleId;
    private Long stationId;
    private Long companyId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal energyConsumed;
    private BigDecimal cost;
    private ChargingSession.SessionStatus status;
    private Double initialSoc;
    private Double finalSoc;
    private String notes;

    public static ChargingSessionResponse from(ChargingSession session) {
        return ChargingSessionResponse.builder()
            .id(session.getId())
            .vehicleId(session.getVehicleId())
            .stationId(session.getStationId())
            .companyId(session.getCompanyId())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .energyConsumed(session.getEnergyConsumed())
            .cost(session.getCost())
            .status(session.getStatus())
            .initialSoc(session.getInitialSoc())
            .finalSoc(session.getFinalSoc())
            .notes(session.getNotes())
            .build();
    }
}
