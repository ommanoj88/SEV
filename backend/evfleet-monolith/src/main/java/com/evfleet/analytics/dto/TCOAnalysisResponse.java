package com.evfleet.analytics.dto;

import com.evfleet.fleet.model.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Total Cost of Ownership Analysis Response
 * 
 * Enhanced with:
 * - Multi-fuel support fields
 * - Carbon emissions and costs
 * - 5-year projections
 * - Current vehicle value
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TCOAnalysisResponse {

    private Long id;
    private Long vehicleId;
    private String vehicleName;
    private String vehicleNumber;
    private FuelType fuelType;
    private LocalDate analysisDate;

    // Acquisition costs
    private BigDecimal purchasePrice;
    private BigDecimal depreciation;
    private BigDecimal currentValue;
    private Integer ageMonths;

    // Operating costs (lifetime)
    private BigDecimal energyCosts;  // Fuel or electricity
    private BigDecimal maintenanceCosts;
    private BigDecimal insuranceCosts;
    private BigDecimal taxesFees;
    private BigDecimal otherCosts;

    // Carbon/ESG costs
    private BigDecimal carbonEmissionsKg;
    private BigDecimal carbonCost;

    // Totals and metrics
    private BigDecimal totalCost;
    private BigDecimal costPerKm;
    private BigDecimal costPerYear;
    private Integer analysisPeriodYears;
    private BigDecimal totalDistanceKm;

    // 5-year projections
    private BigDecimal projected5YrTotalCost;
    private BigDecimal projected5YrEnergyCost;
    private BigDecimal projected5YrMaintenanceCost;
    private BigDecimal projected5YrCarbonCost;

    // ICE comparison (for EVs/non-ICE)
    private ComparisonWithICE comparisonWithICE;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonWithICE {
        private BigDecimal fuelSavings;
        private BigDecimal maintenanceSavings;
        private BigDecimal totalSavings;
        private Integer paybackPeriodMonths;
        private Double savingsPercentage;
    }
}
