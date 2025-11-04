package com.evfleet.charging.dto;

import com.evfleet.charging.entity.ChargingSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionResponse {
    private Long id;
    private Long vehicleId;
    private Long stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal energyConsumed;
    private BigDecimal cost;
    private BigDecimal startBatteryLevel;
    private BigDecimal endBatteryLevel;
    private String status;
    private Long durationMinutes;
    private String transactionId;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public static ChargingSessionResponse from(ChargingSession session) {
        ChargingSessionResponse response = new ChargingSessionResponse();
        response.setId(session.getId());
        response.setVehicleId(session.getVehicleId());
        response.setStationId(session.getStationId());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setEnergyConsumed(session.getEnergyConsumed());
        response.setCost(session.getCost());
        response.setStartBatteryLevel(session.getStartBatteryLevel());
        response.setEndBatteryLevel(session.getEndBatteryLevel());
        response.setStatus(session.getStatus().name());
        response.setDurationMinutes(session.getDurationMinutes());
        response.setTransactionId(session.getTransactionId());
        response.setPaymentMethod(session.getPaymentMethod());
        response.setPaymentStatus(session.getPaymentStatus());
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }
}
