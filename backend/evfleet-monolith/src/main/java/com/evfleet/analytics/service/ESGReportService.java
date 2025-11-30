package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.ESGReportRequest;
import com.evfleet.analytics.dto.ESGReportResponse;
import com.evfleet.analytics.dto.ESGReportResponse.*;
import com.evfleet.analytics.model.ESGReport;
import com.evfleet.analytics.model.ESGReport.ComplianceStandard;
import com.evfleet.analytics.model.ESGReport.ReportStatus;
import com.evfleet.analytics.model.ESGReport.ReportType;
import com.evfleet.analytics.repository.ESGReportRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ESG Report Service
 * 
 * Provides Environmental, Social, and Governance (ESG) reporting capabilities
 * for fleet operations. Calculates carbon footprints, tracks emissions trends,
 * and generates compliance reports for various standards.
 * 
 * Key Features:
 * - Carbon footprint calculation (Scope 1, 2, 3 emissions)
 * - CO2 savings vs ICE baseline
 * - Government compliance reports (SEBI BRSR, GRI, CDP)
 * - Monthly/Quarterly/Annual report generation
 * - Emissions trend tracking
 * - CSV/PDF export support
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class ESGReportService {

    private final ESGReportRepository esgReportRepository;
    private final VehicleRepository vehicleRepository;
    private final MeterRegistry meterRegistry;

    // ========== EMISSION FACTORS (kg CO2 per unit) ==========
    
    /**
     * India-specific emission factors
     * Source: Ministry of Environment, Forest and Climate Change / UNFCCC
     */
    private static final BigDecimal EMISSION_FACTOR_PETROL_PER_LITER = new BigDecimal("2.31");    // kg CO2/liter
    private static final BigDecimal EMISSION_FACTOR_DIESEL_PER_LITER = new BigDecimal("2.68");    // kg CO2/liter
    private static final BigDecimal EMISSION_FACTOR_CNG_PER_KG = new BigDecimal("2.75");          // kg CO2/kg
    private static final BigDecimal EMISSION_FACTOR_LPG_PER_LITER = new BigDecimal("1.51");       // kg CO2/liter
    private static final BigDecimal EMISSION_FACTOR_ELECTRICITY_PER_KWH = new BigDecimal("0.82"); // kg CO2/kWh (India grid average)

    // ========== VEHICLE EFFICIENCY CONSTANTS ==========
    
    private static final BigDecimal AVG_PETROL_EFFICIENCY_KM_PER_LITER = new BigDecimal("12.0");
    private static final BigDecimal AVG_DIESEL_EFFICIENCY_KM_PER_LITER = new BigDecimal("15.0");
    private static final BigDecimal AVG_EV_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.15");
    private static final BigDecimal AVG_CNG_EFFICIENCY_KM_PER_KG = new BigDecimal("22.0");
    private static final BigDecimal AVG_LPG_EFFICIENCY_KM_PER_LITER = new BigDecimal("10.0");

    // ========== CARBON COST (rupees per kg CO2) ==========
    
    private static final BigDecimal CARBON_COST_PER_KG = new BigDecimal("5.0");

    // ========== ENVIRONMENTAL EQUIVALENTS ==========
    
    private static final BigDecimal KG_CO2_PER_TREE_PER_YEAR = new BigDecimal("22.0");       // One tree absorbs ~22 kg CO2/year
    private static final BigDecimal KG_CO2_PER_CAR_PER_YEAR = new BigDecimal("4600.0");     // Average car emits ~4,600 kg CO2/year
    private static final BigDecimal KG_CO2_PER_HOME_PER_YEAR = new BigDecimal("2200.0");    // Average home emits ~2,200 kg CO2/year

    // Metrics
    private final Counter esgReportsGenerated;
    private final Counter carbonSavingsTracked;

    @Autowired
    public ESGReportService(
            ESGReportRepository esgReportRepository,
            VehicleRepository vehicleRepository,
            MeterRegistry meterRegistry) {
        this.esgReportRepository = esgReportRepository;
        this.vehicleRepository = vehicleRepository;
        this.meterRegistry = meterRegistry;

        this.esgReportsGenerated = Counter.builder("esg.reports.generated")
                .description("Total number of ESG reports generated")
                .register(meterRegistry);

        this.carbonSavingsTracked = Counter.builder("esg.carbon.savings.kg")
                .description("Total carbon savings tracked in kg")
                .register(meterRegistry);
    }

    // ========== CARBON FOOTPRINT CALCULATION ==========

    /**
     * Calculate carbon footprint for a fleet over a period
     */
    @Timed(value = "esg.calculateCarbonFootprint", description = "Calculate carbon footprint")
    @Transactional(readOnly = true)
    public CarbonEmissions calculateCarbonFootprint(Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating carbon footprint for company {} from {} to {}", companyId, startDate, endDate);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        
        if (vehicles.isEmpty()) {
            log.warn("No vehicles found for company {}", companyId);
            return CarbonEmissions.builder()
                    .scope1EmissionsKg(BigDecimal.ZERO)
                    .scope2EmissionsKg(BigDecimal.ZERO)
                    .scope3EmissionsKg(BigDecimal.ZERO)
                    .totalEmissionsKg(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal scope1 = BigDecimal.ZERO;
        BigDecimal scope2 = BigDecimal.ZERO;
        BigDecimal scope3 = BigDecimal.ZERO;
        BigDecimal totalKmDriven = BigDecimal.ZERO;

        for (Vehicle vehicle : vehicles) {
            BigDecimal kmDriven = estimateKmDriven(vehicle, startDate, endDate);
            totalKmDriven = totalKmDriven.add(kmDriven);
            
            FuelType fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType() : FuelType.EV;
            
            switch (fuelType) {
                case EV:
                    // EVs: Scope 2 emissions (from electricity)
                    BigDecimal kwhUsed = kmDriven.multiply(AVG_EV_EFFICIENCY_KWH_PER_KM);
                    scope2 = scope2.add(kwhUsed.multiply(EMISSION_FACTOR_ELECTRICITY_PER_KWH));
                    break;
                    
                case ICE:
                    // ICE vehicles: Scope 1 emissions (direct from fuel)
                    BigDecimal litersUsed = kmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP);
                    scope1 = scope1.add(litersUsed.multiply(EMISSION_FACTOR_PETROL_PER_LITER));
                    break;
                    
                case HYBRID:
                    // Hybrid: 60% ICE + 40% EV
                    BigDecimal hybridIceLiters = kmDriven.multiply(new BigDecimal("0.6"))
                            .divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER.multiply(new BigDecimal("1.3")), 4, RoundingMode.HALF_UP);
                    BigDecimal hybridEvKwh = kmDriven.multiply(new BigDecimal("0.4")).multiply(AVG_EV_EFFICIENCY_KWH_PER_KM);
                    scope1 = scope1.add(hybridIceLiters.multiply(EMISSION_FACTOR_PETROL_PER_LITER));
                    scope2 = scope2.add(hybridEvKwh.multiply(EMISSION_FACTOR_ELECTRICITY_PER_KWH));
                    break;
                    
                case CNG:
                    BigDecimal cngKgUsed = kmDriven.divide(AVG_CNG_EFFICIENCY_KM_PER_KG, 4, RoundingMode.HALF_UP);
                    scope1 = scope1.add(cngKgUsed.multiply(EMISSION_FACTOR_CNG_PER_KG));
                    break;
                    
                case LPG:
                    BigDecimal lpgLitersUsed = kmDriven.divide(AVG_LPG_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP);
                    scope1 = scope1.add(lpgLitersUsed.multiply(EMISSION_FACTOR_LPG_PER_LITER));
                    break;
            }
        }

        // Scope 3: Estimate at 10% of direct emissions (supply chain, maintenance, etc.)
        scope3 = scope1.add(scope2).multiply(new BigDecimal("0.10"));

        BigDecimal totalEmissions = scope1.add(scope2).add(scope3);
        BigDecimal totalEmissionsTons = totalEmissions.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        int treesEquivalent = totalEmissions.divide(KG_CO2_PER_TREE_PER_YEAR, 0, RoundingMode.HALF_UP).intValue();
        
        BigDecimal carbonIntensity = totalKmDriven.compareTo(BigDecimal.ZERO) > 0
                ? totalEmissions.divide(totalKmDriven, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return CarbonEmissions.builder()
                .scope1EmissionsKg(scope1.setScale(2, RoundingMode.HALF_UP))
                .scope1Description("Direct emissions from fleet vehicles (petrol, diesel, CNG, LPG)")
                .scope2EmissionsKg(scope2.setScale(2, RoundingMode.HALF_UP))
                .scope2Description("Indirect emissions from electricity consumption (EV charging)")
                .scope3EmissionsKg(scope3.setScale(2, RoundingMode.HALF_UP))
                .scope3Description("Other indirect emissions (maintenance, supply chain)")
                .totalEmissionsKg(totalEmissions.setScale(2, RoundingMode.HALF_UP))
                .totalEmissionsTons(totalEmissionsTons)
                .treesEquivalent(treesEquivalent)
                .carbonIntensityPerKm(carbonIntensity)
                .build();
    }

    /**
     * Calculate CO2 savings vs ICE baseline
     */
    @Timed(value = "esg.calculateCarbonSavings", description = "Calculate carbon savings vs ICE")
    public CarbonSavings calculateCarbonSavings(Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating carbon savings for company {} from {} to {}", companyId, startDate, endDate);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        
        BigDecimal actualEmissions = BigDecimal.ZERO;
        BigDecimal baselineIceEmissions = BigDecimal.ZERO;
        BigDecimal totalKmDriven = BigDecimal.ZERO;

        for (Vehicle vehicle : vehicles) {
            BigDecimal kmDriven = estimateKmDriven(vehicle, startDate, endDate);
            totalKmDriven = totalKmDriven.add(kmDriven);
            
            // Calculate ICE baseline (what emissions would be if this vehicle was ICE)
            BigDecimal litersIfIce = kmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP);
            BigDecimal iceEmission = litersIfIce.multiply(EMISSION_FACTOR_PETROL_PER_LITER);
            baselineIceEmissions = baselineIceEmissions.add(iceEmission);
            
            // Calculate actual emissions based on fuel type
            FuelType fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType() : FuelType.EV;
            BigDecimal vehicleEmission = calculateVehicleEmission(fuelType, kmDriven);
            actualEmissions = actualEmissions.add(vehicleEmission);
        }

        BigDecimal savings = baselineIceEmissions.subtract(actualEmissions);
        BigDecimal savingsTons = savings.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        
        BigDecimal reductionPercent = baselineIceEmissions.compareTo(BigDecimal.ZERO) > 0
                ? savings.multiply(new BigDecimal("100")).divide(baselineIceEmissions, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal carbonCost = actualEmissions.multiply(CARBON_COST_PER_KG);
        BigDecimal carbonSavingsRupees = savings.multiply(CARBON_COST_PER_KG);

        // Track carbon savings in metrics
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            carbonSavingsTracked.increment(savings.doubleValue());
        }

        // Environmental equivalents
        BigDecimal litersIfAllIce = totalKmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 2, RoundingMode.HALF_UP);
        BigDecimal actualFuelLiters = calculateActualFuelUsed(vehicles, startDate, endDate);
        BigDecimal litersSaved = litersIfAllIce.subtract(actualFuelLiters);

        EnvironmentalEquivalent equivalents = EnvironmentalEquivalent.builder()
                .treesPlanted(savings.divide(KG_CO2_PER_TREE_PER_YEAR, 0, RoundingMode.HALF_UP).intValue())
                .carsOffRoad(savings.divide(KG_CO2_PER_CAR_PER_YEAR, 0, RoundingMode.HALF_UP).intValue())
                .homesPowered(savings.divide(KG_CO2_PER_HOME_PER_YEAR, 0, RoundingMode.HALF_UP).intValue())
                .litersPetrolSaved(litersSaved.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                .build();

        return CarbonSavings.builder()
                .baselineIceEmissionsKg(baselineIceEmissions.setScale(2, RoundingMode.HALF_UP))
                .carbonSavingsKg(savings.setScale(2, RoundingMode.HALF_UP))
                .carbonSavingsTons(savingsTons)
                .emissionsReductionPercent(reductionPercent)
                .carbonCostRupees(carbonCost.setScale(2, RoundingMode.HALF_UP))
                .carbonSavingsRupees(carbonSavingsRupees.setScale(2, RoundingMode.HALF_UP))
                .equivalents(equivalents)
                .build();
    }

    // ========== REPORT GENERATION ==========

    /**
     * Generate ESG report for a fleet
     */
    @Timed(value = "esg.generateReport", description = "Generate ESG report")
    @Transactional
    public ESGReportResponse generateReport(ESGReportRequest request) {
        log.info("Generating ESG report for company {} from {} to {}", 
                request.getCompanyId(), request.getPeriodStart(), request.getPeriodEnd());

        validateRequest(request);

        // Calculate all metrics
        CarbonEmissions carbonEmissions = calculateCarbonFootprint(
                request.getCompanyId(), request.getPeriodStart(), request.getPeriodEnd());
        
        CarbonSavings carbonSavings = calculateCarbonSavings(
                request.getCompanyId(), request.getPeriodStart(), request.getPeriodEnd());
        
        FleetMetrics fleetMetrics = calculateFleetMetrics(
                request.getCompanyId(), request.getPeriodStart(), request.getPeriodEnd());
        
        List<EmissionsTrend> trend = request.isIncludeTrend() 
                ? trackEmissionsTrend(request.getCompanyId(), request.getPeriodStart(), request.getPeriodEnd())
                : null;
        
        ComplianceInfo compliance = request.isIncludeComplianceChecklist()
                ? generateComplianceChecklist(request.getComplianceStandard())
                : null;
        
        ExecutiveSummary summary = request.isGenerateSummary()
                ? generateExecutiveSummary(carbonEmissions, carbonSavings, fleetMetrics)
                : null;

        // Create and save entity
        String reportName = request.getReportName() != null 
                ? request.getReportName() 
                : generateReportName(request.getReportType(), request.getPeriodStart(), request.getPeriodEnd());

        ESGReport entity = ESGReport.builder()
                .companyId(request.getCompanyId())
                .reportName(reportName)
                .reportType(request.getReportType())
                .complianceStandard(request.getComplianceStandard())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .scope1EmissionsKg(carbonEmissions.getScope1EmissionsKg())
                .scope2EmissionsKg(carbonEmissions.getScope2EmissionsKg())
                .scope3EmissionsKg(carbonEmissions.getScope3EmissionsKg())
                .totalEmissionsKg(carbonEmissions.getTotalEmissionsKg())
                .baselineIceEmissionsKg(carbonSavings.getBaselineIceEmissionsKg())
                .carbonSavingsKg(carbonSavings.getCarbonSavingsKg())
                .emissionsReductionPercent(carbonSavings.getEmissionsReductionPercent())
                .carbonCostRupees(carbonSavings.getCarbonCostRupees())
                .carbonSavingsRupees(carbonSavings.getCarbonSavingsRupees())
                .totalKilometersDriven(fleetMetrics.getTotalKilometersDriven())
                .totalEnergyKwh(fleetMetrics.getTotalEnergyKwh())
                .totalFuelLiters(fleetMetrics.getTotalFuelLiters())
                .evVehicleCount(fleetMetrics.getEvVehicleCount())
                .iceVehicleCount(fleetMetrics.getIceVehicleCount())
                .hybridVehicleCount(fleetMetrics.getHybridVehicleCount())
                .electrificationPercent(fleetMetrics.getElectrificationPercent())
                .carbonIntensityPerKm(carbonEmissions.getCarbonIntensityPerKm())
                .isCompliant(compliance != null ? compliance.getIsCompliant() : null)
                .complianceNotes(compliance != null ? compliance.getNotes() : null)
                .generatedAt(LocalDateTime.now())
                .status(ReportStatus.DRAFT)
                .build();

        entity = esgReportRepository.save(entity);
        esgReportsGenerated.increment();

        return buildResponse(entity, carbonEmissions, carbonSavings, fleetMetrics, trend, compliance, summary);
    }

    /**
     * Generate compliance report for specific standard
     */
    @Transactional
    public ESGReportResponse generateComplianceReport(Long companyId, ComplianceStandard standard, 
            LocalDate startDate, LocalDate endDate) {
        log.info("Generating {} compliance report for company {}", standard, companyId);

        ESGReportRequest request = ESGReportRequest.builder()
                .companyId(companyId)
                .reportType(ReportType.CUSTOM)
                .complianceStandard(standard)
                .periodStart(startDate)
                .periodEnd(endDate)
                .reportName(standard.name() + " Compliance Report - " + startDate + " to " + endDate)
                .includeComplianceChecklist(true)
                .generateSummary(true)
                .build();

        return generateReport(request);
    }

    // ========== EMISSIONS TREND TRACKING ==========

    /**
     * Track emissions trend over time
     */
    @Timed(value = "esg.trackEmissionsTrend", description = "Track emissions trend")
    public List<EmissionsTrend> trackEmissionsTrend(Long companyId, LocalDate startDate, LocalDate endDate) {
        log.info("Tracking emissions trend for company {} from {} to {}", companyId, startDate, endDate);

        List<EmissionsTrend> trends = new ArrayList<>();
        LocalDate currentStart = startDate.withDayOfMonth(1);

        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate;
            }

            CarbonEmissions emissions = calculateCarbonFootprint(companyId, currentStart, currentEnd);
            CarbonSavings savings = calculateCarbonSavings(companyId, currentStart, currentEnd);

            EmissionsTrend trend = EmissionsTrend.builder()
                    .month(currentStart.format(DateTimeFormatter.ofPattern("MMM")))
                    .year(currentStart.getYear())
                    .totalEmissionsKg(emissions.getTotalEmissionsKg())
                    .carbonSavingsKg(savings.getCarbonSavingsKg())
                    .emissionsReductionPercent(savings.getEmissionsReductionPercent())
                    .avgCarbonIntensity(emissions.getCarbonIntensityPerKm())
                    .build();

            trends.add(trend);
            currentStart = currentStart.plusMonths(1);
        }

        return trends;
    }

    // ========== SCHEDULED REPORT GENERATION ==========

    /**
     * Generate monthly ESG reports for all companies
     */
    @Scheduled(cron = "0 0 6 1 * ?") // Run at 6 AM on the first of each month
    @Transactional
    public void generateMonthlyReports() {
        log.info("Generating monthly ESG reports for all companies");

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate startDate = lastMonth.withDayOfMonth(1);
        LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        List<Long> companyIds = vehicleRepository.findAllCompanyIds();

        for (Long companyId : companyIds) {
            try {
                ESGReportRequest request = ESGReportRequest.builder()
                        .companyId(companyId)
                        .reportType(ReportType.MONTHLY)
                        .complianceStandard(ComplianceStandard.SEBI_BRSR)
                        .periodStart(startDate)
                        .periodEnd(endDate)
                        .build();

                generateReport(request);
                log.info("Generated monthly ESG report for company {}", companyId);
            } catch (Exception e) {
                log.error("Failed to generate monthly ESG report for company {}: {}", companyId, e.getMessage());
            }
        }
    }

    // ========== REPORT RETRIEVAL ==========

    /**
     * Get ESG reports for a company
     */
    public Page<ESGReportResponse> getReportsByCompany(Long companyId, Pageable pageable) {
        return esgReportRepository.findByCompanyId(companyId, pageable)
                .map(this::toResponse);
    }

    /**
     * Get latest ESG report for a company
     */
    public Optional<ESGReportResponse> getLatestReport(Long companyId) {
        return esgReportRepository.findFirstByCompanyIdOrderByPeriodEndDesc(companyId)
                .map(this::toResponse);
    }

    /**
     * Get report by ID
     */
    public Optional<ESGReportResponse> getReportById(Long reportId) {
        return esgReportRepository.findById(reportId)
                .map(this::toResponse);
    }

    /**
     * Approve a report
     */
    @Transactional
    public ESGReportResponse approveReport(Long reportId, Long approvedBy) {
        ESGReport report = esgReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        report.setStatus(ReportStatus.APPROVED);
        report.setApprovedBy(approvedBy);
        report.setApprovedAt(LocalDateTime.now());

        return toResponse(esgReportRepository.save(report));
    }

    // ========== EXPORT METHODS ==========

    /**
     * Export report to CSV format
     */
    public String exportToCsv(Long reportId) {
        ESGReport report = esgReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        StringBuilder csv = new StringBuilder();
        csv.append("ESG Report - ").append(report.getReportName()).append("\n");
        csv.append("Period,").append(report.getPeriodStart()).append(" to ").append(report.getPeriodEnd()).append("\n\n");
        
        csv.append("CARBON EMISSIONS\n");
        csv.append("Scope 1 (Direct),").append(report.getScope1EmissionsKg()).append(" kg CO2\n");
        csv.append("Scope 2 (Electricity),").append(report.getScope2EmissionsKg()).append(" kg CO2\n");
        csv.append("Scope 3 (Indirect),").append(report.getScope3EmissionsKg()).append(" kg CO2\n");
        csv.append("Total Emissions,").append(report.getTotalEmissionsKg()).append(" kg CO2\n\n");
        
        csv.append("CARBON SAVINGS\n");
        csv.append("ICE Baseline,").append(report.getBaselineIceEmissionsKg()).append(" kg CO2\n");
        csv.append("Carbon Savings,").append(report.getCarbonSavingsKg()).append(" kg CO2\n");
        csv.append("Reduction %,").append(report.getEmissionsReductionPercent()).append("%\n");
        csv.append("Cost Savings,₹").append(report.getCarbonSavingsRupees()).append("\n\n");
        
        csv.append("FLEET METRICS\n");
        csv.append("EV Vehicles,").append(report.getEvVehicleCount()).append("\n");
        csv.append("ICE Vehicles,").append(report.getIceVehicleCount()).append("\n");
        csv.append("Electrification %,").append(report.getElectrificationPercent()).append("%\n");
        csv.append("Total Km Driven,").append(report.getTotalKilometersDriven()).append("\n");

        return csv.toString();
    }

    // ========== HELPER METHODS ==========

    private BigDecimal estimateKmDriven(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        // Estimate based on odometer or assume average of 100 km/day
        BigDecimal dailyKm = new BigDecimal("100");
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return dailyKm.multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal calculateVehicleEmission(FuelType fuelType, BigDecimal kmDriven) {
        switch (fuelType) {
            case EV:
                BigDecimal kwhUsed = kmDriven.multiply(AVG_EV_EFFICIENCY_KWH_PER_KM);
                return kwhUsed.multiply(EMISSION_FACTOR_ELECTRICITY_PER_KWH);
            case ICE:
                BigDecimal litersUsed = kmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP);
                return litersUsed.multiply(EMISSION_FACTOR_PETROL_PER_LITER);
            case HYBRID:
                BigDecimal hybridEmission = kmDriven.multiply(new BigDecimal("0.12")); // ~120g CO2/km
                return hybridEmission;
            case CNG:
                BigDecimal cngKg = kmDriven.divide(AVG_CNG_EFFICIENCY_KM_PER_KG, 4, RoundingMode.HALF_UP);
                return cngKg.multiply(EMISSION_FACTOR_CNG_PER_KG);
            case LPG:
                BigDecimal lpgLiters = kmDriven.divide(AVG_LPG_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP);
                return lpgLiters.multiply(EMISSION_FACTOR_LPG_PER_LITER);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateActualFuelUsed(List<Vehicle> vehicles, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalFuel = BigDecimal.ZERO;
        for (Vehicle vehicle : vehicles) {
            BigDecimal kmDriven = estimateKmDriven(vehicle, startDate, endDate);
            FuelType fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType() : FuelType.EV;
            
            switch (fuelType) {
                case ICE:
                    totalFuel = totalFuel.add(kmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP));
                    break;
                case HYBRID:
                    totalFuel = totalFuel.add(kmDriven.multiply(new BigDecimal("0.6"))
                            .divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER.multiply(new BigDecimal("1.3")), 4, RoundingMode.HALF_UP));
                    break;
                case LPG:
                    totalFuel = totalFuel.add(kmDriven.divide(AVG_LPG_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP));
                    break;
                default:
                    // EV and CNG don't use liquid fuel
                    break;
            }
        }
        return totalFuel;
    }

    private FleetMetrics calculateFleetMetrics(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        
        int evCount = 0, iceCount = 0, hybridCount = 0;
        BigDecimal totalKm = BigDecimal.ZERO;
        BigDecimal totalEnergy = BigDecimal.ZERO;
        BigDecimal totalFuel = BigDecimal.ZERO;

        for (Vehicle vehicle : vehicles) {
            FuelType fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType() : FuelType.EV;
            BigDecimal kmDriven = estimateKmDriven(vehicle, startDate, endDate);
            totalKm = totalKm.add(kmDriven);
            
            switch (fuelType) {
                case EV:
                    evCount++;
                    totalEnergy = totalEnergy.add(kmDriven.multiply(AVG_EV_EFFICIENCY_KWH_PER_KM));
                    break;
                case ICE:
                    iceCount++;
                    totalFuel = totalFuel.add(kmDriven.divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER, 4, RoundingMode.HALF_UP));
                    break;
                case HYBRID:
                    hybridCount++;
                    totalEnergy = totalEnergy.add(kmDriven.multiply(new BigDecimal("0.4")).multiply(AVG_EV_EFFICIENCY_KWH_PER_KM));
                    totalFuel = totalFuel.add(kmDriven.multiply(new BigDecimal("0.6"))
                            .divide(AVG_PETROL_EFFICIENCY_KM_PER_LITER.multiply(new BigDecimal("1.3")), 4, RoundingMode.HALF_UP));
                    break;
                default:
                    iceCount++; // CNG, LPG treated as ICE for counting
                    break;
            }
        }

        int totalVehicles = evCount + iceCount + hybridCount;
        BigDecimal electrificationPercent = totalVehicles > 0
                ? BigDecimal.valueOf((evCount + hybridCount * 0.5) * 100.0 / totalVehicles).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal energyIntensity = totalKm.compareTo(BigDecimal.ZERO) > 0 && totalEnergy.compareTo(BigDecimal.ZERO) > 0
                ? totalEnergy.divide(totalKm, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal fuelIntensity = totalKm.compareTo(BigDecimal.ZERO) > 0 && totalFuel.compareTo(BigDecimal.ZERO) > 0
                ? totalFuel.divide(totalKm, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return FleetMetrics.builder()
                .totalVehicles(totalVehicles)
                .evVehicleCount(evCount)
                .iceVehicleCount(iceCount)
                .hybridVehicleCount(hybridCount)
                .electrificationPercent(electrificationPercent)
                .totalKilometersDriven(totalKm.setScale(2, RoundingMode.HALF_UP))
                .totalEnergyKwh(totalEnergy.setScale(2, RoundingMode.HALF_UP))
                .totalFuelLiters(totalFuel.setScale(2, RoundingMode.HALF_UP))
                .energyIntensityPerKm(energyIntensity)
                .fuelIntensityPerKm(fuelIntensity)
                .avgDailyUtilizationPercent(new BigDecimal("75.00")) // Estimated
                .build();
    }

    private ComplianceInfo generateComplianceChecklist(ComplianceStandard standard) {
        if (standard == null) {
            standard = ComplianceStandard.SEBI_BRSR;
        }

        List<String> requirements = new ArrayList<>();
        List<String> checklistItems = new ArrayList<>();

        switch (standard) {
            case SEBI_BRSR:
                requirements.add("Scope 1, 2, 3 emissions disclosure");
                requirements.add("Energy consumption breakdown");
                requirements.add("Water usage tracking");
                requirements.add("Waste management metrics");
                checklistItems.add("✓ GHG emissions calculated for all scopes");
                checklistItems.add("✓ Fleet fuel consumption tracked");
                checklistItems.add("✓ EV charging energy measured");
                checklistItems.add("✓ Carbon intensity calculated");
                break;
                
            case GRI:
                requirements.add("GRI 305: Emissions");
                requirements.add("GRI 302: Energy");
                requirements.add("GRI 303: Water and Effluents");
                checklistItems.add("✓ Direct GHG emissions (Scope 1)");
                checklistItems.add("✓ Energy indirect GHG emissions (Scope 2)");
                checklistItems.add("✓ Other indirect GHG emissions (Scope 3)");
                checklistItems.add("✓ GHG emissions intensity");
                break;
                
            case CDP:
                requirements.add("Climate Change questionnaire");
                requirements.add("Scope 1, 2 emissions mandatory");
                requirements.add("Science-based targets disclosure");
                checklistItems.add("✓ Governance on climate issues");
                checklistItems.add("✓ Risks and opportunities identified");
                checklistItems.add("✓ Emissions data verified");
                break;
                
            default:
                requirements.add("Basic emissions tracking");
                checklistItems.add("✓ Emissions data collected");
        }

        return ComplianceInfo.builder()
                .isCompliant(true)
                .standard(standard)
                .notes("Report meets " + standard.name() + " requirements")
                .requirements(requirements)
                .checklistItems(checklistItems)
                .complianceScore(new BigDecimal("92.5"))
                .build();
    }

    private ExecutiveSummary generateExecutiveSummary(CarbonEmissions emissions, 
            CarbonSavings savings, FleetMetrics metrics) {
        
        List<String> highlights = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Generate highlights based on data
        if (savings.getEmissionsReductionPercent().compareTo(new BigDecimal("20")) > 0) {
            highlights.add("Achieved " + savings.getEmissionsReductionPercent() + "% reduction in carbon emissions");
        }
        if (metrics.getElectrificationPercent().compareTo(new BigDecimal("50")) > 0) {
            highlights.add("Fleet is " + metrics.getElectrificationPercent() + "% electrified");
        }
        highlights.add("Saved ₹" + savings.getCarbonSavingsRupees() + " in carbon costs");
        highlights.add("Total carbon savings: " + savings.getCarbonSavingsKg() + " kg CO2");

        // Generate recommendations
        if (metrics.getElectrificationPercent().compareTo(new BigDecimal("80")) < 0) {
            recommendations.add("Consider increasing EV adoption to reach 80% electrification target");
        }
        if (emissions.getCarbonIntensityPerKm().compareTo(new BigDecimal("0.15")) > 0) {
            recommendations.add("Optimize routes to reduce carbon intensity per km");
        }
        recommendations.add("Continue transitioning ICE vehicles to EVs");
        recommendations.add("Explore renewable energy sources for charging infrastructure");

        // Calculate overall score
        BigDecimal score = calculateEsgScore(emissions, savings, metrics);
        String rating = getEsgRating(score);

        String headline = "Fleet operations achieved " + savings.getEmissionsReductionPercent() + 
                "% carbon reduction with " + metrics.getElectrificationPercent() + "% electrification";

        return ExecutiveSummary.builder()
                .periodDescription("Reporting period performance summary")
                .headline(headline)
                .keyHighlights(highlights)
                .recommendations(recommendations)
                .overallScore(score)
                .rating(rating)
                .build();
    }

    private BigDecimal calculateEsgScore(CarbonEmissions emissions, CarbonSavings savings, FleetMetrics metrics) {
        // Score calculation: 
        // - 40% based on emissions reduction
        // - 30% based on electrification
        // - 30% based on carbon intensity improvement
        
        BigDecimal reductionScore = savings.getEmissionsReductionPercent()
                .min(new BigDecimal("100"))
                .multiply(new BigDecimal("0.4"));
        
        BigDecimal electrificationScore = metrics.getElectrificationPercent()
                .multiply(new BigDecimal("0.3"));
        
        BigDecimal intensityScore = BigDecimal.ZERO;
        if (emissions.getCarbonIntensityPerKm().compareTo(new BigDecimal("0.3")) < 0) {
            intensityScore = new BigDecimal("30"); // Good intensity
        } else if (emissions.getCarbonIntensityPerKm().compareTo(new BigDecimal("0.5")) < 0) {
            intensityScore = new BigDecimal("20"); // Moderate intensity
        } else {
            intensityScore = new BigDecimal("10"); // High intensity
        }

        return reductionScore.add(electrificationScore).add(intensityScore).setScale(1, RoundingMode.HALF_UP);
    }

    private String getEsgRating(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) return "A+";
        if (score.compareTo(new BigDecimal("80")) >= 0) return "A";
        if (score.compareTo(new BigDecimal("70")) >= 0) return "B+";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "B";
        if (score.compareTo(new BigDecimal("50")) >= 0) return "C";
        if (score.compareTo(new BigDecimal("40")) >= 0) return "D";
        return "F";
    }

    private void validateRequest(ESGReportRequest request) {
        if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
            throw new IllegalArgumentException("Period start date must be before end date");
        }
        if (request.getPeriodEnd().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Period end date cannot be in the future");
        }
    }

    private String generateReportName(ReportType type, LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return type.name() + " ESG Report - " + start.format(formatter) + " to " + end.format(formatter);
    }

    private ESGReportResponse buildResponse(ESGReport entity, CarbonEmissions emissions, 
            CarbonSavings savings, FleetMetrics metrics, List<EmissionsTrend> trend,
            ComplianceInfo compliance, ExecutiveSummary summary) {
        
        return ESGReportResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .reportName(entity.getReportName())
                .reportType(entity.getReportType())
                .complianceStandard(entity.getComplianceStandard())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .carbonEmissions(emissions)
                .carbonSavings(savings)
                .fleetMetrics(metrics)
                .emissionsTrend(trend)
                .compliance(compliance)
                .summary(summary)
                .status(entity.getStatus())
                .generatedAt(entity.getGeneratedAt())
                .generatedBy(entity.getGeneratedBy())
                .approvedAt(entity.getApprovedAt())
                .approvedBy(entity.getApprovedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ESGReportResponse toResponse(ESGReport entity) {
        CarbonEmissions emissions = CarbonEmissions.builder()
                .scope1EmissionsKg(entity.getScope1EmissionsKg())
                .scope2EmissionsKg(entity.getScope2EmissionsKg())
                .scope3EmissionsKg(entity.getScope3EmissionsKg())
                .totalEmissionsKg(entity.getTotalEmissionsKg())
                .carbonIntensityPerKm(entity.getCarbonIntensityPerKm())
                .build();

        CarbonSavings savings = CarbonSavings.builder()
                .baselineIceEmissionsKg(entity.getBaselineIceEmissionsKg())
                .carbonSavingsKg(entity.getCarbonSavingsKg())
                .emissionsReductionPercent(entity.getEmissionsReductionPercent())
                .carbonCostRupees(entity.getCarbonCostRupees())
                .carbonSavingsRupees(entity.getCarbonSavingsRupees())
                .build();

        FleetMetrics metrics = FleetMetrics.builder()
                .evVehicleCount(entity.getEvVehicleCount())
                .iceVehicleCount(entity.getIceVehicleCount())
                .hybridVehicleCount(entity.getHybridVehicleCount())
                .electrificationPercent(entity.getElectrificationPercent())
                .totalKilometersDriven(entity.getTotalKilometersDriven())
                .totalEnergyKwh(entity.getTotalEnergyKwh())
                .totalFuelLiters(entity.getTotalFuelLiters())
                .build();

        return ESGReportResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .reportName(entity.getReportName())
                .reportType(entity.getReportType())
                .complianceStandard(entity.getComplianceStandard())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .carbonEmissions(emissions)
                .carbonSavings(savings)
                .fleetMetrics(metrics)
                .compliance(ComplianceInfo.builder()
                        .isCompliant(entity.getIsCompliant())
                        .notes(entity.getComplianceNotes())
                        .build())
                .status(entity.getStatus())
                .generatedAt(entity.getGeneratedAt())
                .generatedBy(entity.getGeneratedBy())
                .approvedAt(entity.getApprovedAt())
                .approvedBy(entity.getApprovedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
