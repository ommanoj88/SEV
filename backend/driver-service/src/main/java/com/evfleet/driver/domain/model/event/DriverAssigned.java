package com.evfleet.driver.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssigned {
    private String driverId;
    private String vehicleId;
    private LocalDateTime assignedAt;
    private LocalDateTime timestamp = LocalDateTime.now();

    public DriverAssigned(String driverId, String vehicleId, LocalDateTime assignedAt) {
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.assignedAt = assignedAt;
        this.timestamp = LocalDateTime.now();
    }
}
