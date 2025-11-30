package com.evfleet.analytics.service;

import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.model.HistoricalMetric;
import com.evfleet.analytics.model.HistoricalMetric.MetricType;
import com.evfleet.analytics.model.HistoricalMetric.PeriodType;
import com.evfleet.analytics.model.HistoricalMetric.TrendDirection;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.analytics.repository.HistoricalMetricRepository;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoricalDataService
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Historical Data Service Tests")
class HistoricalDataServiceTest {

    @Mock
    private HistoricalMetricRepository historicalMetricRepository;

    @Mock
    private FleetSummaryRepository fleetSummaryRepository;

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

    @Mock
    private Timer timer;

    @InjectMocks
    private HistoricalDataService historicalDataService;

    @Captor
    private ArgumentCaptor<HistoricalMetric> metricCaptor;

    private static final Long COMPANY_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 6, 15);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(historicalDataService, "retentionYears", 5);
        ReflectionTestUtils.setField(historicalDataService, "dailyRetentionDays", 365);

        // Mock metrics
        when(meterRegistry.counter(anyString())).thenReturn(counter);
        when(Counter.builder(anyString())).thenReturn(Counter.builder("test"));
        when(Timer.builder(anyString())).thenReturn(Timer.builder("test"));
        
        // Mock timer to execute the supplier
        when(timer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(meterRegistry.timer(anyString())).thenReturn(timer);

        historicalDataService.initMetrics();
    }

    // ==================== Test Data Helpers ====================

    private FleetSummary createFleetSummary(LocalDate date) {
        return FleetSummary.builder()
                .id(1L)
                .companyId(COMPANY_ID)
                .summaryDate(date)
                .totalVehicles(100)
                .activeVehicles(75)
                .totalTrips(250)
                .totalDistance(5000.0)
                .totalEnergyConsumed(new BigDecimal("1500.00"))
                .totalCost(new BigDecimal("25000.00"))
                .maintenanceCost(new BigDecimal("5000.00"))
                .energyCost(new BigDecimal("8000.00"))
                .build();
    }

    private HistoricalMetric createHistoricalMetric(PeriodType periodType, MetricType metricType,
                                                     LocalDate periodStart, BigDecimal value) {
        return HistoricalMetric.builder()
                .id(1L)
                .companyId(COMPANY_ID)
                .periodType(periodType)
                .periodStart(periodStart)
                .periodEnd(periodStart)
                .metricType(metricType)
                .metricValue(value)
                .sampleCount(1)
                .build();
    }

    // ==================== Daily Aggregation Tests ====================

    @Nested
    @DisplayName("Daily Aggregation Tests")
    class DailyAggregationTests {

        @Test
        @DisplayName("Should aggregate daily metrics from fleet summary")
        void shouldAggregateDailyMetrics() {
            // Given
            FleetSummary summary = createFleetSummary(TEST_DATE);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(COMPANY_ID, TEST_DATE))
                    .thenReturn(Optional.of(summary));
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                    any(), any(), any(), any())).thenReturn(Optional.empty());
            when(historicalMetricRepository.findPreviousPeriodMetric(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(historicalMetricRepository.save(any(HistoricalMetric.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            List<HistoricalMetric> result = historicalDataService.aggregateDailyMetrics(COMPANY_ID, TEST_DATE);

            // Then
            assertThat(result).isNotEmpty();
            verify(historicalMetricRepository, atLeast(5)).save(any(HistoricalMetric.class));
        }

        @Test
        @DisplayName("Should return empty list when no fleet summary exists")
        void shouldReturnEmptyWhenNoSummary() {
            // Given
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(COMPANY_ID, TEST_DATE))
                    .thenReturn(Optional.empty());

            // When
            List<HistoricalMetric> result = historicalDataService.aggregateDailyMetrics(COMPANY_ID, TEST_DATE);

            // Then
            assertThat(result).isEmpty();
            verify(historicalMetricRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should calculate utilization rate correctly")
        void shouldCalculateUtilizationRate() {
            // Given
            FleetSummary summary = createFleetSummary(TEST_DATE);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(COMPANY_ID, TEST_DATE))
                    .thenReturn(Optional.of(summary));
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                    any(), any(), any(), any())).thenReturn(Optional.empty());
            when(historicalMetricRepository.findPreviousPeriodMetric(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(historicalMetricRepository.save(any(HistoricalMetric.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            historicalDataService.aggregateDailyMetrics(COMPANY_ID, TEST_DATE);

            // Then
            verify(historicalMetricRepository, atLeastOnce()).save(metricCaptor.capture());
            
            List<HistoricalMetric> savedMetrics = metricCaptor.getAllValues();
            Optional<HistoricalMetric> utilizationMetric = savedMetrics.stream()
                    .filter(m -> m.getMetricType() == MetricType.VEHICLE_UTILIZATION_RATE)
                    .findFirst();
            
            assertThat(utilizationMetric).isPresent();
            // 75/100 = 75%
            assertThat(utilizationMetric.get().getMetricValue())
                    .isEqualByComparingTo(new BigDecimal("75.0000"));
        }

        @Test
        @DisplayName("Should calculate cost per km correctly")
        void shouldCalculateCostPerKm() {
            // Given
            FleetSummary summary = createFleetSummary(TEST_DATE);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(COMPANY_ID, TEST_DATE))
                    .thenReturn(Optional.of(summary));
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                    any(), any(), any(), any())).thenReturn(Optional.empty());
            when(historicalMetricRepository.findPreviousPeriodMetric(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(historicalMetricRepository.save(any(HistoricalMetric.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            historicalDataService.aggregateDailyMetrics(COMPANY_ID, TEST_DATE);

            // Then
            verify(historicalMetricRepository, atLeastOnce()).save(metricCaptor.capture());
            
            List<HistoricalMetric> savedMetrics = metricCaptor.getAllValues();
            Optional<HistoricalMetric> costPerKmMetric = savedMetrics.stream()
                    .filter(m -> m.getMetricType() == MetricType.COST_PER_KM)
                    .findFirst();
            
            assertThat(costPerKmMetric).isPresent();
            // 25000/5000 = 5.00 per km
            assertThat(costPerKmMetric.get().getMetricValue())
                    .isEqualByComparingTo(new BigDecimal("5.0000"));
        }

        @Test
        @DisplayName("Should calculate trend from previous day")
        void shouldCalculateTrendFromPreviousDay() {
            // Given
            FleetSummary summary = createFleetSummary(TEST_DATE);
            HistoricalMetric previousMetric = createHistoricalMetric(
                    PeriodType.DAILY, MetricType.TOTAL_VEHICLES,
                    TEST_DATE.minusDays(1), new BigDecimal("90"));

            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(COMPANY_ID, TEST_DATE))
                    .thenReturn(Optional.of(summary));
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                    any(), any(), any(), any())).thenReturn(Optional.empty());
            when(historicalMetricRepository.findPreviousPeriodMetric(
                    eq(COMPANY_ID), eq(MetricType.TOTAL_VEHICLES), eq(PeriodType.DAILY), any()))
                    .thenReturn(Optional.of(previousMetric));
            when(historicalMetricRepository.findPreviousPeriodMetric(
                    eq(COMPANY_ID), not(eq(MetricType.TOTAL_VEHICLES)), any(), any()))
                    .thenReturn(Optional.empty());
            when(historicalMetricRepository.save(any(HistoricalMetric.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            historicalDataService.aggregateDailyMetrics(COMPANY_ID, TEST_DATE);

            // Then
            verify(historicalMetricRepository, atLeastOnce()).save(metricCaptor.capture());
            
            List<HistoricalMetric> savedMetrics = metricCaptor.getAllValues();
            Optional<HistoricalMetric> vehicleMetric = savedMetrics.stream()
                    .filter(m -> m.getMetricType() == MetricType.TOTAL_VEHICLES)
                    .findFirst();
            
            assertThat(vehicleMetric).isPresent();
            assertThat(vehicleMetric.get().getPreviousValue())
                    .isEqualByComparingTo(new BigDecimal("90"));
            // (100-90)/90 = 11.11% increase
            assertThat(vehicleMetric.get().getChangePercent())
                    .isGreaterThan(BigDecimal.ZERO);
            assertThat(vehicleMetric.get().getTrendDirection()).isEqualTo(TrendDirection.UP);
        }
    }

    // ==================== Monthly Aggregation Tests ====================

    @Nested
    @DisplayName("Monthly Aggregation Tests")
    class MonthlyAggregationTests {

        @Test
        @DisplayName("Should aggregate monthly metrics from daily data")
        void shouldAggregateMonthlyMetrics() {
            // Given
            YearMonth yearMonth = YearMonth.of(2024, 6);
            List<HistoricalMetric> dailyMetrics = List.of(
                    createHistoricalMetric(PeriodType.DAILY, MetricType.TOTAL_TRIPS,
                            LocalDate.of(2024, 6, 1), new BigDecimal("100")),
                    createHistoricalMetric(PeriodType.DAILY, MetricType.TOTAL_TRIPS,
                            LocalDate.of(2024, 6, 2), new BigDecimal("150")),
                    createHistoricalMetric(PeriodType.DAILY, MetricType.TOTAL_TRIPS,
                            LocalDate.of(2024, 6, 3), new BigDecimal("120"))
            );

            when(historicalMetricRepository.findDailyMetrics(eq(COMPANY_ID), any(), any()))
                    .thenReturn(dailyMetrics);
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeAndPeriodStartAndMetricType(
                    any(), any(), any(), any())).thenReturn(Optional.empty());
            when(historicalMetricRepository.findPreviousPeriodMetric(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(historicalMetricRepository.save(any(HistoricalMetric.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            List<HistoricalMetric> result = historicalDataService.aggregateMonthlyMetrics(COMPANY_ID, yearMonth);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMetricType()).isEqualTo(MetricType.TOTAL_TRIPS);
            // Sum of 100 + 150 + 120 = 370 (cumulative metric)
            assertThat(result.get(0).getMetricValue()).isEqualByComparingTo(new BigDecimal("370"));
            assertThat(result.get(0).getSampleCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return empty when no daily metrics exist")
        void shouldReturnEmptyWhenNoDailyMetrics() {
            // Given
            YearMonth yearMonth = YearMonth.of(2024, 6);
            when(historicalMetricRepository.findDailyMetrics(eq(COMPANY_ID), any(), any()))
                    .thenReturn(List.of());

            // When
            List<HistoricalMetric> result = historicalDataService.aggregateMonthlyMetrics(COMPANY_ID, yearMonth);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ==================== Trend Calculation Tests ====================

    @Nested
    @DisplayName("Trend Calculation Tests")
    class TrendCalculationTests {

        @Test
        @DisplayName("Should calculate trends correctly")
        void shouldCalculateTrends() {
            // Given
            List<HistoricalMetric> metrics = List.of(
                    createMetricWithChange(LocalDate.of(2024, 6, 1), new BigDecimal("100"), null),
                    createMetricWithChange(LocalDate.of(2024, 6, 2), new BigDecimal("110"), new BigDecimal("10")),
                    createMetricWithChange(LocalDate.of(2024, 6, 3), new BigDecimal("105"), new BigDecimal("-4.55"))
            );

            when(historicalMetricRepository.findRecentMetrics(COMPANY_ID, MetricType.TOTAL_TRIPS, PeriodType.DAILY))
                    .thenReturn(metrics);

            // When
            HistoricalDataService.TrendAnalysisResponse response = 
                    historicalDataService.calculateTrends(COMPANY_ID, MetricType.TOTAL_TRIPS, PeriodType.DAILY, 10);

            // Then
            assertThat(response.getPeriodsAnalyzed()).isEqualTo(3);
            assertThat(response.getDataPoints()).hasSize(3);
            assertThat(response.getMinValue()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(response.getMaxValue()).isEqualByComparingTo(new BigDecimal("110"));
        }

        @Test
        @DisplayName("Should return empty response when no metrics exist")
        void shouldReturnEmptyResponseWhenNoMetrics() {
            // Given
            when(historicalMetricRepository.findRecentMetrics(any(), any(), any()))
                    .thenReturn(List.of());

            // When
            HistoricalDataService.TrendAnalysisResponse response = 
                    historicalDataService.calculateTrends(COMPANY_ID, MetricType.TOTAL_TRIPS, PeriodType.DAILY, 10);

            // Then
            assertThat(response.getPeriodsAnalyzed()).isZero();
            assertThat(response.getDataPoints()).isEmpty();
            assertThat(response.getOverallTrend()).isEqualTo(TrendDirection.STABLE);
        }

        private HistoricalMetric createMetricWithChange(LocalDate date, BigDecimal value, BigDecimal change) {
            HistoricalMetric metric = createHistoricalMetric(PeriodType.DAILY, MetricType.TOTAL_TRIPS, date, value);
            metric.setChangePercent(change);
            if (change != null) {
                if (change.compareTo(BigDecimal.ONE) > 0) {
                    metric.setTrendDirection(TrendDirection.UP);
                } else if (change.compareTo(BigDecimal.valueOf(-1)) < 0) {
                    metric.setTrendDirection(TrendDirection.DOWN);
                } else {
                    metric.setTrendDirection(TrendDirection.STABLE);
                }
            }
            return metric;
        }
    }

    // ==================== Retention Policy Tests ====================

    @Nested
    @DisplayName("Retention Policy Tests")
    class RetentionPolicyTests {

        @Test
        @DisplayName("Should get retention status")
        void shouldGetRetentionStatus() {
            // Given
            List<Object[]> counts = List.of(
                    new Object[]{PeriodType.DAILY, 365L},
                    new Object[]{PeriodType.MONTHLY, 24L},
                    new Object[]{PeriodType.YEARLY, 5L}
            );
            when(historicalMetricRepository.countMetricsByPeriodType(COMPANY_ID))
                    .thenReturn(counts);
            when(historicalMetricRepository.findByCompanyIdAndPeriodTypeOrderByPeriodStartDesc(
                    COMPANY_ID, PeriodType.DAILY))
                    .thenReturn(List.of(createHistoricalMetric(PeriodType.DAILY, MetricType.TOTAL_TRIPS,
                            LocalDate.of(2023, 1, 1), BigDecimal.ONE)));

            // When
            HistoricalDataService.RetentionPolicyStatus status = 
                    historicalDataService.getRetentionStatus(COMPANY_ID);

            // Then
            assertThat(status.getRetentionYears()).isEqualTo(5);
            assertThat(status.getDailyRetentionDays()).isEqualTo(365);
            assertThat(status.getDailyMetricsCount()).isEqualTo(365L);
            assertThat(status.getMonthlyMetricsCount()).isEqualTo(24L);
            assertThat(status.getYearlyMetricsCount()).isEqualTo(5L);
        }
    }

    // ==================== Trends Summary Tests ====================

    @Nested
    @DisplayName("Trends Summary Tests")
    class TrendsSummaryTests {

        @Test
        @DisplayName("Should get trends summary for all metric types")
        void shouldGetTrendsSummary() {
            // Given
            List<HistoricalMetric> latestMetrics = List.of(
                    createMetricWithTrend(MetricType.TOTAL_VEHICLES, new BigDecimal("100"), 
                            new BigDecimal("90"), new BigDecimal("11.11"), TrendDirection.UP),
                    createMetricWithTrend(MetricType.TOTAL_COST, new BigDecimal("50000"), 
                            new BigDecimal("55000"), new BigDecimal("-9.09"), TrendDirection.DOWN)
            );

            when(historicalMetricRepository.findLatestMetricsOfEachType(COMPANY_ID, PeriodType.MONTHLY))
                    .thenReturn(latestMetrics);

            // When
            List<HistoricalDataService.TrendSummary> summaries = 
                    historicalDataService.getTrendsSummary(COMPANY_ID, PeriodType.MONTHLY);

            // Then
            assertThat(summaries).hasSize(2);
            assertThat(summaries.get(0).getTrendDirection()).isEqualTo(TrendDirection.UP);
            assertThat(summaries.get(1).getTrendDirection()).isEqualTo(TrendDirection.DOWN);
        }

        private HistoricalMetric createMetricWithTrend(MetricType type, BigDecimal current, 
                                                        BigDecimal previous, BigDecimal change,
                                                        TrendDirection direction) {
            HistoricalMetric metric = createHistoricalMetric(PeriodType.MONTHLY, type, 
                    LocalDate.of(2024, 6, 1), current);
            metric.setPreviousValue(previous);
            metric.setChangePercent(change);
            metric.setTrendDirection(direction);
            return metric;
        }
    }
}
