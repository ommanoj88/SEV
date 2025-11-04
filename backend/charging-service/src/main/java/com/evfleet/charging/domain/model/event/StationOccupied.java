package com.evfleet.charging.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationOccupied {
    private String stationId;
    private String sessionId;
    private Integer availableSlots;
    private LocalDateTime timestamp = LocalDateTime.now();

    public StationOccupied(String stationId, String sessionId, Integer availableSlots) {
        this.stationId = stationId;
        this.sessionId = sessionId;
        this.availableSlots = availableSlots;
        this.timestamp = LocalDateTime.now();
    }
}
