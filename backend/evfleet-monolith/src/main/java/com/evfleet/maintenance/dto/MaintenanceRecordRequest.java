package com.evfleet.maintenance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Maintenance Record Request DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecordRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Maintenance type is required")
    private String type;  // ROUTINE_SERVICE, BATTERY_CHECK, TIRE_REPLACEMENT, BRAKE_SERVICE, EMERGENCY_REPAIR

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private LocalDate completedDate;

    private String status;  // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Positive(message = "Cost must be positive")
    private BigDecimal cost;

    private String description;

    private String serviceProvider;
}
