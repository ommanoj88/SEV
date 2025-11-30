package com.evfleet.analytics.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ESG Report Entity
 * 
 * Stores Environmental, Social, and Governance (ESG) reporting data for fleet operations.
 * Tracks carbon emissions, sustainability metrics, and compliance with environmental regulations.
 * 
 * Key Features:
 * - Carbon footprint tracking (Scope 1, 2, 3 emissions)
 * - EV vs ICE baseline comparison for CO2 savings
 * - Government compliance report formats (SEBI BRSR, GRI, CDP)
 * - Monthly/Quarterly/Annual report generation
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "esg_reports", indexes = {
    @Index(name = "idx_esg_company_period", columnList = "company_id, period_start, period_end"),
    @Index(name = "idx_esg_report_type", columnList = "report_type"),
    @Index(name = "idx_esg_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESGReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    /**
     * Report type: MONTHLY, QUARTERLY, ANNUAL, CUSTOM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    /**
     * Compliance standard: SEBI_BRSR, GRI, CDP, CUSTOM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_standard")
    private ComplianceStandard complianceStandard;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // ========== CARBON EMISSIONS (Scope 1, 2, 3) ==========
    
    /**
     * Scope 1: Direct emissions from owned/controlled sources (fleet vehicles)
     * Measured in kg CO2 equivalent
     */
    @Column(name = "scope1_emissions_kg", precision = 15, scale = 2)
    private BigDecimal scope1EmissionsKg;

    /**
     * Scope 2: Indirect emissions from purchased electricity (charging stations)
     * Measured in kg CO2 equivalent
     */
    @Column(name = "scope2_emissions_kg", precision = 15, scale = 2)
    private BigDecimal scope2EmissionsKg;

    /**
     * Scope 3: Other indirect emissions (supply chain, employee commuting)
     * Measured in kg CO2 equivalent
     */
    @Column(name = "scope3_emissions_kg", precision = 15, scale = 2)
    private BigDecimal scope3EmissionsKg;

    /**
     * Total carbon emissions (Scope 1 + 2 + 3)
     */
    @Column(name = "total_emissions_kg", precision = 15, scale = 2)
    private BigDecimal totalEmissionsKg;

    // ========== CARBON SAVINGS ==========

    /**
     * Baseline emissions if entire fleet were ICE vehicles
     */
    @Column(name = "baseline_ice_emissions_kg", precision = 15, scale = 2)
    private BigDecimal baselineIceEmissionsKg;

    /**
     * CO2 savings compared to ICE baseline
     */
    @Column(name = "carbon_savings_kg", precision = 15, scale = 2)
    private BigDecimal carbonSavingsKg;

    /**
     * Percentage reduction compared to ICE baseline
     */
    @Column(name = "emissions_reduction_percent", precision = 5, scale = 2)
    private BigDecimal emissionsReductionPercent;

    // ========== FLEET METRICS ==========

    /**
     * Total kilometers driven by fleet during period
     */
    @Column(name = "total_kilometers_driven", precision = 12, scale = 2)
    private BigDecimal totalKilometersDriven;

    /**
     * Total energy consumed in kWh (electricity)
     */
    @Column(name = "total_energy_kwh", precision = 12, scale = 2)
    private BigDecimal totalEnergyKwh;

    /**
     * Total fuel consumed in liters (petrol/diesel)
     */
    @Column(name = "total_fuel_liters", precision = 12, scale = 2)
    private BigDecimal totalFuelLiters;

    /**
     * Number of EV vehicles in fleet
     */
    @Column(name = "ev_vehicle_count")
    private Integer evVehicleCount;

    /**
     * Number of ICE vehicles in fleet
     */
    @Column(name = "ice_vehicle_count")
    private Integer iceVehicleCount;

    /**
     * Number of Hybrid vehicles in fleet
     */
    @Column(name = "hybrid_vehicle_count")
    private Integer hybridVehicleCount;

    /**
     * Fleet electrification percentage
     */
    @Column(name = "electrification_percent", precision = 5, scale = 2)
    private BigDecimal electrificationPercent;

    // ========== COST METRICS ==========

    /**
     * Total carbon cost in rupees (carbon credits/offsets)
     */
    @Column(name = "carbon_cost_rupees", precision = 12, scale = 2)
    private BigDecimal carbonCostRupees;

    /**
     * Cost savings from reduced carbon emissions
     */
    @Column(name = "carbon_savings_rupees", precision = 12, scale = 2)
    private BigDecimal carbonSavingsRupees;

    // ========== INTENSITY METRICS ==========

    /**
     * Carbon intensity: kg CO2 per km
     */
    @Column(name = "carbon_intensity_per_km", precision = 8, scale = 4)
    private BigDecimal carbonIntensityPerKm;

    /**
     * Energy intensity: kWh per km (for EVs)
     */
    @Column(name = "energy_intensity_per_km", precision = 8, scale = 4)
    private BigDecimal energyIntensityPerKm;

    /**
     * Fuel intensity: liters per km (for ICE)
     */
    @Column(name = "fuel_intensity_per_km", precision = 8, scale = 4)
    private BigDecimal fuelIntensityPerKm;

    // ========== COMPLIANCE ==========

    /**
     * Whether report meets compliance requirements
     */
    @Column(name = "is_compliant")
    private Boolean isCompliant;

    /**
     * Compliance notes or issues
     */
    @Column(name = "compliance_notes", length = 1000)
    private String complianceNotes;

    // ========== REPORT METADATA ==========

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status;

    /**
     * JSON data for additional metrics
     */
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    // ========== ENUMS ==========

    public enum ReportType {
        MONTHLY,
        QUARTERLY,
        ANNUAL,
        CUSTOM
    }

    public enum ComplianceStandard {
        SEBI_BRSR,   // SEBI Business Responsibility and Sustainability Report (India)
        GRI,          // Global Reporting Initiative
        CDP,          // Carbon Disclosure Project
        TCFD,         // Task Force on Climate-related Financial Disclosures
        ISO_14064,    // ISO standard for greenhouse gas accounting
        CUSTOM
    }

    public enum ReportStatus {
        DRAFT,
        PENDING_REVIEW,
        APPROVED,
        PUBLISHED,
        ARCHIVED
    }

    // ========== HELPER METHODS ==========

    /**
     * Calculate total emissions from all scopes
     */
    public BigDecimal calculateTotalEmissions() {
        BigDecimal total = BigDecimal.ZERO;
        if (scope1EmissionsKg != null) total = total.add(scope1EmissionsKg);
        if (scope2EmissionsKg != null) total = total.add(scope2EmissionsKg);
        if (scope3EmissionsKg != null) total = total.add(scope3EmissionsKg);
        this.totalEmissionsKg = total;
        return total;
    }

    /**
     * Calculate carbon savings compared to ICE baseline
     */
    public BigDecimal calculateCarbonSavings() {
        if (baselineIceEmissionsKg == null || totalEmissionsKg == null) {
            return BigDecimal.ZERO;
        }
        this.carbonSavingsKg = baselineIceEmissionsKg.subtract(totalEmissionsKg);
        return this.carbonSavingsKg;
    }

    /**
     * Calculate emissions reduction percentage
     */
    public BigDecimal calculateEmissionsReductionPercent() {
        if (baselineIceEmissionsKg == null || baselineIceEmissionsKg.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        calculateCarbonSavings();
        this.emissionsReductionPercent = carbonSavingsKg
                .multiply(BigDecimal.valueOf(100))
                .divide(baselineIceEmissionsKg, 2, java.math.RoundingMode.HALF_UP);
        return this.emissionsReductionPercent;
    }

    /**
     * Calculate electrification percentage
     */
    public BigDecimal calculateElectrificationPercent() {
        int evCount = evVehicleCount != null ? evVehicleCount : 0;
        int iceCount = iceVehicleCount != null ? iceVehicleCount : 0;
        int hybridCount = hybridVehicleCount != null ? hybridVehicleCount : 0;
        int total = evCount + iceCount + hybridCount;
        
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        
        // EVs count 100%, Hybrids count 50%
        double electrifiedEquivalent = evCount + (hybridCount * 0.5);
        this.electrificationPercent = BigDecimal.valueOf(electrifiedEquivalent * 100 / total)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return this.electrificationPercent;
    }
}
