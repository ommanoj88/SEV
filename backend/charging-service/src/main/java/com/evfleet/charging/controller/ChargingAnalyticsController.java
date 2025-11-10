package com.evfleet.charging.controller;

import com.evfleet.charging.dto.CostSummaryResponse;
import com.evfleet.charging.dto.StationAnalyticsResponse;
import com.evfleet.charging.dto.UtilizationMetricsResponse;
import com.evfleet.charging.service.ChargingAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for Charging Analytics operations
 * Provides endpoints for utilization metrics, cost analysis, and energy tracking
 * 
 * @since PR-10 (Charging Analytics)
 */
@RestController
@RequestMapping("/api/v1/charging/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charging Analytics", description = "APIs for charging station analytics and metrics")
public class ChargingAnalyticsController {

    private final ChargingAnalyticsService analyticsService;

    @GetMapping("/stations/{stationId}")
    @Operation(
        summary = "Get station analytics",
        description = "Retrieve comprehensive analytics for a specific charging station including utilization, cost, and energy metrics"
    )
    public ResponseEntity<StationAnalyticsResponse> getStationAnalytics(
            @Parameter(description = "Station ID", required = true)
            @PathVariable Long stationId,
            @Parameter(description = "Start date for analytics period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @Parameter(description = "End date for analytics period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate) {
        
        log.info("REST request to get analytics for station ID: {}", stationId);
        
        StationAnalyticsResponse response;
        if (startDate != null && endDate != null) {
            log.info("Retrieving analytics for period: {} to {}", startDate, endDate);
            response = analyticsService.getStationAnalytics(stationId, startDate, endDate);
        } else {
            log.info("Retrieving all-time analytics");
            response = analyticsService.getStationAnalytics(stationId);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/utilization")
    @Operation(
        summary = "Get network utilization metrics",
        description = "Retrieve overall utilization metrics across all charging stations in the network"
    )
    public ResponseEntity<UtilizationMetricsResponse> getUtilizationMetrics() {
        log.info("REST request to get overall utilization metrics");
        UtilizationMetricsResponse response = analyticsService.getUtilizationMetrics();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost-summary")
    @Operation(
        summary = "Get cost summary",
        description = "Retrieve cost and revenue summary for a specified period"
    )
    public ResponseEntity<CostSummaryResponse> getCostSummary(
            @Parameter(description = "Start date for cost summary period (ISO format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @Parameter(description = "End date for cost summary period (ISO format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate) {
        
        log.info("REST request to get cost summary from {} to {}", startDate, endDate);
        CostSummaryResponse response = analyticsService.getCostSummary(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost-summary/today")
    @Operation(
        summary = "Get today's cost summary",
        description = "Retrieve cost and revenue summary for the current day"
    )
    public ResponseEntity<CostSummaryResponse> getTodayCostSummary() {
        log.info("REST request to get today's cost summary");
        CostSummaryResponse response = analyticsService.getTodayCostSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost-summary/month")
    @Operation(
        summary = "Get current month's cost summary",
        description = "Retrieve cost and revenue summary for the current month"
    )
    public ResponseEntity<CostSummaryResponse> getMonthCostSummary() {
        log.info("REST request to get current month's cost summary");
        CostSummaryResponse response = analyticsService.getMonthCostSummary();
        return ResponseEntity.ok(response);
    }
}
