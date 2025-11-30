package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.DashboardMetricsResponse;
import com.evfleet.analytics.dto.LiveVehiclePositionResponse;
import com.evfleet.analytics.service.DashboardMetricsService;
import com.evfleet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Dashboard Metrics Controller
 * 
 * REST API endpoints for real-time dashboard data.
 * Implements caching, ETag support, and response compression.
 * 
 * Performance Optimizations:
 * - 5-minute cache TTL for summary metrics
 * - 30-second cache TTL for live positions
 * - ETag support for conditional requests (304 Not Modified)
 * - Cache-Control headers for browser caching
 * - Lightweight DTOs for minimal payload size
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Real-Time Dashboard Metrics API")
public class DashboardMetricsController {

    private final DashboardMetricsService dashboardMetricsService;

    // ========== SUMMARY METRICS ==========

    @GetMapping("/summary")
    @Operation(summary = "Get cached dashboard summary metrics",
               description = "Returns cached summary metrics with 5-minute TTL. Supports ETag for conditional requests.")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getSummary(
            @RequestParam Long companyId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        
        log.debug("GET /api/v1/dashboard/summary - companyId: {}, If-None-Match: {}", companyId, ifNoneMatch);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, ifNoneMatch);
        
        // Return 304 Not Modified if ETag matches
        if (metrics == null) {
            log.debug("Returning 304 Not Modified for company {}", companyId);
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .eTag(metrics.getEtag())
                .body(ApiResponse.success("Dashboard summary retrieved successfully", metrics));
    }

    @GetMapping("/summary/{companyId}")
    @Operation(summary = "Get dashboard summary by company ID (path parameter)",
               description = "Alternative endpoint using path parameter for company ID")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getSummaryByPath(
            @PathVariable Long companyId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        
        return getSummary(companyId, ifNoneMatch);
    }

    // ========== LIVE POSITIONS ==========

    @GetMapping("/live")
    @Operation(summary = "Get real-time vehicle positions",
               description = "Returns live vehicle positions with 30-second cache TTL for map rendering")
    public ResponseEntity<ApiResponse<LiveVehiclePositionResponse>> getLivePositions(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/live - companyId: {}", companyId);
        
        LiveVehiclePositionResponse positions = dashboardMetricsService.getLivePositions(companyId);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(ApiResponse.success("Live positions retrieved successfully", positions));
    }

    @GetMapping("/live/{companyId}")
    @Operation(summary = "Get live positions by company ID (path parameter)",
               description = "Alternative endpoint using path parameter for company ID")
    public ResponseEntity<ApiResponse<LiveVehiclePositionResponse>> getLivePositionsByPath(
            @PathVariable Long companyId) {
        
        return getLivePositions(companyId);
    }

    // ========== CACHE MANAGEMENT ==========

    @PostMapping("/refresh")
    @Operation(summary = "Force refresh dashboard cache",
               description = "Invalidates and refreshes the cached metrics for a company")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> refreshMetrics(
            @RequestParam Long companyId) {
        
        log.info("POST /api/v1/dashboard/refresh - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.refreshMetrics(companyId);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(ApiResponse.success("Dashboard metrics refreshed successfully", metrics));
    }

    @PostMapping("/refresh-all")
    @Operation(summary = "Force refresh all dashboard caches",
               description = "Invalidates all cached dashboard metrics (admin only)")
    public ResponseEntity<ApiResponse<String>> refreshAllMetrics() {
        
        log.info("POST /api/v1/dashboard/refresh-all");
        
        dashboardMetricsService.refreshAllMetrics();
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(ApiResponse.success("All dashboard caches refreshed", "OK"));
    }

    // ========== QUICK STATS ENDPOINTS ==========

    @GetMapping("/fleet-overview")
    @Operation(summary = "Get fleet overview only",
               description = "Returns only fleet overview metrics for quick status check")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse.FleetOverview>> getFleetOverview(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/fleet-overview - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("Fleet overview retrieved", metrics.getFleet()));
    }

    @GetMapping("/battery-status")
    @Operation(summary = "Get battery metrics only",
               description = "Returns only battery-related metrics for EV monitoring")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse.BatteryMetrics>> getBatteryStatus(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/battery-status - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("Battery status retrieved", metrics.getBattery()));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get current alerts only",
               description = "Returns only alert summaries for notification panel")
    public ResponseEntity<ApiResponse<java.util.List<DashboardMetricsResponse.AlertSummary>>> getAlerts(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/alerts - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("Alerts retrieved", metrics.getAlerts()));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's performance only",
               description = "Returns only today's performance metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse.TodayPerformance>> getTodayPerformance(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/today - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("Today's performance retrieved", metrics.getToday()));
    }

    @GetMapping("/esg-quick")
    @Operation(summary = "Get ESG quick stats only",
               description = "Returns only ESG summary for sustainability widget")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse.EsgQuickStats>> getEsgQuickStats(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/esg-quick - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("ESG quick stats retrieved", metrics.getEsg()));
    }

    @GetMapping("/charging")
    @Operation(summary = "Get charging infrastructure status",
               description = "Returns charging station availability")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse.ChargingStatus>> getChargingStatus(
            @RequestParam Long companyId) {
        
        log.debug("GET /api/v1/dashboard/charging - companyId: {}", companyId);
        
        DashboardMetricsResponse metrics = dashboardMetricsService.getSummaryMetrics(companyId, null);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success("Charging status retrieved", metrics.getCharging()));
    }

    // ========== HEALTH CHECK ==========

    @GetMapping("/health")
    @Operation(summary = "Dashboard service health check",
               description = "Returns service health status")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard service is healthy", "OK"));
    }
}
