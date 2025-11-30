package com.evfleet.analytics.controller;

import com.evfleet.analytics.dto.ESGReportRequest;
import com.evfleet.analytics.dto.ESGReportResponse;
import com.evfleet.analytics.dto.ESGReportResponse.*;
import com.evfleet.analytics.model.ESGReport.ComplianceStandard;
import com.evfleet.analytics.service.ESGReportService;
import com.evfleet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ESG Report Controller
 * 
 * REST API endpoints for Environmental, Social, and Governance (ESG) reporting.
 * Provides endpoints for carbon footprint calculation, report generation,
 * compliance tracking, and emissions trend analysis.
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/esg")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ESG Reporting", description = "Environmental, Social, and Governance Report APIs")
public class ESGController {

    private final ESGReportService esgReportService;

    // ========== CARBON FOOTPRINT ==========

    @GetMapping("/carbon-footprint/{companyId}")
    @Operation(summary = "Calculate carbon footprint for a fleet",
               description = "Calculate Scope 1, 2, 3 emissions for a company's fleet over a period")
    public ResponseEntity<ApiResponse<CarbonEmissions>> calculateCarbonFootprint(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/v1/esg/carbon-footprint/{} - {} to {}", companyId, startDate, endDate);
        
        CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Carbon footprint calculated successfully", emissions));
    }

    // ========== CARBON SAVINGS ==========

    @GetMapping("/carbon-savings/{companyId}")
    @Operation(summary = "Calculate carbon savings vs ICE baseline",
               description = "Calculate CO2 savings compared to equivalent ICE fleet")
    public ResponseEntity<ApiResponse<CarbonSavings>> calculateCarbonSavings(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/v1/esg/carbon-savings/{} - {} to {}", companyId, startDate, endDate);
        
        CarbonSavings savings = esgReportService.calculateCarbonSavings(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Carbon savings calculated successfully", savings));
    }

    // ========== EMISSIONS TREND ==========

    @GetMapping("/emissions-trend/{companyId}")
    @Operation(summary = "Track emissions trend over time",
               description = "Get monthly emissions trend for analysis")
    public ResponseEntity<ApiResponse<List<EmissionsTrend>>> trackEmissionsTrend(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/v1/esg/emissions-trend/{} - {} to {}", companyId, startDate, endDate);
        
        List<EmissionsTrend> trend = esgReportService.trackEmissionsTrend(companyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Emissions trend retrieved successfully", trend));
    }

    // ========== REPORT GENERATION ==========

    @PostMapping("/reports/generate")
    @Operation(summary = "Generate ESG report",
               description = "Generate comprehensive ESG report with emissions, savings, and compliance data")
    public ResponseEntity<ApiResponse<ESGReportResponse>> generateReport(
            @Valid @RequestBody ESGReportRequest request) {
        
        log.info("POST /api/v1/esg/reports/generate - Company: {}, Type: {}", 
                request.getCompanyId(), request.getReportType());
        
        ESGReportResponse report = esgReportService.generateReport(request);
        return ResponseEntity.ok(ApiResponse.success("ESG report generated successfully", report));
    }

    @PostMapping("/reports/compliance")
    @Operation(summary = "Generate compliance report",
               description = "Generate ESG report for specific compliance standard (SEBI BRSR, GRI, CDP)")
    public ResponseEntity<ApiResponse<ESGReportResponse>> generateComplianceReport(
            @RequestParam Long companyId,
            @RequestParam ComplianceStandard standard,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("POST /api/v1/esg/reports/compliance - Company: {}, Standard: {}", companyId, standard);
        
        ESGReportResponse report = esgReportService.generateComplianceReport(companyId, standard, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(standard.name() + " compliance report generated successfully", report));
    }

    // ========== REPORT RETRIEVAL ==========

    @GetMapping("/reports/company/{companyId}")
    @Operation(summary = "Get ESG reports for a company",
               description = "Retrieve all ESG reports for a company with pagination")
    public ResponseEntity<ApiResponse<Page<ESGReportResponse>>> getReportsByCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.info("GET /api/v1/esg/reports/company/{}", companyId);
        
        Page<ESGReportResponse> reports = esgReportService.getReportsByCompany(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success("ESG reports retrieved successfully", reports));
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "Get ESG report by ID",
               description = "Retrieve a specific ESG report")
    public ResponseEntity<ApiResponse<ESGReportResponse>> getReportById(@PathVariable Long reportId) {
        
        log.info("GET /api/v1/esg/reports/{}", reportId);
        
        return esgReportService.getReportById(reportId)
                .map(report -> ResponseEntity.ok(ApiResponse.success("ESG report retrieved successfully", report)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reports/latest/{companyId}")
    @Operation(summary = "Get latest ESG report",
               description = "Retrieve the most recent ESG report for a company")
    public ResponseEntity<ApiResponse<ESGReportResponse>> getLatestReport(@PathVariable Long companyId) {
        
        log.info("GET /api/v1/esg/reports/latest/{}", companyId);
        
        return esgReportService.getLatestReport(companyId)
                .map(report -> ResponseEntity.ok(ApiResponse.success("Latest ESG report retrieved successfully", report)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== REPORT ACTIONS ==========

    @PutMapping("/reports/{reportId}/approve")
    @Operation(summary = "Approve ESG report",
               description = "Mark an ESG report as approved")
    public ResponseEntity<ApiResponse<ESGReportResponse>> approveReport(
            @PathVariable Long reportId,
            @RequestParam Long approvedBy) {
        
        log.info("PUT /api/v1/esg/reports/{}/approve - By: {}", reportId, approvedBy);
        
        ESGReportResponse report = esgReportService.approveReport(reportId, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("ESG report approved successfully", report));
    }

    // ========== EXPORT ==========

    @GetMapping("/reports/{reportId}/export/csv")
    @Operation(summary = "Export report to CSV",
               description = "Download ESG report in CSV format")
    public ResponseEntity<String> exportToCsv(@PathVariable Long reportId) {
        
        log.info("GET /api/v1/esg/reports/{}/export/csv", reportId);
        
        String csv = esgReportService.exportToCsv(reportId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=esg-report-" + reportId + ".csv")
                .body(csv);
    }

    // ========== QUICK SUMMARY ==========

    @GetMapping("/summary/{companyId}")
    @Operation(summary = "Get ESG summary",
               description = "Get quick ESG summary with key metrics for dashboard")
    public ResponseEntity<ApiResponse<ESGSummary>> getEsgSummary(
            @PathVariable Long companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Default to last 30 days if dates not provided
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        log.info("GET /api/v1/esg/summary/{} - {} to {}", companyId, startDate, endDate);
        
        CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(companyId, startDate, endDate);
        CarbonSavings savings = esgReportService.calculateCarbonSavings(companyId, startDate, endDate);
        
        ESGSummary summary = ESGSummary.builder()
                .companyId(companyId)
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalEmissionsKg(emissions.getTotalEmissionsKg())
                .carbonSavingsKg(savings.getCarbonSavingsKg())
                .emissionsReductionPercent(savings.getEmissionsReductionPercent())
                .carbonSavingsRupees(savings.getCarbonSavingsRupees())
                .carbonIntensityPerKm(emissions.getCarbonIntensityPerKm())
                .treesEquivalent(emissions.getTreesEquivalent())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("ESG summary retrieved successfully", summary));
    }

    // ========== INNER DTO ==========

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ESGSummary {
        private Long companyId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private java.math.BigDecimal totalEmissionsKg;
        private java.math.BigDecimal carbonSavingsKg;
        private java.math.BigDecimal emissionsReductionPercent;
        private java.math.BigDecimal carbonSavingsRupees;
        private java.math.BigDecimal carbonIntensityPerKm;
        private Integer treesEquivalent;
    }
}
