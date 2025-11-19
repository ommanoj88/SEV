package com.evfleet.analytics.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TCO Analysis Entity
 * 
 * Stores Total Cost of Ownership analysis data for vehicles.
 * Tracks acquisition costs, operating costs, and comparison with equivalent ICE vehicles.
 *
 * @author SEV Platform Team
 * @version 1.0.0
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

    // Vehicle costs
    @Column(name = "purchase_price", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "depreciation_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depreciationValue = BigDecimal.ZERO;

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
                .add(this.otherCosts);
    }

    /**
     * Calculate cost per kilometer
     */
    public void calculateCostPerKm() {
        if (this.totalDistanceKm != null && this.totalDistanceKm.compareTo(BigDecimal.ZERO) > 0) {
            this.costPerKm = this.totalCost.divide(this.totalDistanceKm, 4, BigDecimal.ROUND_HALF_UP);
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
                    BigDecimal.valueOf(this.analysisPeriodYears), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.costPerYear = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate total ICE savings
     */
    public void calculateIceTotalSavings() {
        this.iceTotalSavings = this.iceFuelSavings.add(this.iceMaintenanceSavings);
    }
}
