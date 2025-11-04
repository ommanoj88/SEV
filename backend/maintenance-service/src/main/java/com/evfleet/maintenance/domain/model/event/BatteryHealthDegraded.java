package com.evfleet.maintenance.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryHealthDegraded {
    private String vehicleId;
    private BigDecimal previousSoh;
    private BigDecimal currentSoh;
    private BigDecimal degradationRate;
    private LocalDateTime timestamp = LocalDateTime.now();

    public BatteryHealthDegraded(String vehicleId, BigDecimal previousSoh,
                                BigDecimal currentSoh, BigDecimal degradationRate) {
        this.vehicleId = vehicleId;
        this.previousSoh = previousSoh;
        this.currentSoh = currentSoh;
        this.degradationRate = degradationRate;
        this.timestamp = LocalDateTime.now();
    }
}
