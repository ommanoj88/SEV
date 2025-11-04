package com.evfleet.charging.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionFailed {
    private String sessionId;
    private String vehicleId;
    private String stationId;
    private String reason;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ChargingSessionFailed(String sessionId, String vehicleId, String stationId, String reason) {
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}
