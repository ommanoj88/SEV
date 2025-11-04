package com.evfleet.maintenance.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceScheduled {
    private String scheduleId;
    private String vehicleId;
    private String serviceType;
    private LocalDateTime dueDate;
    private LocalDateTime timestamp = LocalDateTime.now();

    public MaintenanceScheduled(String scheduleId, String vehicleId, String serviceType, LocalDateTime dueDate) {
        this.scheduleId = scheduleId;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.dueDate = dueDate;
        this.timestamp = LocalDateTime.now();
    }
}
