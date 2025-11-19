package com.evfleet.analytics.dto;

import com.evfleet.analytics.model.TCOAnalysis;
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
 * @author SEV Platform Team
 * @version 1.0.0
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
    private Integer ageMonths;

    // Operating costs (lifetime)
    private BigDecimal energyCosts;  // Fuel or electricity
    private BigDecimal maintenanceCosts;
    private BigDecimal insuranceCosts;
    private BigDecimal taxesFees;
    private BigDecimal otherCosts;
    private BigDecimal totalCost;
    
    // Metrics
    private BigDecimal costPerKm;
    private BigDecimal costPerYear;
    private Integer analysisPeriodYears;
    private BigDecimal totalDistanceKm;
    
    // ICE Comparison
    private ComparisonWithICE comparisonWithICE;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonWithICE {
        private BigDecimal fuelSavings;
        private BigDecimal maintenanceSavings;
        private BigDecimal totalSavings;
        private Integer paybackPeriod;  // in months
        private Double savingsPercentage;
    }

    public static TCOAnalysisResponse fromEntity(TCOAnalysis tco) {
        if (tco == null) {
            return null;
        }

        ComparisonWithICE comparison = null;
        if (tco.getIceTotalSavings() != null && tco.getIceTotalSavings().compareTo(BigDecimal.ZERO) > 0) {
            comparison = ComparisonWithICE.builder()
                    .fuelSavings(tco.getIceFuelSavings())
                    .maintenanceSavings(tco.getIceMaintenanceSavings())
                    .totalSavings(tco.getIceTotalSavings())
                    .paybackPeriod(tco.getIcePaybackPeriodMonths())
                    .build();
        }

        return TCOAnalysisResponse.builder()
                .id(tco.getId())
                .vehicleId(tco.getVehicleId())
                .analysisDate(tco.getAnalysisDate())
                .purchasePrice(tco.getPurchasePrice())
                .depreciation(tco.getDepreciationValue())
                .energyCosts(tco.getEnergyCosts())
                .maintenanceCosts(tco.getMaintenanceCosts())
                .insuranceCosts(tco.getInsuranceCosts())
                .taxesFees(tco.getTaxesFees())
                .otherCosts(tco.getOtherCosts())
                .totalCost(tco.getTotalCost())
                .costPerKm(tco.getCostPerKm())
                .costPerYear(tco.getCostPerYear())
                .analysisPeriodYears(tco.getAnalysisPeriodYears())
                .totalDistanceKm(tco.getTotalDistanceKm())
                .comparisonWithICE(comparison)
                .build();
    }
}
