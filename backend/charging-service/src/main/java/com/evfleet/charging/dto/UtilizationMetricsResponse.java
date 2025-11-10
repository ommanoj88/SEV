package com.evfleet.charging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for overall network utilization metrics
 * Contains aggregated data across all charging stations
 * 
 * @since PR-10 (Charging Analytics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilizationMetricsResponse {
    
    private Integer totalStations;
    private Integer activeStations;
    private Integer totalSlots;
    private Integer availableSlots;
    private Integer occupiedSlots;
    
    // Overall utilization
    private BigDecimal overallUtilizationRate; // Percentage (0-100)
    private BigDecimal averageStationUtilization; // Average across all stations
    
    // Session statistics
    private Long totalSessions;
    private Long activeSessions;
    private Long completedSessionsToday;
    
    // Top performing stations
    private List<StationUtilizationSummary> topStations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StationUtilizationSummary {
        private Long stationId;
        private String stationName;
        private BigDecimal utilizationRate;
        private Long sessionsCount;
    }
}
