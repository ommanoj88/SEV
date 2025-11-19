package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.*;
import com.evfleet.analytics.service.AnalyticsService;
import com.evfleet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Analytics Controller
 * Handles all analytics-related REST endpoints
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Fleet Analytics and Reporting API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/fleet")
    @Operation(summary = "Get fleet analytics summary (default: today)")
    public ResponseEntity<ApiResponse<FleetSummaryResponse>> getFleet(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/v1/analytics/fleet - companyId: {}", companyId);
        // For now, return today's summary for company 1 if no companyId provided
        Long targetCompanyId = (companyId != null) ? companyId : 1L;
        FleetSummaryResponse summary = analyticsService.getTodaysSummary(targetCompanyId);
        return ResponseEntity.ok(ApiResponse.success("Fleet analytics retrieved successfully", summary));
    }

    @GetMapping("/fleet-summary")
    @Operation(summary = "Get fleet summary for a specific date")
    public ResponseEntity<ApiResponse<FleetSummaryResponse>> getFleetSummary(
            @RequestParam Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/v1/analytics/fleet-summary - companyId: {}, date: {}", companyId, date);
        FleetSummaryResponse summary = analyticsService.getFleetSummary(companyId, date);
        return ResponseEntity.ok(ApiResponse.success("Fleet summary retrieved successfully", summary));
    }

    @GetMapping("/fleet-summary/today")
    @Operation(summary = "Get today's fleet summary")
    public ResponseEntity<ApiResponse<FleetSummaryResponse>> getTodaysSummary(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/analytics/fleet-summary/today - companyId: {}", companyId);
        FleetSummaryResponse summary = analyticsService.getTodaysSummary(companyId);
        return ResponseEntity.ok(ApiResponse.success("Today's fleet summary retrieved successfully", summary));
    }

    @GetMapping("/fleet-summary/range")
    @Operation(summary = "Get fleet summary for a date range")
    public ResponseEntity<ApiResponse<List<FleetSummaryResponse>>> getFleetSummaryRange(
            @RequestParam Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/fleet-summary/range - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);
        List<FleetSummaryResponse> summaries = analyticsService.getFleetSummaryRange(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Fleet summary range retrieved successfully", summaries));
    }

    @GetMapping("/monthly-report")
    @Operation(summary = "Get monthly fleet report")
    public ResponseEntity<ApiResponse<List<FleetSummaryResponse>>> getMonthlyReport(
            @RequestParam Long companyId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("GET /api/v1/analytics/monthly-report - companyId: {}, year: {}, month: {}", companyId, year, month);
        List<FleetSummaryResponse> report = analyticsService.getMonthlyReport(companyId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Monthly report retrieved successfully", report));
    }

    /**
     * E1 Fix: Get comprehensive fleet analytics
     */
    @GetMapping("/fleet-analytics")
    @Operation(summary = "Get comprehensive fleet analytics including status breakdown and battery metrics")
    public ResponseEntity<ApiResponse<FleetAnalyticsResponse>> getFleetAnalytics(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/v1/analytics/fleet-analytics - companyId: {}", companyId);
        Long targetCompanyId = (companyId != null) ? companyId : 1L;
        FleetAnalyticsResponse analytics = analyticsService.getFleetAnalytics(targetCompanyId);
        return ResponseEntity.ok(ApiResponse.success("Fleet analytics retrieved successfully", analytics));
    }

    /**
     * E2 Fix: Get utilization reports for all vehicles
     */
    @GetMapping("/utilization-reports")
    @Operation(summary = "Get vehicle utilization reports")
    public ResponseEntity<ApiResponse<List<VehicleUtilizationResponse>>> getUtilizationReports(
            @RequestParam Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/utilization-reports - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        List<VehicleUtilizationResponse> reports = analyticsService.getUtilizationReports(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Utilization reports retrieved successfully", reports));
    }

    /**
     * E3 Fix: Get cost analytics for company
     */
    @GetMapping("/cost-analytics")
    @Operation(summary = "Get cost analytics for company")
    public ResponseEntity<ApiResponse<List<CostAnalyticsResponse>>> getCostAnalytics(
            @RequestParam Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/cost-analytics - companyId: {}, startDate: {}, endDate: {}",
                companyId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.now().minusMonths(12);
        if (endDate == null) endDate = LocalDate.now();

        List<CostAnalyticsResponse> analytics = analyticsService.getCostAnalytics(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Cost analytics retrieved successfully", analytics));
    }

    /**
     * E3 Fix: Get TCO analysis for vehicle
     */
    @GetMapping("/tco-analysis/{vehicleId}")
    @Operation(summary = "Get Total Cost of Ownership analysis for vehicle")
    public ResponseEntity<ApiResponse<TCOAnalysisResponse>> getTCOAnalysis(
            @PathVariable Long vehicleId) {
        log.info("GET /api/v1/analytics/tco-analysis/{} - vehicleId: {}", vehicleId, vehicleId);
        TCOAnalysisResponse tco = analyticsService.getTCOAnalysis(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("TCO analysis retrieved successfully", tco));
    }
}
