package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cost Analytics Response
 * Contains cost breakdown and metrics for a period
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostAnalyticsResponse {

    private String period;  // e.g., "November 2025"

    // Cost breakdown
    private BigDecimal energyCost;
    private BigDecimal maintenanceCost;
    private BigDecimal insuranceCost;
    private BigDecimal otherCosts;
    private BigDecimal totalCost;

    // Metrics
    private Double costPerKm;
    private Double costPerVehicle;

    // Additional data
    private Integer vehicleCount;
    private Double totalDistance;
}
