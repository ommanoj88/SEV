package com.evfleet.charging.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionStarted {
    private String sessionId;
    private String vehicleId;
    private String stationId;
    private LocalDateTime startTime;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ChargingSessionStarted(String sessionId, String vehicleId, String stationId, LocalDateTime startTime) {
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.startTime = startTime;
        this.timestamp = LocalDateTime.now();
    }
}
