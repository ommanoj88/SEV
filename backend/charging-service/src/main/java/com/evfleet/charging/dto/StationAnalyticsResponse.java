package com.evfleet.charging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for station-specific analytics
 * Contains utilization metrics and cost data for a charging station
 * 
 * @since PR-10 (Charging Analytics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationAnalyticsResponse {
    
    private Long stationId;
    private String stationName;
    private Integer totalSlots;
    private Integer availableSlots;
    
    // Utilization metrics
    private Long totalSessions;
    private Long completedSessions;
    private Long activeSessions;
    private BigDecimal utilizationRate; // Percentage (0-100)
    
    // Energy metrics
    private BigDecimal totalEnergyCharged; // kWh
    private BigDecimal averageEnergyPerSession; // kWh
    
    // Cost metrics
    private BigDecimal totalRevenue; // Total cost of all sessions
    private BigDecimal averageSessionCost;
    private BigDecimal pricePerKwh;
    
    // Time metrics
    private Long averageSessionDurationMinutes;
    private Long totalChargingMinutes;
}
