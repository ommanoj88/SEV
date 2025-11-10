package com.evfleet.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MaintenanceCostBreakdownDTO
 * Detailed breakdown of maintenance costs by vehicle.
 * 
 * @since 2.0.0 (PR 12: Maintenance Cost Tracking)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceCostBreakdownDTO {
    
    /**
     * Vehicle ID
     */
    private String vehicleId;
    
    /**
     * Fuel type of the vehicle (ICE, EV, HYBRID)
     */
    private String fuelType;
    
    /**
     * Total maintenance cost for this vehicle
     */
    private BigDecimal totalCost;
    
    /**
     * Number of maintenance records for this vehicle
     */
    private Integer recordCount;
    
    /**
     * Average cost per maintenance service
     */
    private BigDecimal avgCostPerService;
    
    /**
     * Most expensive maintenance type for this vehicle
     */
    private String mostExpensiveMaintenanceType;
    
    /**
     * Cost of the most expensive maintenance
     */
    private BigDecimal mostExpensiveCost;
    
    /**
     * List of individual cost records
     */
    private List<CostRecord> costRecords;
    
    /**
     * Individual cost record
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CostRecord {
        private String serviceHistoryId;
        private LocalDate serviceDate;
        private String serviceType;
        private BigDecimal cost;
        private String serviceCenter;
        private String description;
    }
}
