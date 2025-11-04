package com.evfleet.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordResponse {

    private String vehicleId;
    private ScheduleInfo schedule;
    private HistoryInfo history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfo {
        private String id;
        private String serviceType;
        private LocalDate dueDate;
        private Integer dueMileage;
        private String status;
        private String priority;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryInfo {
        private String id;
        private LocalDate serviceDate;
        private String serviceType;
        private BigDecimal cost;
        private String serviceCenter;
        private String description;
        private List<String> partsReplaced;
        private String technician;
        private LocalDateTime createdAt;
    }
}
