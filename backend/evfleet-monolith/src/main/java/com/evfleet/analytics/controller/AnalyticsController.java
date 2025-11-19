package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.*;
import com.evfleet.analytics.service.AnalyticsService;
import com.evfleet.analytics.service.TCOAnalysisService;
import com.evfleet.analytics.service.EnergyAnalyticsService;
import com.evfleet.analytics.service.ReportGenerationService;
import com.evfleet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final TCOAnalysisService tcoAnalysisService;
    private final EnergyAnalyticsService energyAnalyticsService;
    private final ReportGenerationService reportGenerationService;

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

    // ================================================================================
    // E4-E7 Analytics Features - New Endpoints
    // ================================================================================

    /**
     * E4: Calculate TCO analysis for a vehicle with custom period
     */
    @GetMapping("/tco/{vehicleId}")
    @Operation(summary = "Calculate Total Cost of Ownership analysis for vehicle")
    public ResponseEntity<ApiResponse<TCOAnalysisResponse>> calculateTCO(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "5") Integer years) {
        log.info("GET /api/v1/analytics/tco/{} - vehicleId: {}, years: {}", vehicleId, vehicleId, years);
        TCOAnalysisResponse tco = tcoAnalysisService.calculateTCO(vehicleId, years);
        return ResponseEntity.ok(ApiResponse.success("TCO analysis calculated successfully", tco));
    }

    /**
     * E4: Get TCO trend for a vehicle over time
     */
    @GetMapping("/tco/{vehicleId}/trend")
    @Operation(summary = "Get TCO trend for a vehicle over time")
    public ResponseEntity<ApiResponse<List<TCOAnalysisResponse>>> getTCOTrend(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/tco/{}/trend - vehicleId: {}, startDate: {}, endDate: {}", 
                vehicleId, vehicleId, startDate, endDate);
        List<TCOAnalysisResponse> trend = tcoAnalysisService.getTCOTrend(vehicleId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("TCO trend retrieved successfully", trend));
    }

    /**
     * E5: Get energy consumption for a vehicle
     */
    @GetMapping("/energy-consumption/{vehicleId}")
    @Operation(summary = "Get energy consumption analytics for a vehicle")
    public ResponseEntity<ApiResponse<List<EnergyConsumptionResponse>>> getEnergyConsumption(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/energy-consumption/{} - vehicleId: {}, startDate: {}, endDate: {}", 
                vehicleId, vehicleId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        List<EnergyConsumptionResponse> energy = energyAnalyticsService.getEnergyConsumption(vehicleId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Energy consumption retrieved successfully", energy));
    }

    /**
     * E5: Get energy consumption for a specific date
     */
    @GetMapping("/energy-consumption/{vehicleId}/date/{date}")
    @Operation(summary = "Get energy consumption for a specific date")
    public ResponseEntity<ApiResponse<EnergyConsumptionResponse>> getEnergyConsumptionForDate(
            @PathVariable Long vehicleId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/v1/analytics/energy-consumption/{}/date/{} - vehicleId: {}, date: {}", 
                vehicleId, date, vehicleId, date);
        EnergyConsumptionResponse energy = energyAnalyticsService.getEnergyConsumptionForDate(vehicleId, date);
        return ResponseEntity.ok(ApiResponse.success("Energy consumption retrieved successfully", energy));
    }

    /**
     * E5: Compare vehicle efficiency across fleet
     */
    @GetMapping("/energy-comparison")
    @Operation(summary = "Compare vehicle efficiency across the fleet")
    public ResponseEntity<ApiResponse<List<EnergyConsumptionResponse>>> compareVehicleEfficiency(
            @RequestParam Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/energy-comparison - companyId: {}, startDate: {}, endDate: {}", 
                companyId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        List<EnergyConsumptionResponse> comparison = energyAnalyticsService.compareVehicleEfficiency(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Vehicle efficiency comparison retrieved successfully", comparison));
    }

    /**
     * E5: Get energy trend for a vehicle
     */
    @GetMapping("/energy/{vehicleId}/trend")
    @Operation(summary = "Get energy consumption trend for a vehicle")
    public ResponseEntity<ApiResponse<List<EnergyConsumptionResponse>>> getEnergyTrend(
            @PathVariable Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/v1/analytics/energy/{}/trend - vehicleId: {}, startDate: {}, endDate: {}", 
                vehicleId, vehicleId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        List<EnergyConsumptionResponse> trend = energyAnalyticsService.getEnergyTrend(vehicleId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Energy trend retrieved successfully", trend));
    }

    /**
     * E6: Generate comprehensive vehicle report (PDF)
     */
    @PostMapping("/reports/vehicle")
    @Operation(summary = "Generate comprehensive vehicle report in PDF format")
    public ResponseEntity<byte[]> generateVehicleReport(@RequestBody VehicleReportRequest request) {
        log.info("POST /api/v1/analytics/reports/vehicle - vehicleId: {}", request.getVehicleId());
        
        try {
            byte[] pdfBytes = reportGenerationService.generateVehicleReport(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "vehicle-report-" + request.getVehicleId() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            log.error("Error generating vehicle report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * E6: Generate genealogy report (event timeline PDF)
     */
    @GetMapping("/reports/genealogy/{vehicleId}")
    @Operation(summary = "Generate vehicle genealogy report (event timeline) in PDF format")
    public ResponseEntity<byte[]> generateGenealogyReport(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /api/v1/analytics/reports/genealogy/{} - vehicleId: {}, startDate: {}, endDate: {}", 
                vehicleId, vehicleId, startDate, endDate);
        
        try {
            byte[] pdfBytes = reportGenerationService.generateGenealogyReport(vehicleId, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "genealogy-report-" + vehicleId + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            log.error("Error generating genealogy report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
