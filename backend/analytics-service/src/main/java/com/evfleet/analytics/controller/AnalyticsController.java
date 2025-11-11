package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.FleetSummaryResponse;
import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.dto.VehicleReportRequest;
import com.evfleet.analytics.entity.CostAnalytics;
import com.evfleet.analytics.entity.UtilizationReport;
import com.evfleet.analytics.service.AnalyticsService;
import com.evfleet.analytics.service.VehicleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics API for fleet management and cost analysis")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final VehicleReportService vehicleReportService;

    @GetMapping("/fleet")
    @Operation(summary = "Get fleet summary (all)", description = "Retrieve fleet summary for all companies")
    public ResponseEntity<FleetSummaryResponse> getFleetSummaryAll() {
        // Note: Should aggregate across all companies
        // For now, returning empty response
        return ResponseEntity.ok(new FleetSummaryResponse());
    }

    @GetMapping("/fleet/{companyId}")
    @Operation(summary = "Get fleet summary", description = "Retrieve fleet summary for a specific company")
    public ResponseEntity<FleetSummaryResponse> getFleetSummary(@PathVariable String companyId) {
        FleetSummaryResponse response = analyticsService.getFleetSummary(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fleet/company/{companyId}")
    @Operation(summary = "Get fleet analytics by company (alias)", description = "Retrieve fleet analytics for a specific company - alternate path")
    public ResponseEntity<FleetSummaryResponse> getFleetAnalyticsByCompany(@PathVariable String companyId) {
        FleetSummaryResponse response = analyticsService.getFleetSummary(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tco/{vehicleId}")
    @Operation(summary = "Get TCO analysis (short path)", description = "Retrieve Total Cost of Ownership analysis for a specific vehicle")
    public ResponseEntity<TCOAnalysisResponse> getTCOAnalysis(@PathVariable String vehicleId) {
        TCOAnalysisResponse response = analyticsService.getTCOAnalysis(vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tco-analysis/{vehicleId}")
    @Operation(summary = "Get TCO analysis", description = "Retrieve Total Cost of Ownership analysis for a specific vehicle - alternate path")
    public ResponseEntity<TCOAnalysisResponse> getTCOAnalysisAlt(@PathVariable String vehicleId) {
        TCOAnalysisResponse response = analyticsService.getTCOAnalysis(vehicleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cost/{companyId}")
    @Operation(summary = "Get cost analytics (short path)", description = "Retrieve cost analytics data for a specific company")
    public ResponseEntity<List<CostAnalytics>> getCostAnalytics(@PathVariable String companyId) {
        List<CostAnalytics> costAnalytics = analyticsService.getCostAnalytics(companyId);
        return ResponseEntity.ok(costAnalytics);
    }

    @GetMapping("/cost-analytics")
    @Operation(summary = "Get cost analytics (all)", description = "Retrieve cost analytics data for all companies")
    public ResponseEntity<List<CostAnalytics>> getCostAnalyticsAll() {
        // Note: Should aggregate across all companies
        // For now, returning empty list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/cost-analytics/{vehicleId}")
    @Operation(summary = "Get cost analytics by vehicle", description = "Retrieve cost analytics for a specific vehicle")
    public ResponseEntity<CostAnalytics> getCostAnalyticsByVehicle(@PathVariable String vehicleId) {
        // Note: Should be implemented in service layer
        // For now, returning empty response
        return ResponseEntity.ok(new CostAnalytics());
    }

    @GetMapping("/utilization/{vehicleId}")
    @Operation(summary = "Get utilization reports", description = "Retrieve utilization reports for a specific vehicle")
    public ResponseEntity<List<UtilizationReport>> getUtilizationReports(@PathVariable String vehicleId) {
        List<UtilizationReport> reports = analyticsService.getUtilizationReports(vehicleId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/utilization-reports")
    @Operation(summary = "Get all utilization reports", description = "Retrieve utilization reports for all vehicles")
    public ResponseEntity<List<UtilizationReport>> getAllUtilizationReports() {
        // Note: Should be implemented in service layer to get reports for all vehicles
        // For now, returning empty list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/energy-consumption")
    @Operation(summary = "Get energy consumption", description = "Retrieve energy consumption analytics")
    public ResponseEntity<List<java.util.Map<String, Object>>> getEnergyConsumption(@RequestParam(required = false) java.util.Map<String, String> params) {
        // Note: Should be implemented in service layer
        // For now, returning empty list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/carbon-footprint")
    @Operation(summary = "Get carbon footprint", description = "Retrieve carbon footprint data")
    public ResponseEntity<List<java.util.Map<String, Object>>> getCarbonFootprint(@RequestParam(required = false) java.util.Map<String, String> params) {
        // Note: Should be implemented in service layer
        // For now, returning empty list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/battery")
    @Operation(summary = "Get battery analytics", description = "Retrieve battery analytics data")
    public ResponseEntity<java.util.Map<String, Object>> getBatteryAnalytics() {
        // Note: Should be implemented in service layer
        // For now, returning empty response
        return ResponseEntity.ok(java.util.Map.of());
    }

    @PostMapping("/export")
    @Operation(summary = "Export analytics report", description = "Export analytics report in specified format")
    public ResponseEntity<byte[]> exportReport(@RequestBody java.util.Map<String, Object> request) {
        // Note: Should be implemented in service layer
        // For now, returning empty response
        return ResponseEntity.ok(new byte[0]);
    }

    // ========================================
    // VEHICLE REPORT ENDPOINTS
    // ========================================

    @PostMapping("/reports/vehicle")
    @Operation(summary = "Generate vehicle report", 
               description = "Generate a comprehensive vehicle report with genealogy and historical data")
    public ResponseEntity<byte[]> generateVehicleReport(@RequestBody VehicleReportRequest request) {
        byte[] reportData = vehicleReportService.generateVehicleReport(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "vehicle_report_" + request.getVehicleId() + "_" + System.currentTimeMillis() + ".pdf");
        
        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }

    @GetMapping("/reports/vehicle/{vehicleId}/genealogy")
    @Operation(summary = "Generate vehicle genealogy report", 
               description = "Generate a genealogy report focusing on complete event history")
    public ResponseEntity<byte[]> generateGenealogyReport(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        byte[] reportData = vehicleReportService.generateGenealogyReport(vehicleId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "vehicle_genealogy_" + vehicleId + "_" + System.currentTimeMillis() + ".pdf");
        
        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }
}
