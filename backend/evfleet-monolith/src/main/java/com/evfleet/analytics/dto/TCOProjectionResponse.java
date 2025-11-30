package com.evfleet.analytics.dto;

import com.evfleet.fleet.model.FuelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * TCO Projection Response DTO
 * 
 * Provides projected Total Cost of Ownership data for vehicles,
 * supporting multi-fuel comparison and 5-year forecasting.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TCOProjectionResponse {

    /**
     * Vehicle ID (null for hypothetical comparisons)
     */
    private Long vehicleId;

    /**
     * Fuel type being projected
     */
    private FuelType fuelType;

    /**
     * Number of years in the projection
     */
    private Integer projectionYears;

    /**
     * Total projected cost over the period
     */
    private BigDecimal totalProjectedCost;

    /**
     * Projected energy/fuel costs
     */
    private BigDecimal projectedEnergyCost;

    /**
     * Projected maintenance costs
     */
    private BigDecimal projectedMaintenanceCost;

    /**
     * Projected carbon costs (ESG)
     */
    private BigDecimal projectedCarbonCost;

    /**
     * Projected depreciation over the period
     */
    private BigDecimal projectedDepreciation;

    /**
     * Total carbon emissions in kg CO2
     */
    private BigDecimal carbonEmissionsKg;

    /**
     * Projected cost per kilometer
     */
    private BigDecimal costPerKmProjected;

    /**
     * Projected residual value at end of period
     */
    private BigDecimal projectedResidualValue;

    /**
     * Break-even distance vs ICE (km)
     */
    private BigDecimal breakEvenDistanceKm;

    /**
     * Monthly cost estimate
     */
    private BigDecimal monthlyAvgCost;

    /**
     * Calculate monthly average cost
     */
    public BigDecimal getMonthlyAvgCost() {
        if (monthlyAvgCost != null) {
            return monthlyAvgCost;
        }
        if (totalProjectedCost != null && projectionYears != null && projectionYears > 0) {
            return totalProjectedCost.divide(
                    BigDecimal.valueOf(projectionYears * 12L), 
                    2, 
                    java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
