package com.evfleet.analytics.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Historical Metric Entity
 * 
 * Stores aggregated metrics for historical analysis and trend calculations.
 * Supports daily, weekly, monthly, and yearly aggregation periods.
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "historical_metrics", indexes = {
    @Index(name = "idx_hist_company_period", columnList = "company_id, period_type, period_start"),
    @Index(name = "idx_hist_metric_type", columnList = "metric_type, company_id"),
    @Index(name = "idx_hist_period_start", columnList = "period_start"),
    @Index(name = "idx_hist_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "previous_value", precision = 15, scale = 4)
    private BigDecimal previousValue;

    @Column(name = "change_percent", precision = 8, scale = 2)
    private BigDecimal changePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_direction", length = 10)
    private TrendDirection trendDirection;

    @Column(name = "sample_count")
    private Integer sampleCount;

    @Column(name = "min_value", precision = 15, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 15, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "avg_value", precision = 15, scale = 4)
    private BigDecimal avgValue;

    @Column(name = "std_deviation", precision = 15, scale = 4)
    private BigDecimal stdDeviation;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Period types for aggregation
     */
    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    /**
     * Types of metrics that can be tracked
     */
    public enum MetricType {
        // Fleet metrics
        TOTAL_VEHICLES,
        ACTIVE_VEHICLES,
        VEHICLE_UTILIZATION_RATE,
        
        // Trip metrics
        TOTAL_TRIPS,
        TOTAL_DISTANCE_KM,
        AVG_TRIP_DISTANCE_KM,
        AVG_TRIP_DURATION_MINUTES,
        
        // Energy metrics
        TOTAL_ENERGY_KWH,
        AVG_ENERGY_PER_KM,
        CHARGING_SESSIONS_COUNT,
        AVG_CHARGING_DURATION_MINUTES,
        
        // Cost metrics
        TOTAL_COST,
        FUEL_COST,
        ENERGY_COST,
        MAINTENANCE_COST,
        COST_PER_KM,
        COST_PER_VEHICLE,
        
        // Maintenance metrics
        MAINTENANCE_EVENTS_COUNT,
        PREVENTIVE_MAINTENANCE_COUNT,
        CORRECTIVE_MAINTENANCE_COUNT,
        AVG_MAINTENANCE_COST,
        
        // Driver metrics
        ACTIVE_DRIVERS,
        DRIVER_SAFETY_SCORE_AVG,
        DRIVING_EVENTS_COUNT,
        
        // Environmental metrics
        CO2_EMISSIONS_KG,
        CO2_SAVED_KG,
        GREEN_DISTANCE_KM,
        
        // Efficiency metrics
        FLEET_EFFICIENCY_SCORE,
        DOWNTIME_HOURS,
        AVAILABILITY_RATE
    }

    /**
     * Trend direction indicators
     */
    public enum TrendDirection {
        UP,
        DOWN,
        STABLE
    }

    /**
     * Calculate trend direction based on change percent
     */
    public void calculateTrend() {
        if (changePercent == null) {
            this.trendDirection = TrendDirection.STABLE;
        } else if (changePercent.compareTo(BigDecimal.valueOf(1)) > 0) {
            this.trendDirection = TrendDirection.UP;
        } else if (changePercent.compareTo(BigDecimal.valueOf(-1)) < 0) {
            this.trendDirection = TrendDirection.DOWN;
        } else {
            this.trendDirection = TrendDirection.STABLE;
        }
    }

    /**
     * Get display label for the period
     */
    public String getPeriodLabel() {
        return switch (periodType) {
            case DAILY -> periodStart.toString();
            case WEEKLY -> "Week " + periodStart.toString();
            case MONTHLY -> YearMonth.from(periodStart).toString();
            case QUARTERLY -> "Q" + ((periodStart.getMonthValue() - 1) / 3 + 1) + " " + periodStart.getYear();
            case YEARLY -> String.valueOf(periodStart.getYear());
        };
    }
}
