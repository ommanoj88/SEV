package com.evfleet.charging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for cost summary analytics
 * Contains revenue and cost metrics for charging operations
 * 
 * @since PR-10 (Charging Analytics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostSummaryResponse {
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Revenue metrics
    private BigDecimal totalRevenue;
    private BigDecimal averageSessionCost;
    private BigDecimal totalEnergyCharged; // kWh
    
    // Cost breakdown
    private BigDecimal minSessionCost;
    private BigDecimal maxSessionCost;
    private BigDecimal medianSessionCost;
    
    // Energy metrics
    private BigDecimal averageEnergyPerSession; // kWh
    private BigDecimal totalSessions;
    private BigDecimal completedSessions;
    
    // Pricing
    private BigDecimal averagePricePerKwh;
    private BigDecimal revenuePerKwh;
}
