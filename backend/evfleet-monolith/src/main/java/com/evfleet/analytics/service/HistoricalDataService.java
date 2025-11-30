package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.*;
import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.model.HistoricalMetric;
import com.evfleet.analytics.model.HistoricalMetric.MetricType;
import com.evfleet.analytics.model.HistoricalMetric.PeriodType;
import com.evfleet.analytics.model.HistoricalMetric.TrendDirection;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.analytics.repository.HistoricalMetricRepository;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Historical Data Service
 * 
 * Provides historical data aggregation, trend analysis, and data retention.
 * Aggregates daily, monthly, quarterly, and yearly metrics for analytics.
 * 
 * Features:
 * - Daily metrics aggregation from fleet summaries
 * - Monthly rollup from daily data
 * - Trend calculation with percentage changes
 * - Data retention policy (5 years default)
 * - Scheduled background aggregation
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class HistoricalDataService {

    private final HistoricalMetricRepository historicalMetricRepository;
    private final FleetSummaryRepository fleetSummaryRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final MeterRegistry meterRegistry;

    @Value("${analytics.retention.years:5}")
    private int retentionYears;

    @Value("${analytics.daily-retention.days:365}")
    private int dailyRetentionDays;

    private Counter aggregationsCounter;
    private Counter trendsCalculatedCounter;
    private Timer aggregationTimer;

    @PostConstruct
    public void initMetrics() {
        aggregationsCounter = Counter.builder("analytics.historical.aggregations")
                .description("Total historical data aggregations performed")
                .register(meterRegistry);
        
        trendsCalculatedCounter = Counter.builder("analytics.historical.trends_calculated")
                .description("Total trend calculations performed")
                .register(meterRegistry);
        
        aggregationTimer = Timer.builder("analytics.historical.aggregation_duration")
                .description("Duration of aggregation operations")
                .register(meterRegistry);
    }

    // ==================== Daily Aggregation ====================

    /**
     * Aggregate daily metrics for a specific date
     */
    public List<HistoricalMetric> aggregateDailyMetrics(Long companyId, LocalDate date) {
        log.info("Aggregating daily metrics for company {} on {}", companyId, date);
        
        return aggregationTimer.record(() -> {
            List<HistoricalMetric> metrics = new ArrayList<>();
            
            // Get fleet summary for the date
            Optional<FleetSummary> summaryOpt = fleetSummaryRepository
                    .findByCompanyIdAndSummaryDate(companyId, date);
            
            if (summaryOpt.isEmpty()) {
                log.debug("No fleet summary found for company {} on {}", companyId, date);
                return metrics;
            }
            
            FleetSummary summary = summaryOpt.get();
            LocalDate previousDate = date.minusDays(1);
            
            // Aggregate each metric type
            metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                    MetricType.TOTAL_VEHICLES, BigDecimal.valueOf(summary.getTotalVehicles()),
                    previousDate));
            
            metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                    MetricType.ACTIVE_VEHICLES, BigDecimal.valueOf(summary.getActiveVehicles()),
                    previousDate));
            
            metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                    MetricType.TOTAL_TRIPS, BigDecimal.valueOf(summary.getTotalTrips()),
                    previousDate));
            
            metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                    MetricType.TOTAL_DISTANCE_KM, BigDecimal.valueOf(summary.getTotalDistance()),
                    previousDate));
            
            if (summary.getTotalEnergyConsumed() != null) {
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.TOTAL_ENERGY_KWH, summary.getTotalEnergyConsumed(),
                        previousDate));
            }
            
            if (summary.getTotalCost() != null) {
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.TOTAL_COST, summary.getTotalCost(),
                        previousDate));
            }
            
            if (summary.getMaintenanceCost() != null) {
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.MAINTENANCE_COST, summary.getMaintenanceCost(),
                        previousDate));
            }
            
            if (summary.getEnergyCost() != null) {
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.ENERGY_COST, summary.getEnergyCost(),
                        previousDate));
            }
            
            // Calculate utilization rate
            if (summary.getTotalVehicles() > 0) {
                BigDecimal utilizationRate = BigDecimal.valueOf(summary.getActiveVehicles())
                        .divide(BigDecimal.valueOf(summary.getTotalVehicles()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.VEHICLE_UTILIZATION_RATE, utilizationRate,
                        previousDate));
            }
            
            // Calculate cost per km
            if (summary.getTotalDistance() != null && summary.getTotalDistance() > 0 
                    && summary.getTotalCost() != null) {
                BigDecimal costPerKm = summary.getTotalCost()
                        .divide(BigDecimal.valueOf(summary.getTotalDistance()), 4, RoundingMode.HALF_UP);
                metrics.add(createMetric(companyId, PeriodType.DAILY, date, date,
                        MetricType.COST_PER_KM, costPerKm,
                        previousDate));
            }
            
            // Save all metrics
            List<HistoricalMetric> savedMetrics = new ArrayList<>();
            for (HistoricalMetric metric : metrics) {
                savedMetrics.add(saveOrUpdateMetric(metric));
            }
            
            aggregationsCounter.increment();
            log.info("Aggregated {} daily metrics for company {} on {}", savedMetrics.size(), companyId, date);
            
            return savedMetrics;
        });
    }

    // ==================== Monthly Aggregation ====================

    /**
     * Aggregate monthly metrics from daily data
     */
    public List<HistoricalMetric> aggregateMonthlyMetrics(Long companyId, YearMonth yearMonth) {
        log.info("Aggregating monthly metrics for company {} for {}", companyId, yearMonth);
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDate previousMonthStart = yearMonth.minusMonths(1).atDay(1);
        
        List<HistoricalMetric> metrics = new ArrayList<>();
        
        // Get all daily metrics for the month
        List<HistoricalMetric> dailyMetrics = historicalMetricRepository
                .findDailyMetrics(companyId, startDate, endDate);
        
        if (dailyMetrics.isEmpty()) {
            log.debug("No daily metrics found for company {} in {}", companyId, yearMonth);
            return metrics;
        }
        
        // Group by metric type
        Map<MetricType, List<HistoricalMetric>> metricsByType = dailyMetrics.stream()
                .collect(Collectors.groupingBy(HistoricalMetric::getMetricType));
        
        for (Map.Entry<MetricType, List<HistoricalMetric>> entry : metricsByType.entrySet()) {
            MetricType metricType = entry.getKey();
            List<HistoricalMetric> dailyValues = entry.getValue();
            
            // Calculate aggregated values
            BigDecimal sum = dailyValues.stream()
                    .map(HistoricalMetric::getMetricValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avg = sum.divide(BigDecimal.valueOf(dailyValues.size()), 4, RoundingMode.HALF_UP);
            
            BigDecimal min = dailyValues.stream()
                    .map(HistoricalMetric::getMetricValue)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal max = dailyValues.stream()
                    .map(HistoricalMetric::getMetricValue)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            // Use sum for cumulative metrics, avg for rates
            BigDecimal value = isCumulativeMetric(metricType) ? sum : avg;
            
            HistoricalMetric metric = HistoricalMetric.builder()
                    .companyId(companyId)
                    .periodType(PeriodType.MONTHLY)
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .metricType(metricType)
                    .metricValue(value)
                    .sampleCount(dailyValues.size())
                    .minValue(min)
                    .maxValue(max)
                    .avgValue(avg)
                    .build();
            
            // Calculate trend vs previous month
            calculateTrend(metric, companyId, metricType, PeriodType.MONTHLY, previousMonthStart);
            
            metrics.add(saveOrUpdateMetric(metric));
        }
        
        aggregationsCounter.increment();
        log.info("Aggregated {} monthly metrics for company {} for {}", metrics.size(), companyId, yearMonth);
        
        return metrics;
    }

    // ==================== Trend Calculation ====================

    /**
     * Calculate trends for a specific metric over a period
     */
    @Transactional(readOnly = true)
    public TrendAnalysisResponse calculateTrends(Long companyId, MetricType metricType, 
                                                  PeriodType periodType, int periodsBack) {
        log.info("Calculating trends for company {}, metric {}, period {}, {} periods",
                companyId, metricType, periodType, periodsBack);
        
        trendsCalculatedCounter.increment();
        
        List<HistoricalMetric> metrics = historicalMetricRepository
                .findRecentMetrics(companyId, metricType, periodType);
        
        if (metrics.isEmpty()) {
            return TrendAnalysisResponse.empty(metricType, periodType);
        }
        
        // Limit to requested periods
        List<HistoricalMetric> limitedMetrics = metrics.stream()
                .limit(periodsBack)
                .collect(Collectors.toList());
        
        // Reverse to get chronological order
        Collections.reverse(limitedMetrics);
        
        // Calculate statistics
        List<BigDecimal> values = limitedMetrics.stream()
                .map(HistoricalMetric::getMetricValue)
                .toList();
        
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        // Calculate overall trend
        BigDecimal firstValue = values.get(0);
        BigDecimal lastValue = values.get(values.size() - 1);
        BigDecimal overallChange = BigDecimal.ZERO;
        TrendDirection overallDirection = TrendDirection.STABLE;
        
        if (firstValue.compareTo(BigDecimal.ZERO) != 0) {
            overallChange = lastValue.subtract(firstValue)
                    .divide(firstValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (overallChange.compareTo(BigDecimal.valueOf(1)) > 0) {
                overallDirection = TrendDirection.UP;
            } else if (overallChange.compareTo(BigDecimal.valueOf(-1)) < 0) {
                overallDirection = TrendDirection.DOWN;
            }
        }
        
        // Build data points
        List<TrendDataPoint> dataPoints = limitedMetrics.stream()
                .map(m -> TrendDataPoint.builder()
                        .periodLabel(m.getPeriodLabel())
                        .value(m.getMetricValue())
                        .changePercent(m.getChangePercent())
                        .trendDirection(m.getTrendDirection())
                        .build())
                .toList();
        
        return TrendAnalysisResponse.builder()
                .metricType(metricType)
                .periodType(periodType)
                .periodsAnalyzed(limitedMetrics.size())
                .dataPoints(dataPoints)
                .averageValue(avg)
                .minValue(min)
                .maxValue(max)
                .overallChangePercent(overallChange)
                .overallTrend(overallDirection)
                .firstPeriod(limitedMetrics.get(0).getPeriodLabel())
                .lastPeriod(limitedMetrics.get(limitedMetrics.size() - 1).getPeriodLabel())
                .build();
    }

    /**
     * Get all trends summary for a company
     */
    @Transactional(readOnly = true)
    public List<TrendSummary> getTrendsSummary(Long companyId, PeriodType periodType) {
        List<HistoricalMetric> latestMetrics = historicalMetricRepository
                .findLatestMetricsOfEachType(companyId, periodType);
        
        return latestMetrics.stream()
                .map(m -> TrendSummary.builder()
                        .metricType(m.getMetricType())
                        .currentValue(m.getMetricValue())
                        .previousValue(m.getPreviousValue())
                        .changePercent(m.getChangePercent())
                        .trendDirection(m.getTrendDirection())
                        .periodLabel(m.getPeriodLabel())
                        .build())
                .toList();
    }

    // ==================== Data Retention ====================

    /**
     * Apply data retention policy
     * Runs daily at 3 AM to clean up old data
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void applyRetentionPolicy() {
        log.info("Applying data retention policy");
        
        // Delete all metrics older than retention period
        LocalDate retentionDate = LocalDate.now().minusYears(retentionYears);
        int deletedOld = historicalMetricRepository.deleteOldMetrics(retentionDate);
        log.info("Deleted {} metrics older than {}", deletedOld, retentionDate);
        
        // Delete daily metrics older than daily retention (keep monthly aggregates)
        LocalDate dailyRetentionDate = LocalDate.now().minusDays(dailyRetentionDays);
        int deletedDaily = historicalMetricRepository.deleteDailyMetricsOlderThan(dailyRetentionDate);
        log.info("Deleted {} daily metrics older than {}", deletedDaily, dailyRetentionDate);
    }

    /**
     * Get retention policy status
     */
    @Transactional(readOnly = true)
    public RetentionPolicyStatus getRetentionStatus(Long companyId) {
        List<Object[]> counts = historicalMetricRepository.countMetricsByPeriodType(companyId);
        
        Map<PeriodType, Long> countsByType = new EnumMap<>(PeriodType.class);
        for (Object[] row : counts) {
            countsByType.put((PeriodType) row[0], (Long) row[1]);
        }
        
        return RetentionPolicyStatus.builder()
                .retentionYears(retentionYears)
                .dailyRetentionDays(dailyRetentionDays)
                .dailyMetricsCount(countsByType.getOrDefault(PeriodType.DAILY, 0L))
                .monthlyMetricsCount(countsByType.getOrDefault(PeriodType.MONTHLY, 0L))
                .yearlyMetricsCount(countsByType.getOrDefault(PeriodType.YEARLY, 0L))
                .oldestDataDate(getOldestMetricDate(companyId))
                .build();
    }

    // ==================== Scheduled Aggregation ====================

    /**
     * Daily aggregation job - runs at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledDailyAggregation() {
        log.info("Starting scheduled daily aggregation");
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // Get all companies with fleet summaries
        List<Long> companyIds = fleetSummaryRepository.findDistinctCompanyIds();
        
        for (Long companyId : companyIds) {
            try {
                aggregateDailyMetrics(companyId, yesterday);
            } catch (Exception e) {
                log.error("Failed to aggregate daily metrics for company {}: {}", 
                        companyId, e.getMessage());
            }
        }
        
        log.info("Completed scheduled daily aggregation for {} companies", companyIds.size());
    }

    /**
     * Monthly aggregation job - runs on 1st of each month at 2 AM
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void scheduledMonthlyAggregation() {
        log.info("Starting scheduled monthly aggregation");
        
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        
        // Get all companies
        List<Long> companyIds = fleetSummaryRepository.findDistinctCompanyIds();
        
        for (Long companyId : companyIds) {
            try {
                aggregateMonthlyMetrics(companyId, lastMonth);
            } catch (Exception e) {
                log.error("Failed to aggregate monthly metrics for company {}: {}", 
                        companyId, e.getMessage());
            }
        }
        
        log.info("Completed scheduled monthly aggregation for {} companies", companyIds.size());
    }

    // ==================== Helper Methods ====================

    private HistoricalMetric createMetric(Long companyId, PeriodType periodType, 
                                          LocalDate periodStart, LocalDate periodEnd,
                                          MetricType metricType, BigDecimal value,
                                          LocalDate previousPeriodStart) {
        HistoricalMetric metric = HistoricalMetric.builder()
                .companyId(companyId)
                .periodType(periodType)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .metricType(metricType)
                .metricValue(value)
                .sampleCount(1)
                .build();
        
        // Calculate trend vs previous period
        calculateTrend(metric, companyId, metricType, periodType, previousPeriodStart);
        
        return metric;
    }

    private void calculateTrend(HistoricalMetric metric, Long companyId, 
                                MetricType metricType, PeriodType periodType,
                                LocalDate previousPeriodStart) {
        Optional<HistoricalMetric> previousOpt = historicalMetricRepository
                .findPreviousPeriodMetric(companyId, metricType, periodType, metric.getPeriodStart());
        
        if (previousOpt.isPresent()) {
            HistoricalMetric previous = previousOpt.get();
            metric.setPreviousValue(previous.getMetricValue());
            
            if (previous.getMetricValue().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal change = metric.getMetricValue().subtract(previous.getMetricValue())
                        .divide(previous.getMetricValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                metric.setChangePercent(change);
            }
        }
        
        metric.calculateTrend();
    }

    private HistoricalMetric saveOrUpdateMetric(HistoricalMetric metric) {
        Optional<HistoricalMetric> existingOpt = historicalMetricRepository
                .findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                        metric.getCompanyId(),
                        metric.getPeriodType(),
                        metric.getPeriodStart(),
                        metric.getMetricType());
        
        if (existingOpt.isPresent()) {
            HistoricalMetric existing = existingOpt.get();
            existing.setMetricValue(metric.getMetricValue());
            existing.setPreviousValue(metric.getPreviousValue());
            existing.setChangePercent(metric.getChangePercent());
            existing.setTrendDirection(metric.getTrendDirection());
            existing.setSampleCount(metric.getSampleCount());
            existing.setMinValue(metric.getMinValue());
            existing.setMaxValue(metric.getMaxValue());
            existing.setAvgValue(metric.getAvgValue());
            return historicalMetricRepository.save(existing);
        }
        
        return historicalMetricRepository.save(metric);
    }

    private boolean isCumulativeMetric(MetricType metricType) {
        return switch (metricType) {
            case TOTAL_TRIPS, TOTAL_DISTANCE_KM, TOTAL_ENERGY_KWH, TOTAL_COST,
                 FUEL_COST, ENERGY_COST, MAINTENANCE_COST, CHARGING_SESSIONS_COUNT,
                 MAINTENANCE_EVENTS_COUNT, DRIVING_EVENTS_COUNT, CO2_EMISSIONS_KG,
                 CO2_SAVED_KG, GREEN_DISTANCE_KM, DOWNTIME_HOURS -> true;
            default -> false;
        };
    }

    private LocalDate getOldestMetricDate(Long companyId) {
        List<HistoricalMetric> metrics = historicalMetricRepository
                .findByCompanyIdAndPeriodTypeOrderByPeriodStartDesc(companyId, PeriodType.DAILY);
        
        if (metrics.isEmpty()) {
            return null;
        }
        
        return metrics.get(metrics.size() - 1).getPeriodStart();
    }

    // ==================== DTOs ====================

    @lombok.Builder
    @lombok.Data
    public static class TrendAnalysisResponse {
        private MetricType metricType;
        private PeriodType periodType;
        private int periodsAnalyzed;
        private List<TrendDataPoint> dataPoints;
        private BigDecimal averageValue;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private BigDecimal overallChangePercent;
        private TrendDirection overallTrend;
        private String firstPeriod;
        private String lastPeriod;
        
        public static TrendAnalysisResponse empty(MetricType metricType, PeriodType periodType) {
            return TrendAnalysisResponse.builder()
                    .metricType(metricType)
                    .periodType(periodType)
                    .periodsAnalyzed(0)
                    .dataPoints(List.of())
                    .overallTrend(TrendDirection.STABLE)
                    .build();
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class TrendDataPoint {
        private String periodLabel;
        private BigDecimal value;
        private BigDecimal changePercent;
        private TrendDirection trendDirection;
    }

    @lombok.Builder
    @lombok.Data
    public static class TrendSummary {
        private MetricType metricType;
        private BigDecimal currentValue;
        private BigDecimal previousValue;
        private BigDecimal changePercent;
        private TrendDirection trendDirection;
        private String periodLabel;
    }

    @lombok.Builder
    @lombok.Data
    public static class RetentionPolicyStatus {
        private int retentionYears;
        private int dailyRetentionDays;
        private long dailyMetricsCount;
        private long monthlyMetricsCount;
        private long yearlyMetricsCount;
        private LocalDate oldestDataDate;
    }
}
