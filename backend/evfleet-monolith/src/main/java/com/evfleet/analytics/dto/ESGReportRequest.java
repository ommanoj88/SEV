package com.evfleet.analytics.dto;

import com.evfleet.analytics.model.ESGReport.ComplianceStandard;
import com.evfleet.analytics.model.ESGReport.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ESG Report Request DTO
 * 
 * Request object for generating ESG reports.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESGReportRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private ComplianceStandard complianceStandard;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    /**
     * Optional custom report name
     */
    private String reportName;

    /**
     * Include detailed emissions breakdown
     */
    @Builder.Default
    private boolean includeEmissionsBreakdown = true;

    /**
     * Include fleet metrics
     */
    @Builder.Default
    private boolean includeFleetMetrics = true;

    /**
     * Include trend analysis
     */
    @Builder.Default
    private boolean includeTrend = true;

    /**
     * Include compliance checklist
     */
    @Builder.Default
    private boolean includeComplianceChecklist = true;

    /**
     * Generate executive summary
     */
    @Builder.Default
    private boolean generateSummary = true;
}
