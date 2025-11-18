package com.evfleet.maintenance.dto;

import com.evfleet.maintenance.model.MaintenanceRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maintenance Record Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecordResponse {

    private Long id;
    private Long vehicleId;
    private Long companyId;
    private String type;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String status;
    private BigDecimal cost;
    private String description;
    private String serviceProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MaintenanceRecordResponse fromEntity(MaintenanceRecord record) {
        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .vehicleId(record.getVehicleId())
                .companyId(record.getCompanyId())
                .type(record.getType().name())
                .scheduledDate(record.getScheduledDate())
                .completedDate(record.getCompletedDate())
                .status(record.getStatus().name())
                .cost(record.getCost())
                .description(record.getDescription())
                .serviceProvider(record.getServiceProvider())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
