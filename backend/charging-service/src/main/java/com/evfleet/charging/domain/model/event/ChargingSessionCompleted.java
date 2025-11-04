package com.evfleet.charging.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionCompleted {
    private String sessionId;
    private String vehicleId;
    private String stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal energyConsumed;
    private BigDecimal cost;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ChargingSessionCompleted(String sessionId, String vehicleId, String stationId,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    BigDecimal energyConsumed, BigDecimal cost) {
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyConsumed = energyConsumed;
        this.cost = cost;
        this.timestamp = LocalDateTime.now();
    }
}
