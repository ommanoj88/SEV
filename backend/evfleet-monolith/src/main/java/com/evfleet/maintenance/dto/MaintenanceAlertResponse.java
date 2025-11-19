package com.evfleet.maintenance.dto;

import com.evfleet.fleet.model.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for Maintenance Alerts
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceAlertResponse {

    private Long id;
    private Long vehicleId;
    private String vehicleNumber;
    private FuelType fuelType;
    private String maintenanceType;
    private LocalDate scheduledDate;
    private String status;
    private Priority priority;
    private String description;
    private Integer daysUntilDue;

    public enum Priority {
        HIGH,    // Overdue
        MEDIUM,  // Due within 7 days
        LOW      // Due within 30 days
    }
}
