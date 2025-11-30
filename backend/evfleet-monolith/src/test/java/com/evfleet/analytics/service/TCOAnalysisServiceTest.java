package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.dto.TCOProjectionResponse;
import com.evfleet.analytics.model.TCOAnalysis;
import com.evfleet.analytics.repository.TCOAnalysisRepository;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TCOAnalysisService with multi-fuel support
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TCOAnalysisServiceTest {

    @Mock
    private TCOAnalysisRepository tcoAnalysisRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ChargingSessionRepository chargingSessionRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private TCOAnalysisService tcoAnalysisService;

    private Vehicle testVehicle;
    private TCOAnalysis testTCOAnalysis;

    @BeforeEach
    void setUp() {
        // Set default values
        ReflectionTestUtils.setField(tcoAnalysisService, "defaultProjectionYears", 5);
        ReflectionTestUtils.setField(tcoAnalysisService, "includeCarbonCost", true);

        // Create test vehicle
        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setCompanyId(100L);
        testVehicle.setMake("Tata");
        testVehicle.setModel("Nexon EV");
        testVehicle.setFuelType(FuelType.EV);
        testVehicle.setVehicleNumber("MH01AB1234");
        testVehicle.setCreatedAt(LocalDateTime.now().minusYears(1));

        // Create test TCO analysis
        testTCOAnalysis = TCOAnalysis.builder()
                .id(1L)
                .companyId(100L)
                .vehicleId(1L)
                .analysisDate(LocalDate.now())
                .analysisPeriodYears(5)
                .fuelType(FuelType.EV)
                .purchasePrice(new BigDecimal("1500000"))
                .depreciationValue(new BigDecimal("300000"))
                .currentValue(new BigDecimal("1200000"))
                .energyCosts(new BigDecimal("25000"))
                .maintenanceCosts(new BigDecimal("10000"))
                .insuranceCosts(new BigDecimal("45000"))
                .taxesFees(new BigDecimal("5000"))
                .carbonEmissionsKg(new BigDecimal("500"))
                .carbonCost(new BigDecimal("2500"))
                .totalCost(new BigDecimal("387500"))
                .costPerKm(new BigDecimal("5.00"))
                .costPerYear(new BigDecimal("77500"))
                .totalDistanceKm(new BigDecimal("50000"))
                .iceFuelSavings(new BigDecimal("150000"))
                .iceMaintenanceSavings(new BigDecimal("40000"))
                .iceTotalSavings(new BigDecimal("190000"))
                .icePaybackPeriodMonths(31)
                .projected5YrTotalCost(new BigDecimal("750000"))
                .projected5YrEnergyCost(new BigDecimal("140000"))
                .projected5YrMaintenanceCost(new BigDecimal("55000"))
                .projected5YrCarbonCost(new BigDecimal("12500"))
                .build();

        // Mock meter registry
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Nested
    @DisplayName("Calculate TCO Tests")
    class CalculateTCOTests {

        @Test
        @DisplayName("Should calculate TCO for EV vehicle")
        void shouldCalculateTCOForEVVehicle() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
            when(tripRepository.findByVehicleId(1L)).thenReturn(createTestTrips());
            when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(createTestChargingSessions());
            when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(createTestMaintenanceRecords());
            when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(i -> {
                TCOAnalysis tco = i.getArgument(0);
                tco.setId(1L);
                return tco;
            });

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.calculateTCO(1L, 5);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getFuelType()).isEqualTo(FuelType.EV);
            assertThat(response.getEnergyCosts()).isNotNull();
            assertThat(response.getCarbonCost()).isNotNull();
            assertThat(response.getProjected5YrTotalCost()).isNotNull();
            assertThat(response.getComparisonWithICE()).isNotNull();

            verify(tcoAnalysisRepository).save(any(TCOAnalysis.class));
            verify(counter).increment();
        }

        @ParameterizedTest
        @EnumSource(FuelType.class)
        @DisplayName("Should calculate TCO for all fuel types")
        void shouldCalculateTCOForAllFuelTypes(FuelType fuelType) {
            // Arrange
            testVehicle.setFuelType(fuelType);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
            when(tripRepository.findByVehicleId(1L)).thenReturn(createTestTrips());
            when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(createTestChargingSessions());
            when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(createTestMaintenanceRecords());
            when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(i -> {
                TCOAnalysis tco = i.getArgument(0);
                tco.setId(1L);
                return tco;
            });

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.calculateTCO(1L, 5);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getFuelType()).isEqualTo(fuelType);
            assertThat(response.getEnergyCosts()).isNotNull();
            assertThat(response.getMaintenanceCosts()).isNotNull();
            assertThat(response.getTotalCost()).isNotNull();
        }

        @Test
        @DisplayName("Should use existing TCO analysis if available")
        void shouldUseExistingTCOAnalysis() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));
            when(tripRepository.findByVehicleId(1L)).thenReturn(createTestTrips());
            when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(createTestChargingSessions());
            when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(createTestMaintenanceRecords());
            when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenReturn(testTCOAnalysis);

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.calculateTCO(1L, 5);

            // Assert
            assertThat(response).isNotNull();
            verify(tcoAnalysisRepository).findLatestByVehicleId(1L);
        }
    }

    @Nested
    @DisplayName("Get TCO Analysis Tests")
    class GetTCOAnalysisTests {

        @Test
        @DisplayName("Should get existing TCO analysis")
        void shouldGetExistingTCOAnalysis() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getVehicleId()).isEqualTo(1L);
            assertThat(response.getTotalCost()).isEqualTo(testTCOAnalysis.getTotalCost());
        }

        @Test
        @DisplayName("Should calculate TCO if not found")
        void shouldCalculateTCOIfNotFound() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.empty());
            when(tripRepository.findByVehicleId(1L)).thenReturn(createTestTrips());
            when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(createTestChargingSessions());
            when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(createTestMaintenanceRecords());
            when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenAnswer(i -> {
                TCOAnalysis tco = i.getArgument(0);
                tco.setId(1L);
                return tco;
            });

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

            // Assert
            assertThat(response).isNotNull();
            verify(tcoAnalysisRepository).save(any(TCOAnalysis.class));
        }
    }

    @Nested
    @DisplayName("TCO Comparison Tests")
    class TCOComparisonTests {

        @Test
        @DisplayName("Should compare TCO between two vehicles")
        void shouldCompareTCOBetweenTwoVehicles() {
            // Arrange
            Vehicle vehicle2 = new Vehicle();
            vehicle2.setId(2L);
            vehicle2.setCompanyId(100L);
            vehicle2.setMake("Maruti");
            vehicle2.setModel("Dzire");
            vehicle2.setFuelType(FuelType.ICE);
            vehicle2.setVehicleNumber("MH01CD5678");
            vehicle2.setCreatedAt(LocalDateTime.now().minusYears(1));

            TCOAnalysis tco2 = TCOAnalysis.builder()
                    .id(2L)
                    .companyId(100L)
                    .vehicleId(2L)
                    .analysisDate(LocalDate.now())
                    .fuelType(FuelType.ICE)
                    .totalCost(new BigDecimal("500000"))
                    .build();

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle2));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));
            when(tcoAnalysisRepository.findLatestByVehicleId(2L)).thenReturn(Optional.of(tco2));

            // Act
            TCOAnalysisService.TCOComparisonResponse comparison = 
                    tcoAnalysisService.compareTCO(1L, 2L, 5);

            // Assert
            assertThat(comparison).isNotNull();
            assertThat(comparison.getVehicle1()).isNotNull();
            assertThat(comparison.getVehicle2()).isNotNull();
            assertThat(comparison.getCostDifference()).isNotNull();
            assertThat(comparison.getCheaperVehicleId()).isIn(1L, 2L);
        }

        @Test
        @DisplayName("Should compare fuel types for hypothetical vehicle")
        void shouldCompareFuelTypesForHypotheticalVehicle() {
            // Act
            Map<FuelType, TCOProjectionResponse> comparisons = tcoAnalysisService.compareFuelTypes(
                    new BigDecimal("1500000"),
                    new BigDecimal("15000"),
                    5,
                    "MUMBAI"
            );

            // Assert
            assertThat(comparisons).isNotNull();
            assertThat(comparisons).hasSize(FuelType.values().length);
            assertThat(comparisons).containsKeys(FuelType.EV, FuelType.ICE, FuelType.HYBRID);

            // Verify EV has lower energy costs than ICE
            TCOProjectionResponse evProjection = comparisons.get(FuelType.EV);
            TCOProjectionResponse iceProjection = comparisons.get(FuelType.ICE);
            assertThat(evProjection.getProjectedEnergyCost())
                    .isLessThan(iceProjection.getProjectedEnergyCost());
        }
    }

    @Nested
    @DisplayName("5-Year Projection Tests")
    class ProjectionTests {

        @Test
        @DisplayName("Should get 5-year projection for vehicle")
        void shouldGet5YearProjection() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));

            // Act
            TCOProjectionResponse projection = tcoAnalysisService.get5YearProjection(1L);

            // Assert
            assertThat(projection).isNotNull();
            assertThat(projection.getVehicleId()).isEqualTo(1L);
            assertThat(projection.getProjectionYears()).isEqualTo(5);
            assertThat(projection.getTotalProjectedCost()).isNotNull();
            assertThat(projection.getProjectedEnergyCost()).isNotNull();
            assertThat(projection.getProjectedMaintenanceCost()).isNotNull();
            assertThat(projection.getProjectedCarbonCost()).isNotNull();
        }
    }

    @Nested
    @DisplayName("TCO Trend Tests")
    class TCOTrendTests {

        @Test
        @DisplayName("Should get TCO trend over time")
        void shouldGetTCOTrendOverTime() {
            // Arrange
            LocalDate startDate = LocalDate.now().minusMonths(6);
            LocalDate endDate = LocalDate.now();
            
            List<TCOAnalysis> tcoList = Arrays.asList(
                    createTCOForDate(LocalDate.now().minusMonths(5)),
                    createTCOForDate(LocalDate.now().minusMonths(4)),
                    createTCOForDate(LocalDate.now().minusMonths(3)),
                    createTCOForDate(LocalDate.now().minusMonths(2)),
                    createTCOForDate(LocalDate.now().minusMonths(1)),
                    createTCOForDate(LocalDate.now())
            );

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findByVehicleIdAndAnalysisDateBetween(1L, startDate, endDate))
                    .thenReturn(tcoList);

            // Act
            List<TCOAnalysisResponse> trend = tcoAnalysisService.getTCOTrend(1L, startDate, endDate);

            // Assert
            assertThat(trend).isNotNull();
            assertThat(trend).hasSize(6);
        }
    }

    @Nested
    @DisplayName("Batch Recalculation Tests")
    class BatchRecalculationTests {

        @Test
        @DisplayName("Should recalculate TCO for all vehicles")
        void shouldRecalculateTCOForAllVehicles() {
            // Arrange
            List<Vehicle> vehicles = Arrays.asList(testVehicle);
            
            when(vehicleRepository.findAll()).thenReturn(vehicles);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));
            when(tripRepository.findByVehicleId(1L)).thenReturn(createTestTrips());
            when(chargingSessionRepository.findByVehicleId(1L)).thenReturn(createTestChargingSessions());
            when(maintenanceRecordRepository.findByVehicleId(1L)).thenReturn(createTestMaintenanceRecords());
            when(tcoAnalysisRepository.save(any(TCOAnalysis.class))).thenReturn(testTCOAnalysis);

            // Act
            tcoAnalysisService.recalculateTCOForAllVehicles();

            // Assert
            verify(vehicleRepository).findAll();
            verify(tcoAnalysisRepository, atLeastOnce()).save(any(TCOAnalysis.class));
        }
    }

    @Nested
    @DisplayName("ICE Comparison Tests")
    class ICEComparisonTests {

        @Test
        @DisplayName("Should calculate ICE comparison for EV")
        void shouldCalculateICEComparisonForEV() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getComparisonWithICE()).isNotNull();
            assertThat(response.getComparisonWithICE().getFuelSavings()).isNotNull();
            assertThat(response.getComparisonWithICE().getMaintenanceSavings()).isNotNull();
            assertThat(response.getComparisonWithICE().getTotalSavings()).isNotNull();
            assertThat(response.getComparisonWithICE().getPaybackPeriodMonths()).isNotNull();
        }

        @Test
        @DisplayName("Should not have ICE comparison for ICE vehicle")
        void shouldNotHaveICEComparisonForICEVehicle() {
            // Arrange
            testVehicle.setFuelType(FuelType.ICE);
            testTCOAnalysis.setFuelType(FuelType.ICE);
            testTCOAnalysis.setIceTotalSavings(BigDecimal.ZERO);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getComparisonWithICE()).isNull();
        }
    }

    @Nested
    @DisplayName("Carbon Cost Tests")
    class CarbonCostTests {

        @Test
        @DisplayName("Should calculate carbon costs for EV")
        void shouldCalculateCarbonCostsForEV() {
            // Arrange
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
            when(tcoAnalysisRepository.findLatestByVehicleId(1L)).thenReturn(Optional.of(testTCOAnalysis));

            // Act
            TCOAnalysisResponse response = tcoAnalysisService.getTCOAnalysis(1L);

            // Assert
            assertThat(response.getCarbonEmissionsKg()).isNotNull();
            assertThat(response.getCarbonCost()).isNotNull();
            // EV should have lower emissions than ICE
            assertThat(response.getCarbonEmissionsKg()).isLessThan(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("Carbon emissions should vary by fuel type")
        void carbonEmissionsShouldVaryByFuelType() {
            // Act
            Map<FuelType, TCOProjectionResponse> comparisons = tcoAnalysisService.compareFuelTypes(
                    new BigDecimal("1500000"),
                    new BigDecimal("15000"),
                    5,
                    "DELHI"
            );

            // Assert - EV should have lowest carbon
            BigDecimal evCarbon = comparisons.get(FuelType.EV).getCarbonEmissionsKg();
            BigDecimal iceCarbon = comparisons.get(FuelType.ICE).getCarbonEmissionsKg();
            
            assertThat(evCarbon).isLessThan(iceCarbon);
        }
    }

    // ==================== Helper Methods ====================

    private List<Trip> createTestTrips() {
        Trip trip1 = new Trip();
        trip1.setId(1L);
        trip1.setVehicleId(1L);
        trip1.setDistance(100.0);

        Trip trip2 = new Trip();
        trip2.setId(2L);
        trip2.setVehicleId(1L);
        trip2.setDistance(150.0);

        return Arrays.asList(trip1, trip2);
    }

    private List<ChargingSession> createTestChargingSessions() {
        ChargingSession session1 = new ChargingSession();
        session1.setId(1L);
        session1.setVehicleId(1L);
        session1.setTotalCost(new BigDecimal("500"));
        session1.setEnergyDeliveredKwh(new BigDecimal("50"));

        ChargingSession session2 = new ChargingSession();
        session2.setId(2L);
        session2.setVehicleId(1L);
        session2.setTotalCost(new BigDecimal("600"));
        session2.setEnergyDeliveredKwh(new BigDecimal("60"));

        return Arrays.asList(session1, session2);
    }

    private List<MaintenanceRecord> createTestMaintenanceRecords() {
        MaintenanceRecord record1 = new MaintenanceRecord();
        record1.setId(1L);
        record1.setVehicleId(1L);
        record1.setActualCost(new BigDecimal("5000"));

        MaintenanceRecord record2 = new MaintenanceRecord();
        record2.setId(2L);
        record2.setVehicleId(1L);
        record2.setActualCost(new BigDecimal("3000"));

        return Arrays.asList(record1, record2);
    }

    private TCOAnalysis createTCOForDate(LocalDate date) {
        return TCOAnalysis.builder()
                .id((long) date.getDayOfMonth())
                .companyId(100L)
                .vehicleId(1L)
                .analysisDate(date)
                .fuelType(FuelType.EV)
                .totalCost(new BigDecimal("400000"))
                .energyCosts(new BigDecimal("25000"))
                .maintenanceCosts(new BigDecimal("10000"))
                .build();
    }
}
