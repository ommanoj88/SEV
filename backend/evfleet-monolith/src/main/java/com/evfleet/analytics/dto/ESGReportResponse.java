package com.evfleet.analytics.dto;

import com.evfleet.analytics.model.ESGReport.ComplianceStandard;
import com.evfleet.analytics.model.ESGReport.ReportStatus;
import com.evfleet.analytics.model.ESGReport.ReportType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ESG Report Response DTO
 * 
 * Data Transfer Object for ESG (Environmental, Social, Governance) reports.
 * Contains all emission metrics, carbon savings, fleet statistics, and compliance data.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESGReportResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String reportName;
    private ReportType reportType;
    private ComplianceStandard complianceStandard;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;

    // ========== CARBON EMISSIONS ==========

    /**
     * Carbon emissions breakdown by scope
     */
    private CarbonEmissions carbonEmissions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CarbonEmissions {
        /**
         * Scope 1: Direct emissions from fleet vehicles
         */
        private BigDecimal scope1EmissionsKg;
        private String scope1Description;

        /**
         * Scope 2: Indirect emissions from electricity
         */
        private BigDecimal scope2EmissionsKg;
        private String scope2Description;

        /**
         * Scope 3: Other indirect emissions
         */
        private BigDecimal scope3EmissionsKg;
        private String scope3Description;

        /**
         * Total emissions (all scopes)
         */
        private BigDecimal totalEmissionsKg;
        private BigDecimal totalEmissionsTons;

        /**
         * Equivalent trees needed to offset
         */
        private Integer treesEquivalent;

        /**
         * Carbon intensity
         */
        private BigDecimal carbonIntensityPerKm;
    }

    // ========== CARBON SAVINGS ==========

    /**
     * Carbon savings compared to ICE baseline
     */
    private CarbonSavings carbonSavings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CarbonSavings {
        /**
         * Baseline if entire fleet were ICE
         */
        private BigDecimal baselineIceEmissionsKg;

        /**
         * Actual emissions saved
         */
        private BigDecimal carbonSavingsKg;
        private BigDecimal carbonSavingsTons;

        /**
         * Percentage reduction vs baseline
         */
        private BigDecimal emissionsReductionPercent;

        /**
         * Financial value of carbon savings
         */
        private BigDecimal carbonSavingsRupees;
        private BigDecimal carbonCostRupees;

        /**
         * Equivalent environmental impact
         */
        private EnvironmentalEquivalent equivalents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnvironmentalEquivalent {
        /**
         * Trees planted equivalent
         */
        private Integer treesPlanted;

        /**
         * Cars off road for a year
         */
        private Integer carsOffRoad;

        /**
         * Liters of petrol saved
         */
        private BigDecimal litersPetrolSaved;

        /**
         * Homes powered for a year
         */
        private Integer homesPowered;
    }

    // ========== FLEET METRICS ==========

    /**
     * Fleet composition and operations
     */
    private FleetMetrics fleetMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FleetMetrics {
        private Integer totalVehicles;
        private Integer evVehicleCount;
        private Integer iceVehicleCount;
        private Integer hybridVehicleCount;
        private BigDecimal electrificationPercent;

        private BigDecimal totalKilometersDriven;
        private BigDecimal totalEnergyKwh;
        private BigDecimal totalFuelLiters;

        private BigDecimal energyIntensityPerKm;
        private BigDecimal fuelIntensityPerKm;

        /**
         * Average daily utilization
         */
        private BigDecimal avgDailyUtilizationPercent;
    }

    // ========== EMISSIONS TREND ==========

    /**
     * Monthly emissions trend
     */
    private List<EmissionsTrend> emissionsTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmissionsTrend {
        private String month;
        private int year;
        private BigDecimal totalEmissionsKg;
        private BigDecimal carbonSavingsKg;
        private BigDecimal emissionsReductionPercent;
        private BigDecimal avgCarbonIntensity;
    }

    // ========== COMPLIANCE ==========

    /**
     * Compliance status and details
     */
    private ComplianceInfo compliance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplianceInfo {
        private Boolean isCompliant;
        private ComplianceStandard standard;
        private String notes;
        private List<String> requirements;
        private List<String> checklistItems;
        private BigDecimal complianceScore;
    }

    // ========== METADATA ==========

    private ReportStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;
    private Long generatedBy;
    private String generatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== SUMMARY ==========

    /**
     * Executive summary for the report
     */
    private ExecutiveSummary summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExecutiveSummary {
        private String periodDescription;
        private String headline;
        private List<String> keyHighlights;
        private List<String> recommendations;
        private BigDecimal overallScore;
        private String rating; // A, B, C, D, F
    }
}
