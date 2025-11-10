package com.evfleet.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * VehicleCostComparisonDTO
 * Comparison of maintenance costs between fuel types.
 * Useful for TCO (Total Cost of Ownership) analysis.
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCostComparisonDTO {
    
    /**
     * Average monthly maintenance cost for EV vehicles
     */
    private BigDecimal evAvgMonthlyCost;
    
    /**
     * Average monthly maintenance cost for ICE vehicles
     */
    private BigDecimal iceAvgMonthlyCost;
    
    /**
     * Average monthly maintenance cost for HYBRID vehicles
     */
    private BigDecimal hybridAvgMonthlyCost;
    
    /**
     * Cost savings of EV vs ICE (positive means EV is cheaper)
     */
    private BigDecimal evVsIceSavings;
    
    /**
     * Percentage savings of EV vs ICE
     */
    private BigDecimal evVsIceSavingsPercentage;
    
    /**
     * Cost savings of HYBRID vs ICE
     */
    private BigDecimal hybridVsIceSavings;
    
    /**
     * Percentage savings of HYBRID vs ICE
     */
    private BigDecimal hybridVsIceSavingsPercentage;
    
    /**
     * Total number of EV vehicles in comparison
     */
    private Integer evVehicleCount;
    
    /**
     * Total number of ICE vehicles in comparison
     */
    private Integer iceVehicleCount;
    
    /**
     * Total number of HYBRID vehicles in comparison
     */
    private Integer hybridVehicleCount;
    
    /**
     * Number of months included in the analysis
     */
    private Integer monthsAnalyzed;
}
