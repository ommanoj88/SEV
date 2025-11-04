package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.FleetSummaryResponse;
import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.entity.CostAnalytics;
import com.evfleet.analytics.entity.UtilizationReport;
import com.evfleet.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics API for fleet management and cost analysis")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/fleet/{companyId}")
    @Operation(summary = "Get fleet summary", description = "Retrieve fleet summary for a specific company")
    public ResponseEntity<FleetSummaryResponse> getFleetSummary(@PathVariable String companyId) {
        FleetSummaryResponse response = analyticsService.getFleetSummary(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tco/{vehicleId}")
    @Operation(summary = "Get TCO analysis", description = "Retrieve Total Cost of Ownership analysis for a specific vehicle")
    public ResponseEntity<TCOAnalysisResponse> getTCOAnalysis(@PathVariable String vehicleId) {
        TCOAnalysisResponse response = analyticsService.getTCOAnalysis(vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost/{companyId}")
    @Operation(summary = "Get cost analytics", description = "Retrieve cost analytics data for a specific company")
    public ResponseEntity<List<CostAnalytics>> getCostAnalytics(@PathVariable String companyId) {
        List<CostAnalytics> costAnalytics = analyticsService.getCostAnalytics(companyId);
        return ResponseEntity.ok(costAnalytics);
    }

    @GetMapping("/utilization/{vehicleId}")
    @Operation(summary = "Get utilization reports", description = "Retrieve utilization reports for a specific vehicle")
    public ResponseEntity<List<UtilizationReport>> getUtilizationReports(@PathVariable String vehicleId) {
        List<UtilizationReport> reports = analyticsService.getUtilizationReports(vehicleId);
        return ResponseEntity.ok(reports);
    }
}
