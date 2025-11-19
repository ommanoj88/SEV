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
 * Energy Consumption Analytics Entity
 * 
 * Stores daily energy consumption analytics for electric vehicles.
 * Tracks energy usage, efficiency metrics, costs, and environmental impact.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "energy_consumption_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyConsumptionAnalytics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    // Energy metrics
    @Column(name = "total_energy_consumed", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEnergyConsumed = BigDecimal.ZERO;  // kWh

    @Column(name = "total_distance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalDistance = BigDecimal.ZERO;  // km

    @Column(name = "total_charging_sessions")
    @Builder.Default
    private Integer totalChargingSessions = 0;

    // Efficiency metrics
    @Column(name = "average_efficiency", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal averageEfficiency = BigDecimal.ZERO;  // kWh per 100km

    @Column(name = "best_efficiency", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal bestEfficiency = BigDecimal.ZERO;

    @Column(name = "worst_efficiency", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal worstEfficiency = BigDecimal.ZERO;

    // Cost metrics
    @Column(name = "total_charging_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalChargingCost = BigDecimal.ZERO;

    @Column(name = "average_cost_per_kwh", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal averageCostPerKwh = BigDecimal.ZERO;

    @Column(name = "cost_per_km", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal costPerKm = BigDecimal.ZERO;

    // Advanced metrics
    @Column(name = "regenerative_energy", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal regenerativeEnergy = BigDecimal.ZERO;  // kWh recovered

    @Column(name = "regen_percentage", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal regenPercentage = BigDecimal.ZERO;  // %

    @Column(name = "idle_energy_loss", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal idleEnergyLoss = BigDecimal.ZERO;  // kWh

    // Environmental metrics
    @Column(name = "co2_saved", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal co2Saved = BigDecimal.ZERO;  // kg CO2

    /**
     * Calculate average efficiency (kWh per 100km)
     */
    public void calculateAverageEfficiency() {
        if (this.totalDistance != null && this.totalDistance.compareTo(BigDecimal.ZERO) > 0) {
            this.averageEfficiency = this.totalEnergyConsumed
                    .multiply(BigDecimal.valueOf(100))
                    .divide(this.totalDistance, 4, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageEfficiency = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate average cost per kWh
     */
    public void calculateAverageCostPerKwh() {
        if (this.totalEnergyConsumed != null && this.totalEnergyConsumed.compareTo(BigDecimal.ZERO) > 0) {
            this.averageCostPerKwh = this.totalChargingCost.divide(
                    this.totalEnergyConsumed, 4, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageCostPerKwh = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate cost per kilometer
     */
    public void calculateCostPerKm() {
        if (this.totalDistance != null && this.totalDistance.compareTo(BigDecimal.ZERO) > 0) {
            this.costPerKm = this.totalChargingCost.divide(
                    this.totalDistance, 4, BigDecimal.ROUND_HALF_UP);
        } else {
            this.costPerKm = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate regenerative energy percentage
     */
    public void calculateRegenPercentage() {
        if (this.totalEnergyConsumed != null && this.totalEnergyConsumed.compareTo(BigDecimal.ZERO) > 0) {
            this.regenPercentage = this.regenerativeEnergy
                    .multiply(BigDecimal.valueOf(100))
                    .divide(this.totalEnergyConsumed, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.regenPercentage = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate CO2 savings vs equivalent ICE vehicle
     * Using: 0.12 kg CO2/km for ICE, 0.05 kg CO2/km for EV (grid-based)
     */
    public void calculateCo2Savings() {
        if (this.totalDistance != null) {
            BigDecimal iceCo2 = this.totalDistance.multiply(BigDecimal.valueOf(0.12));
            BigDecimal evCo2 = this.totalDistance.multiply(BigDecimal.valueOf(0.05));
            this.co2Saved = iceCo2.subtract(evCo2);
        } else {
            this.co2Saved = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate all derived metrics
     */
    public void calculateAllMetrics() {
        calculateAverageEfficiency();
        calculateAverageCostPerKwh();
        calculateCostPerKm();
        calculateRegenPercentage();
        calculateCo2Savings();
    }
}
