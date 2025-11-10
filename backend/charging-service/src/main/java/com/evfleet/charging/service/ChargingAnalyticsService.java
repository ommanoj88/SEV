package com.evfleet.charging.service;

import com.evfleet.charging.dto.CostSummaryResponse;
import com.evfleet.charging.dto.StationAnalyticsResponse;
import com.evfleet.charging.dto.UtilizationMetricsResponse;

import java.time.LocalDateTime;

/**
 * Service interface for charging analytics operations
 * Provides utilization metrics, cost analysis, and energy tracking
 * 
 * @since PR-10 (Charging Analytics)
 */
public interface ChargingAnalyticsService {
    
    /**
     * Get analytics for a specific charging station
     * 
     * @param stationId the station ID
     * @return station analytics including utilization and cost metrics
     */
    StationAnalyticsResponse getStationAnalytics(Long stationId);
    
    /**
     * Get analytics for a specific charging station within a date range
     * 
     * @param stationId the station ID
     * @param startDate start of the period
     * @param endDate end of the period
     * @return station analytics for the specified period
     */
    StationAnalyticsResponse getStationAnalytics(Long stationId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get overall utilization metrics across all stations
     * 
     * @return network-wide utilization metrics
     */
    UtilizationMetricsResponse getUtilizationMetrics();
    
    /**
     * Get cost summary for all charging operations
     * 
     * @param startDate start of the period
     * @param endDate end of the period
     * @return cost summary including revenue and energy metrics
     */
    CostSummaryResponse getCostSummary(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get cost summary for the current day
     * 
     * @return today's cost summary
     */
    CostSummaryResponse getTodayCostSummary();
    
    /**
     * Get cost summary for the current month
     * 
     * @return current month's cost summary
     */
    CostSummaryResponse getMonthCostSummary();
}
