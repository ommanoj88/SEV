package com.evfleet.charging.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationAvailable {
    private String stationId;
    private Integer availableSlots;
    private LocalDateTime timestamp = LocalDateTime.now();

    public StationAvailable(String stationId, Integer availableSlots) {
        this.stationId = stationId;
        this.availableSlots = availableSlots;
        this.timestamp = LocalDateTime.now();
    }
}
