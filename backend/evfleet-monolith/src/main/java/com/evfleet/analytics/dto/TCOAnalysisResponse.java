package com.evfleet.analytics.dto;

import com.evfleet.fleet.model.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Total Cost of Ownership Analysis Response
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TCOAnalysisResponse {

    private Long vehicleId;
    private String vehicleName;
    private String vehicleNumber;
    private FuelType fuelType;

    // Acquisition costs
    private BigDecimal purchasePrice;
    private BigDecimal depreciation;
    private Integer ageMonths;

    // Operating costs (lifetime)
    private BigDecimal energyCosts;  // Fuel or electricity
    private BigDecimal maintenanceCosts;
    private BigDecimal insuranceCosts;
    private BigDecimal taxesFees;
    private BigDecimal otherCosts;

    // Totals and metrics
    private BigDecimal totalCost;
    private Double costPerKm;
    private Double costPerYear;
    private Double totalDistance;

    // ICE comparison (for EVs)
    private ICEComparison comparisonWithICE;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ICEComparison {
        private BigDecimal fuelSavings;
        private BigDecimal maintenanceSavings;
        private BigDecimal totalSavings;
        private Integer paybackPeriodMonths;
        private Double savingsPercentage;
    }
}
