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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ESGReportService
 * 
 * Tests carbon footprint calculation, carbon savings, emissions tracking,
 * report generation, and compliance reporting.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ESGReportServiceTest {

    @Mock
    private ESGReportRepository esgReportRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private MeterRegistry meterRegistry;
    private ESGReportService esgReportService;

    private static final Long COMPANY_ID = 1L;
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 1, 31);

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        esgReportService = new ESGReportService(
                esgReportRepository,
                vehicleRepository,
                meterRegistry
        );
    }

    // ========== CARBON FOOTPRINT TESTS ==========

    @Nested
    @DisplayName("Carbon Footprint Calculation Tests")
    class CarbonFootprintTests {

        @Test
        @DisplayName("Should calculate zero emissions for empty fleet")
        void shouldCalculateZeroEmissionsForEmptyFleet() {
            // Given
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions).isNotNull();
            assertThat(emissions.getScope1EmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emissions.getScope2EmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emissions.getScope3EmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emissions.getTotalEmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate Scope 2 emissions for EV fleet")
        void shouldCalculateScope2EmissionsForEvFleet() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions.getScope2EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            // EV should have minimal Scope 1 emissions
            assertThat(emissions.getScope1EmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emissions.getTotalEmissionsKg()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate Scope 1 emissions for ICE fleet")
        void shouldCalculateScope1EmissionsForIceFleet() {
            // Given
            Vehicle iceVehicle = createVehicle(1L, FuelType.ICE, "ICE001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(iceVehicle));

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions.getScope1EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            // ICE should have zero Scope 2 emissions (no electricity)
            assertThat(emissions.getScope2EmissionsKg()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(emissions.getTotalEmissionsKg()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate both Scope 1 and 2 for mixed fleet")
        void shouldCalculateMixedScopesForMixedFleet() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            Vehicle iceVehicle = createVehicle(2L, FuelType.ICE, "ICE001");
            Vehicle hybridVehicle = createVehicle(3L, FuelType.HYBRID, "HYB001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID))
                    .thenReturn(List.of(evVehicle, iceVehicle, hybridVehicle));

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions.getScope1EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(emissions.getScope2EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(emissions.getScope3EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(emissions.getTotalEmissionsKg())
                    .isEqualByComparingTo(emissions.getScope1EmissionsKg()
                            .add(emissions.getScope2EmissionsKg())
                            .add(emissions.getScope3EmissionsKg()));
        }

        @Test
        @DisplayName("Should calculate carbon intensity per km")
        void shouldCalculateCarbonIntensity() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions.getCarbonIntensityPerKm()).isNotNull();
            assertThat(emissions.getCarbonIntensityPerKm()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate emissions for CNG and LPG vehicles")
        void shouldCalculateEmissionsForAlternativeFuels() {
            // Given
            Vehicle cngVehicle = createVehicle(1L, FuelType.CNG, "CNG001");
            Vehicle lpgVehicle = createVehicle(2L, FuelType.LPG, "LPG001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID))
                    .thenReturn(List.of(cngVehicle, lpgVehicle));

            // When
            CarbonEmissions emissions = esgReportService.calculateCarbonFootprint(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(emissions.getScope1EmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(emissions.getTotalEmissionsKg()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    // ========== CARBON SAVINGS TESTS ==========

    @Nested
    @DisplayName("Carbon Savings Calculation Tests")
    class CarbonSavingsTests {

        @Test
        @DisplayName("Should calculate carbon savings for EV fleet vs ICE baseline")
        void shouldCalculateCarbonSavingsForEvFleet() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));

            // When
            CarbonSavings savings = esgReportService.calculateCarbonSavings(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(savings.getBaselineIceEmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(savings.getCarbonSavingsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(savings.getEmissionsReductionPercent()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate zero savings for pure ICE fleet")
        void shouldCalculateZeroSavingsForIceFleet() {
            // Given
            Vehicle iceVehicle = createVehicle(1L, FuelType.ICE, "ICE001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(iceVehicle));

            // When
            CarbonSavings savings = esgReportService.calculateCarbonSavings(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            // ICE fleet should have zero or negative savings vs ICE baseline
            assertThat(savings.getBaselineIceEmissionsKg()).isGreaterThan(BigDecimal.ZERO);
            assertThat(savings.getEmissionsReductionPercent())
                    .isLessThanOrEqualTo(new BigDecimal("5")); // Close to zero
        }

        @Test
        @DisplayName("Should calculate carbon cost in rupees")
        void shouldCalculateCarbonCostInRupees() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));

            // When
            CarbonSavings savings = esgReportService.calculateCarbonSavings(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(savings.getCarbonCostRupees()).isNotNull();
            assertThat(savings.getCarbonSavingsRupees()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate environmental equivalents")
        void shouldCalculateEnvironmentalEquivalents() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));

            // When
            CarbonSavings savings = esgReportService.calculateCarbonSavings(
                    COMPANY_ID, START_DATE, END_DATE);

            // Then
            assertThat(savings.getEquivalents()).isNotNull();
            assertThat(savings.getEquivalents().getTreesPlanted()).isGreaterThanOrEqualTo(0);
            assertThat(savings.getEquivalents().getLitersPetrolSaved()).isNotNull();
        }
    }

    // ========== REPORT GENERATION TESTS ==========

    @Nested
    @DisplayName("Report Generation Tests")
    class ReportGenerationTests {

        @Test
        @DisplayName("Should generate monthly ESG report")
        void shouldGenerateMonthlyEsgReport() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ESGReportRequest request = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.MONTHLY)
                    .complianceStandard(ComplianceStandard.SEBI_BRSR)
                    .periodStart(START_DATE)
                    .periodEnd(END_DATE)
                    .build();

            // When
            ESGReportResponse response = esgReportService.generateReport(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getReportType()).isEqualTo(ReportType.MONTHLY);
            assertThat(response.getCarbonEmissions()).isNotNull();
            assertThat(response.getCarbonSavings()).isNotNull();
            assertThat(response.getFleetMetrics()).isNotNull();
            assertThat(response.getStatus()).isEqualTo(ReportStatus.DRAFT);

            verify(esgReportRepository).save(any(ESGReport.class));
        }

        @Test
        @DisplayName("Should generate compliance report for SEBI BRSR")
        void shouldGenerateSebiBrsrComplianceReport() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // When
            ESGReportResponse response = esgReportService.generateComplianceReport(
                    COMPANY_ID, ComplianceStandard.SEBI_BRSR, START_DATE, END_DATE);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getComplianceStandard()).isEqualTo(ComplianceStandard.SEBI_BRSR);
            assertThat(response.getCompliance()).isNotNull();
            assertThat(response.getCompliance().getIsCompliant()).isTrue();
        }

        @Test
        @DisplayName("Should validate request dates")
        void shouldValidateRequestDates() {
            // Given
            ESGReportRequest invalidRequest = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.MONTHLY)
                    .periodStart(END_DATE) // Start after end
                    .periodEnd(START_DATE)
                    .build();

            // When/Then
            assertThatThrownBy(() -> esgReportService.generateReport(invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("before end date");
        }

        @Test
        @DisplayName("Should include emissions trend in report")
        void shouldIncludeEmissionsTrendInReport() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ESGReportRequest request = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.QUARTERLY)
                    .periodStart(LocalDate.of(2024, 1, 1))
                    .periodEnd(LocalDate.of(2024, 3, 31))
                    .includeTrend(true)
                    .build();

            // When
            ESGReportResponse response = esgReportService.generateReport(request);

            // Then
            assertThat(response.getEmissionsTrend()).isNotNull();
            assertThat(response.getEmissionsTrend()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should generate executive summary")
        void shouldGenerateExecutiveSummary() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(evVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ESGReportRequest request = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.MONTHLY)
                    .periodStart(START_DATE)
                    .periodEnd(END_DATE)
                    .generateSummary(true)
                    .build();

            // When
            ESGReportResponse response = esgReportService.generateReport(request);

            // Then
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getSummary().getHeadline()).isNotNull();
            assertThat(response.getSummary().getKeyHighlights()).isNotEmpty();
            assertThat(response.getSummary().getOverallScore()).isNotNull();
            assertThat(response.getSummary().getRating()).isNotNull();
        }
    }

    // ========== FLEET METRICS TESTS ==========

    @Nested
    @DisplayName("Fleet Metrics Tests")
    class FleetMetricsTests {

        @Test
        @DisplayName("Should calculate electrification percentage")
        void shouldCalculateElectrificationPercentage() {
            // Given
            Vehicle evVehicle1 = createVehicle(1L, FuelType.EV, "EV001");
            Vehicle evVehicle2 = createVehicle(2L, FuelType.EV, "EV002");
            Vehicle iceVehicle = createVehicle(3L, FuelType.ICE, "ICE001");
            Vehicle hybridVehicle = createVehicle(4L, FuelType.HYBRID, "HYB001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID))
                    .thenReturn(List.of(evVehicle1, evVehicle2, iceVehicle, hybridVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ESGReportRequest request = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.MONTHLY)
                    .periodStart(START_DATE)
                    .periodEnd(END_DATE)
                    .build();

            // When
            ESGReportResponse response = esgReportService.generateReport(request);

            // Then
            // 2 EVs + 0.5 * 1 Hybrid = 2.5 out of 4 = 62.5%
            assertThat(response.getFleetMetrics().getElectrificationPercent())
                    .isEqualByComparingTo(new BigDecimal("62.50"));
        }

        @Test
        @DisplayName("Should count vehicles by fuel type")
        void shouldCountVehiclesByFuelType() {
            // Given
            Vehicle evVehicle = createVehicle(1L, FuelType.EV, "EV001");
            Vehicle iceVehicle = createVehicle(2L, FuelType.ICE, "ICE001");
            Vehicle hybridVehicle = createVehicle(3L, FuelType.HYBRID, "HYB001");
            when(vehicleRepository.findByCompanyId(COMPANY_ID))
                    .thenReturn(List.of(evVehicle, iceVehicle, hybridVehicle));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> {
                ESGReport saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            ESGReportRequest request = ESGReportRequest.builder()
                    .companyId(COMPANY_ID)
                    .reportType(ReportType.MONTHLY)
                    .periodStart(START_DATE)
                    .periodEnd(END_DATE)
                    .build();

            // When
            ESGReportResponse response = esgReportService.generateReport(request);

            // Then
            assertThat(response.getFleetMetrics().getEvVehicleCount()).isEqualTo(1);
            assertThat(response.getFleetMetrics().getIceVehicleCount()).isEqualTo(1);
            assertThat(response.getFleetMetrics().getHybridVehicleCount()).isEqualTo(1);
            assertThat(response.getFleetMetrics().getTotalVehicles()).isEqualTo(3);
        }
    }

    // ========== REPORT RETRIEVAL TESTS ==========

    @Nested
    @DisplayName("Report Retrieval Tests")
    class ReportRetrievalTests {

        @Test
        @DisplayName("Should retrieve report by ID")
        void shouldRetrieveReportById() {
            // Given
            ESGReport report = createSampleReport();
            when(esgReportRepository.findById(1L)).thenReturn(Optional.of(report));

            // When
            Optional<ESGReportResponse> result = esgReportService.getReportById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should retrieve latest report for company")
        void shouldRetrieveLatestReportForCompany() {
            // Given
            ESGReport report = createSampleReport();
            when(esgReportRepository.findFirstByCompanyIdOrderByPeriodEndDesc(COMPANY_ID))
                    .thenReturn(Optional.of(report));

            // When
            Optional<ESGReportResponse> result = esgReportService.getLatestReport(COMPANY_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCompanyId()).isEqualTo(COMPANY_ID);
        }

        @Test
        @DisplayName("Should approve report")
        void shouldApproveReport() {
            // Given
            ESGReport report = createSampleReport();
            when(esgReportRepository.findById(1L)).thenReturn(Optional.of(report));
            when(esgReportRepository.save(any(ESGReport.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            ESGReportResponse result = esgReportService.approveReport(1L, 100L);

            // Then
            assertThat(result.getStatus()).isEqualTo(ReportStatus.APPROVED);
            assertThat(result.getApprovedBy()).isEqualTo(100L);
        }
    }

    // ========== EXPORT TESTS ==========

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export report to CSV format")
        void shouldExportReportToCsv() {
            // Given
            ESGReport report = createSampleReport();
            when(esgReportRepository.findById(1L)).thenReturn(Optional.of(report));

            // When
            String csv = esgReportService.exportToCsv(1L);

            // Then
            assertThat(csv).isNotNull();
            assertThat(csv).contains("ESG Report");
            assertThat(csv).contains("CARBON EMISSIONS");
            assertThat(csv).contains("CARBON SAVINGS");
            assertThat(csv).contains("FLEET METRICS");
        }
    }

    // ========== HELPER METHODS ==========

    private Vehicle createVehicle(Long id, FuelType fuelType, String vehicleNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setFuelType(fuelType);
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setCompanyId(COMPANY_ID);
        return vehicle;
    }

    private ESGReport createSampleReport() {
        return ESGReport.builder()
                .id(1L)
                .companyId(COMPANY_ID)
                .reportName("Test Report")
                .reportType(ReportType.MONTHLY)
                .complianceStandard(ComplianceStandard.SEBI_BRSR)
                .periodStart(START_DATE)
                .periodEnd(END_DATE)
                .scope1EmissionsKg(new BigDecimal("1000"))
                .scope2EmissionsKg(new BigDecimal("500"))
                .scope3EmissionsKg(new BigDecimal("150"))
                .totalEmissionsKg(new BigDecimal("1650"))
                .baselineIceEmissionsKg(new BigDecimal("3000"))
                .carbonSavingsKg(new BigDecimal("1350"))
                .emissionsReductionPercent(new BigDecimal("45"))
                .carbonCostRupees(new BigDecimal("8250"))
                .carbonSavingsRupees(new BigDecimal("6750"))
                .evVehicleCount(3)
                .iceVehicleCount(2)
                .hybridVehicleCount(1)
                .electrificationPercent(new BigDecimal("58.33"))
                .isCompliant(true)
                .status(ReportStatus.DRAFT)
                .build();
    }
}
