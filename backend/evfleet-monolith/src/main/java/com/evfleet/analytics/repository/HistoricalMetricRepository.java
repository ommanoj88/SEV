package com.evfleet.analytics.repository;

import com.evfleet.analytics.model.HistoricalMetric;
import com.evfleet.analytics.model.HistoricalMetric.MetricType;
import com.evfleet.analytics.model.HistoricalMetric.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Historical Metric Repository
 * 
 * Provides data access for historical metrics with support for:
 * - Period-based queries
 * - Trend analysis
 * - Data retention and cleanup
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface HistoricalMetricRepository extends JpaRepository<HistoricalMetric, Long> {

    // ==================== Basic Queries ====================

    /**
     * Find metric by company, period, and type
     */
    Optional<HistoricalMetric> findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
            Long companyId, PeriodType periodType, LocalDate periodStart, MetricType metricType);

    /**
     * Find all metrics for a company and period type
     */
    List<HistoricalMetric> findByCompanyIdAndPeriodTypeOrderByPeriodStartDesc(
            Long companyId, PeriodType periodType);

    /**
     * Find metrics by type for a company
     */
    List<HistoricalMetric> findByCompanyIdAndMetricTypeOrderByPeriodStartDesc(
            Long companyId, MetricType metricType);

    /**
     * Find all metrics for a specific date range
     */
    List<HistoricalMetric> findByCompanyIdAndPeriodStartBetweenOrderByPeriodStartAsc(
            Long companyId, LocalDate startDate, LocalDate endDate);

    // ==================== Period-Based Queries ====================

    /**
     * Find daily metrics for a date range
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.periodType = 'DAILY' AND h.periodStart BETWEEN :startDate AND :endDate " +
           "ORDER BY h.periodStart ASC")
    List<HistoricalMetric> findDailyMetrics(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find monthly metrics for a year
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.periodType = 'MONTHLY' AND YEAR(h.periodStart) = :year " +
           "ORDER BY h.periodStart ASC")
    List<HistoricalMetric> findMonthlyMetricsForYear(
            @Param("companyId") Long companyId,
            @Param("year") int year);

    /**
     * Find quarterly metrics for a year
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.periodType = 'QUARTERLY' AND YEAR(h.periodStart) = :year " +
           "ORDER BY h.periodStart ASC")
    List<HistoricalMetric> findQuarterlyMetricsForYear(
            @Param("companyId") Long companyId,
            @Param("year") int year);

    /**
     * Find yearly metrics
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.periodType = 'YEARLY' ORDER BY h.periodStart DESC")
    List<HistoricalMetric> findYearlyMetrics(@Param("companyId") Long companyId);

    // ==================== Trend Analysis Queries ====================

    /**
     * Find metrics for trend calculation (last N periods)
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.metricType = :metricType AND h.periodType = :periodType " +
           "ORDER BY h.periodStart DESC")
    List<HistoricalMetric> findRecentMetrics(
            @Param("companyId") Long companyId,
            @Param("metricType") MetricType metricType,
            @Param("periodType") PeriodType periodType);

    /**
     * Find previous period metric for comparison
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.metricType = :metricType AND h.periodType = :periodType " +
           "AND h.periodStart < :currentPeriodStart " +
           "ORDER BY h.periodStart DESC LIMIT 1")
    Optional<HistoricalMetric> findPreviousPeriodMetric(
            @Param("companyId") Long companyId,
            @Param("metricType") MetricType metricType,
            @Param("periodType") PeriodType periodType,
            @Param("currentPeriodStart") LocalDate currentPeriodStart);

    /**
     * Find metrics with positive trend
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.trendDirection = 'UP' AND h.periodStart >= :sinceDate " +
           "ORDER BY h.changePercent DESC")
    List<HistoricalMetric> findPositiveTrends(
            @Param("companyId") Long companyId,
            @Param("sinceDate") LocalDate sinceDate);

    /**
     * Find metrics with negative trend
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.trendDirection = 'DOWN' AND h.periodStart >= :sinceDate " +
           "ORDER BY h.changePercent ASC")
    List<HistoricalMetric> findNegativeTrends(
            @Param("companyId") Long companyId,
            @Param("sinceDate") LocalDate sinceDate);

    // ==================== Statistics Queries ====================

    /**
     * Calculate average for a metric over a period
     */
    @Query("SELECT AVG(h.metricValue) FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.metricType = :metricType AND h.periodStart BETWEEN :startDate AND :endDate")
    BigDecimal calculateAverageMetric(
            @Param("companyId") Long companyId,
            @Param("metricType") MetricType metricType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate sum for a metric over a period
     */
    @Query("SELECT SUM(h.metricValue) FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.metricType = :metricType AND h.periodStart BETWEEN :startDate AND :endDate")
    BigDecimal calculateSumMetric(
            @Param("companyId") Long companyId,
            @Param("metricType") MetricType metricType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find min/max values for a metric
     */
    @Query("SELECT MIN(h.metricValue), MAX(h.metricValue) FROM HistoricalMetric h " +
           "WHERE h.companyId = :companyId AND h.metricType = :metricType " +
           "AND h.periodStart BETWEEN :startDate AND :endDate")
    Object[] findMinMaxMetric(
            @Param("companyId") Long companyId,
            @Param("metricType") MetricType metricType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ==================== Comparison Queries ====================

    /**
     * Compare metrics across companies (for benchmarking)
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.metricType = :metricType " +
           "AND h.periodType = :periodType AND h.periodStart = :periodStart " +
           "ORDER BY h.metricValue DESC")
    List<HistoricalMetric> findMetricsForBenchmark(
            @Param("metricType") MetricType metricType,
            @Param("periodType") PeriodType periodType,
            @Param("periodStart") LocalDate periodStart);

    // ==================== Data Retention Queries ====================

    /**
     * Delete old metrics beyond retention period
     */
    @Modifying
    @Query("DELETE FROM HistoricalMetric h WHERE h.periodStart < :retentionDate")
    int deleteOldMetrics(@Param("retentionDate") LocalDate retentionDate);

    /**
     * Delete daily metrics older than threshold (keep monthly aggregates)
     */
    @Modifying
    @Query("DELETE FROM HistoricalMetric h WHERE h.periodType = 'DAILY' " +
           "AND h.periodStart < :retentionDate")
    int deleteDailyMetricsOlderThan(@Param("retentionDate") LocalDate retentionDate);

    /**
     * Count metrics by period type
     */
    @Query("SELECT h.periodType, COUNT(h) FROM HistoricalMetric h " +
           "WHERE h.companyId = :companyId GROUP BY h.periodType")
    List<Object[]> countMetricsByPeriodType(@Param("companyId") Long companyId);

    /**
     * Check if metrics exist for a period
     */
    boolean existsByCompanyIdAndPeriodTypeAndPeriodStart(
            Long companyId, PeriodType periodType, LocalDate periodStart);

    // ==================== Aggregation Helpers ====================

    /**
     * Find all distinct metric types for a company
     */
    @Query("SELECT DISTINCT h.metricType FROM HistoricalMetric h WHERE h.companyId = :companyId")
    List<MetricType> findDistinctMetricTypes(@Param("companyId") Long companyId);

    /**
     * Find latest metric of each type for a company
     */
    @Query("SELECT h FROM HistoricalMetric h WHERE h.companyId = :companyId " +
           "AND h.periodType = :periodType " +
           "AND h.periodStart = (SELECT MAX(h2.periodStart) FROM HistoricalMetric h2 " +
           "WHERE h2.companyId = h.companyId AND h2.metricType = h.metricType " +
           "AND h2.periodType = :periodType)")
    List<HistoricalMetric> findLatestMetricsOfEachType(
            @Param("companyId") Long companyId,
            @Param("periodType") PeriodType periodType);
}
