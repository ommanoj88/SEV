package com.evfleet.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * MaintenanceCostSummaryDTO
 * Summary of maintenance costs for Total Cost of Ownership (TCO) calculations.
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceCostSummaryDTO {
    
    /**
     * Total maintenance cost across all vehicles
     */
    private BigDecimal totalCost;
    
    /**
     * Total cost for EV vehicles
     */
    private BigDecimal evCost;
    
    /**
     * Total cost for ICE vehicles
     */
    private BigDecimal iceCost;
    
    /**
     * Total cost for HYBRID vehicles
     */
    private BigDecimal hybridCost;
    
    /**
     * Number of EV vehicles in the calculation
     */
    private Integer evVehicleCount;
    
    /**
     * Number of ICE vehicles in the calculation
     */
    private Integer iceVehicleCount;
    
    /**
     * Number of HYBRID vehicles in the calculation
     */
    private Integer hybridVehicleCount;
    
    /**
     * Average cost per EV vehicle
     */
    private BigDecimal avgCostPerEV;
    
    /**
     * Average cost per ICE vehicle
     */
    private BigDecimal avgCostPerICE;
    
    /**
     * Average cost per HYBRID vehicle
     */
    private BigDecimal avgCostPerHybrid;
    
    /**
     * Cost breakdown by maintenance type (e.g., OIL_CHANGE -> $500, BATTERY_CHECK -> $300)
     */
    private Map<String, BigDecimal> costByMaintenanceType;
    
    /**
     * Cost breakdown by fuel type category (ICE-specific, EV-specific, COMMON)
     */
    private Map<String, BigDecimal> costByCategory;
    
    /**
     * Start date of the period for this summary
     */
    private LocalDate periodStart;
    
    /**
     * End date of the period for this summary
     */
    private LocalDate periodEnd;
    
    /**
     * Number of maintenance records included in this summary
     */
    private Integer recordCount;
}
