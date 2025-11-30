package com.evfleet.analytics.model;

import com.evfleet.common.entity.BaseEntity;
import com.evfleet.fleet.model.FuelType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * TCO Analysis Entity
 * 
 * Stores Total Cost of Ownership analysis data for vehicles.
 * Tracks acquisition costs, operating costs, and comparison with equivalent ICE vehicles.
 * 
 * Enhanced with multi-fuel support (EV, ICE, HYBRID, CNG, LPG),
 * carbon cost tracking, regional adjustments, and 5-year projections.
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Entity
@Table(name = "tco_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TCOAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    // Vehicle fuel type for reference
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    // Vehicle costs
    @Column(name = "purchase_price", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "depreciation_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depreciationValue = BigDecimal.ZERO;

    @Column(name = "current_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal currentValue = BigDecimal.ZERO;

    // Operating costs
    @Column(name = "energy_costs", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal energyCosts = BigDecimal.ZERO;

    @Column(name = "maintenance_costs", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal maintenanceCosts = BigDecimal.ZERO;

    @Column(name = "insurance_costs", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal insuranceCosts = BigDecimal.ZERO;

    @Column(name = "taxes_fees", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxesFees = BigDecimal.ZERO;

    @Column(name = "other_costs", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal otherCosts = BigDecimal.ZERO;

    // Carbon costs and emissions (ESG)
    @Column(name = "carbon_emissions_kg", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal carbonEmissionsKg = BigDecimal.ZERO;

    @Column(name = "carbon_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal carbonCost = BigDecimal.ZERO;

    // Regional adjustment
    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "regional_adjustment_factor", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal regionalAdjustmentFactor = BigDecimal.ONE;

    // Calculated totals
    @Column(name = "total_cost", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "cost_per_km", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal costPerKm = BigDecimal.ZERO;

    @Column(name = "cost_per_year", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costPerYear = BigDecimal.ZERO;

    // ICE comparison
    @Column(name = "ice_fuel_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal iceFuelSavings = BigDecimal.ZERO;

    @Column(name = "ice_maintenance_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal iceMaintenanceSavings = BigDecimal.ZERO;

    @Column(name = "ice_total_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal iceTotalSavings = BigDecimal.ZERO;

    @Column(name = "ice_payback_period_months")
    @Builder.Default
    private Integer icePaybackPeriodMonths = 0;

    // Multi-fuel comparison fields
    @Column(name = "comparison_fuel_type")
    @Enumerated(EnumType.STRING)
    private FuelType comparisonFuelType;

    @Column(name = "comparison_fuel_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comparisonFuelSavings = BigDecimal.ZERO;

    @Column(name = "comparison_maintenance_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comparisonMaintenanceSavings = BigDecimal.ZERO;

    @Column(name = "comparison_carbon_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comparisonCarbonSavings = BigDecimal.ZERO;

    @Column(name = "comparison_total_savings", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comparisonTotalSavings = BigDecimal.ZERO;

    @Column(name = "comparison_payback_months")
    @Builder.Default
    private Integer comparisonPaybackMonths = 0;

    // 5-year projection
    @Column(name = "projected_5yr_total_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal projected5YrTotalCost = BigDecimal.ZERO;

    @Column(name = "projected_5yr_energy_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal projected5YrEnergyCost = BigDecimal.ZERO;

    @Column(name = "projected_5yr_maintenance_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal projected5YrMaintenanceCost = BigDecimal.ZERO;

    @Column(name = "projected_5yr_carbon_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal projected5YrCarbonCost = BigDecimal.ZERO;

    // Metadata
    @Column(name = "analysis_period_years")
    @Builder.Default
    private Integer analysisPeriodYears = 5;

    @Column(name = "total_distance_km", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Calculate total cost from all cost components
     */
    public void calculateTotalCost() {
        this.totalCost = this.purchasePrice
                .subtract(this.depreciationValue)
                .add(this.energyCosts)
                .add(this.maintenanceCosts)
                .add(this.insuranceCosts)
                .add(this.taxesFees)
                .add(this.otherCosts)
                .add(this.carbonCost != null ? this.carbonCost : BigDecimal.ZERO);
    }

    /**
     * Calculate total cost including carbon costs
     */
    public void calculateTotalCostWithCarbon() {
        calculateTotalCost();
        if (this.carbonCost != null) {
            this.totalCost = this.totalCost.add(this.carbonCost);
        }
    }

    /**
     * Calculate cost per kilometer
     */
    public void calculateCostPerKm() {
        if (this.totalDistanceKm != null && this.totalDistanceKm.compareTo(BigDecimal.ZERO) > 0) {
            this.costPerKm = this.totalCost.divide(this.totalDistanceKm, 4, RoundingMode.HALF_UP);
        } else {
            this.costPerKm = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate cost per year
     */
    public void calculateCostPerYear() {
        if (this.analysisPeriodYears != null && this.analysisPeriodYears > 0) {
            this.costPerYear = this.totalCost.divide(
                    BigDecimal.valueOf(this.analysisPeriodYears), 2, RoundingMode.HALF_UP);
        } else {
            this.costPerYear = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate total ICE savings (legacy)
     */
    public void calculateIceTotalSavings() {
        this.iceTotalSavings = (this.iceFuelSavings != null ? this.iceFuelSavings : BigDecimal.ZERO)
                .add(this.iceMaintenanceSavings != null ? this.iceMaintenanceSavings : BigDecimal.ZERO);
    }

    /**
     * Calculate total comparison savings (multi-fuel)
     */
    public void calculateComparisonTotalSavings() {
        this.comparisonTotalSavings = (this.comparisonFuelSavings != null ? this.comparisonFuelSavings : BigDecimal.ZERO)
                .add(this.comparisonMaintenanceSavings != null ? this.comparisonMaintenanceSavings : BigDecimal.ZERO)
                .add(this.comparisonCarbonSavings != null ? this.comparisonCarbonSavings : BigDecimal.ZERO);
    }

    /**
     * Calculate current vehicle value after depreciation
     */
    public void calculateCurrentValue() {
        this.currentValue = this.purchasePrice.subtract(this.depreciationValue != null ? this.depreciationValue : BigDecimal.ZERO);
        if (this.currentValue.compareTo(BigDecimal.ZERO) < 0) {
            this.currentValue = BigDecimal.ZERO;
        }
    }

    /**
     * Apply regional adjustment factor to costs
     */
    public void applyRegionalAdjustment() {
        if (this.regionalAdjustmentFactor != null && this.regionalAdjustmentFactor.compareTo(BigDecimal.ONE) != 0) {
            this.energyCosts = this.energyCosts.multiply(this.regionalAdjustmentFactor).setScale(2, RoundingMode.HALF_UP);
            this.maintenanceCosts = this.maintenanceCosts.multiply(this.regionalAdjustmentFactor).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
