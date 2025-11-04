package com.evfleet.maintenance.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCompleted {
    private String scheduleId;
    private String vehicleId;
    private String serviceType;
    private BigDecimal cost;
    private LocalDateTime completedAt;
    private LocalDateTime timestamp = LocalDateTime.now();

    public MaintenanceCompleted(String scheduleId, String vehicleId, String serviceType,
                               BigDecimal cost, LocalDateTime completedAt) {
        this.scheduleId = scheduleId;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.cost = cost;
        this.completedAt = completedAt;
        this.timestamp = LocalDateTime.now();
    }
}
