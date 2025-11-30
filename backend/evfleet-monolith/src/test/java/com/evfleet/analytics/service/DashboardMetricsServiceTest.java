package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.DashboardMetricsResponse;
import com.evfleet.analytics.dto.DashboardMetricsResponse.*;
import com.evfleet.analytics.dto.LiveVehiclePositionResponse;
import com.evfleet.analytics.dto.LiveVehiclePositionResponse.VehiclePosition;
import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.repository.FleetSummaryRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardMetricsService
 * 
 * Tests cached metrics retrieval, live position tracking,
 * cache invalidation, and performance metrics.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DashboardMetricsServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private FleetSummaryRepository fleetSummaryRepository;

    private MeterRegistry meterRegistry;
    private DashboardMetricsService dashboardMetricsService;

    private static final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        dashboardMetricsService = new DashboardMetricsService(
                vehicleRepository,
                fleetSummaryRepository,
                meterRegistry
        );
    }

    // ========== SUMMARY METRICS TESTS ==========

    @Nested
    @DisplayName("Summary Metrics Tests")
    class SummaryMetricsTests {

        @Test
        @DisplayName("Should return dashboard metrics for company")
        void shouldReturnDashboardMetrics() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80")),
                    createVehicle(2L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null),
                    createVehicle(3L, FuelType.EV, Vehicle.VehicleStatus.CHARGING, new BigDecimal("45"))
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCompanyId()).isEqualTo(COMPANY_ID);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getFleet()).isNotNull();
            assertThat(response.getFleet().getTotalVehicles()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return cached metrics on second call")
        void shouldReturnCachedMetricsOnSecondCall() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse first = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);
            DashboardMetricsResponse second = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            assertThat(first.isCached()).isFalse();
            assertThat(second.isCached()).isTrue();
            
            // Repository should only be called once
            verify(vehicleRepository, times(1)).findByCompanyId(COMPANY_ID);
        }

        @Test
        @DisplayName("Should return null when ETag matches (304 Not Modified)")
        void shouldReturnNullWhenEtagMatches() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse first = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);
            String etag = first.getEtag();
            DashboardMetricsResponse second = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, etag);

            // Then
            assertThat(first).isNotNull();
            assertThat(second).isNull(); // Indicates 304 Not Modified
        }

        @Test
        @DisplayName("Should include ETag in response")
        void shouldIncludeEtagInResponse() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            assertThat(response.getEtag()).isNotNull();
            assertThat(response.getEtag()).startsWith("\"");
            assertThat(response.getEtag()).endsWith("\"");
        }
    }

    // ========== FLEET OVERVIEW TESTS ==========

    @Nested
    @DisplayName("Fleet Overview Tests")
    class FleetOverviewTests {

        @Test
        @DisplayName("Should calculate fleet overview correctly")
        void shouldCalculateFleetOverview() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80")),
                    createVehicle(2L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("60")),
                    createVehicle(3L, FuelType.EV, Vehicle.VehicleStatus.CHARGING, new BigDecimal("30")),
                    createVehicle(4L, FuelType.ICE, Vehicle.VehicleStatus.IDLE, null),
                    createVehicle(5L, FuelType.EV, Vehicle.VehicleStatus.MAINTENANCE, new BigDecimal("50"))
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            FleetOverview fleet = response.getFleet();
            assertThat(fleet.getTotalVehicles()).isEqualTo(5);
            assertThat(fleet.getActiveVehicles()).isEqualTo(2);
            assertThat(fleet.getChargingVehicles()).isEqualTo(1);
            assertThat(fleet.getIdleVehicles()).isEqualTo(1);
            assertThat(fleet.getMaintenanceVehicles()).isEqualTo(1);
            assertThat(fleet.getUtilizationPercent()).isEqualByComparingTo(new BigDecimal("40.0")); // 2/5 = 40%
        }

        @Test
        @DisplayName("Should handle empty fleet")
        void shouldHandleEmptyFleet() {
            // Given
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(Collections.emptyList());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            FleetOverview fleet = response.getFleet();
            assertThat(fleet.getTotalVehicles()).isEqualTo(0);
            assertThat(fleet.getUtilizationPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ========== BATTERY METRICS TESTS ==========

    @Nested
    @DisplayName("Battery Metrics Tests")
    class BatteryMetricsTests {

        @Test
        @DisplayName("Should calculate battery metrics correctly")
        void shouldCalculateBatteryMetrics() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("95")),   // Full
                    createVehicle(2L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("50")),   // Normal
                    createVehicle(3L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("15")),   // Low
                    createVehicle(4L, FuelType.EV, Vehicle.VehicleStatus.CHARGING, new BigDecimal("5")),  // Critical
                    createVehicle(5L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null)                     // Not EV
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            BatteryMetrics battery = response.getBattery();
            assertThat(battery.getVehiclesFullyCharged()).isEqualTo(1);    // >= 90%
            assertThat(battery.getVehiclesLowBattery()).isEqualTo(2);       // < 20%
            assertThat(battery.getVehiclesCriticalBattery()).isEqualTo(1);  // < 10%
            assertThat(battery.getVehiclesCharging()).isEqualTo(1);
            // Average: (95+50+15+5)/4 = 41.25
            assertThat(battery.getAvgBatteryLevel()).isEqualByComparingTo(new BigDecimal("41.3"));
        }

        @Test
        @DisplayName("Should handle ICE-only fleet for battery metrics")
        void shouldHandleIceOnlyFleet() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null),
                    createVehicle(2L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null)
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            BatteryMetrics battery = response.getBattery();
            assertThat(battery.getAvgBatteryLevel()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(battery.getVehiclesLowBattery()).isEqualTo(0);
        }
    }

    // ========== LIVE POSITIONS TESTS ==========

    @Nested
    @DisplayName("Live Position Tests")
    class LivePositionTests {

        @Test
        @DisplayName("Should return live vehicle positions")
        void shouldReturnLivePositions() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80")),
                    createVehicle(2L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null)
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);

            // When
            LiveVehiclePositionResponse response = dashboardMetricsService.getLivePositions(COMPANY_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCompanyId()).isEqualTo(COMPANY_ID);
            assertThat(response.getTotalVehicles()).isEqualTo(2);
            assertThat(response.getVehicles()).hasSize(2);
        }

        @Test
        @DisplayName("Should include GPS coordinates in positions")
        void shouldIncludeGpsCoordinates() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));

            // When
            LiveVehiclePositionResponse response = dashboardMetricsService.getLivePositions(COMPANY_ID);

            // Then
            VehiclePosition position = response.getVehicles().get(0);
            assertThat(position.getLatitude()).isNotNull();
            assertThat(position.getLongitude()).isNotNull();
            assertThat(position.getHeading()).isNotNull();
        }

        @Test
        @DisplayName("Should include battery status in positions")
        void shouldIncludeBatteryStatus() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("15"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));

            // When
            LiveVehiclePositionResponse response = dashboardMetricsService.getLivePositions(COMPANY_ID);

            // Then
            VehiclePosition position = response.getVehicles().get(0);
            assertThat(position.getBatteryPercent()).isEqualByComparingTo(new BigDecimal("15"));
            assertThat(position.getBatteryStatus()).isEqualTo("LOW");
        }

        @Test
        @DisplayName("Should include status colors in positions")
        void shouldIncludeStatusColors() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));

            // When
            LiveVehiclePositionResponse response = dashboardMetricsService.getLivePositions(COMPANY_ID);

            // Then
            VehiclePosition position = response.getVehicles().get(0);
            assertThat(position.getStatusColor()).isNotNull();
            assertThat(position.getStatusColor()).startsWith("#");
        }
    }

    // ========== CACHE MANAGEMENT TESTS ==========

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Should refresh metrics on force refresh")
        void shouldRefreshMetricsOnForceRefresh() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null); // First call
            DashboardMetricsResponse refreshed = dashboardMetricsService.refreshMetrics(COMPANY_ID);

            // Then
            assertThat(refreshed).isNotNull();
            assertThat(refreshed.isCached()).isFalse();
            
            // Repository should be called twice (once for initial, once for refresh)
            verify(vehicleRepository, times(2)).findByCompanyId(COMPANY_ID);
        }

        @Test
        @DisplayName("Should clear all caches on refresh all")
        void shouldClearAllCachesOnRefreshAll() {
            // Given
            Vehicle vehicle = createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80"));
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(vehicle));
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Populate cache
            dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // When
            dashboardMetricsService.refreshAllMetrics();
            DashboardMetricsResponse afterRefresh = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            assertThat(afterRefresh.isCached()).isFalse();
        }
    }

    // ========== ESG QUICK STATS TESTS ==========

    @Nested
    @DisplayName("ESG Quick Stats Tests")
    class EsgQuickStatsTests {

        @Test
        @DisplayName("Should calculate electrification percentage")
        void shouldCalculateElectrificationPercentage() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80")),
                    createVehicle(2L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("60")),
                    createVehicle(3L, FuelType.ICE, Vehicle.VehicleStatus.ACTIVE, null),
                    createVehicle(4L, FuelType.HYBRID, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("70"))
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            EsgQuickStats esg = response.getEsg();
            // 2 EVs + 0.5 * 1 Hybrid = 2.5 out of 4 = 62.5%
            assertThat(esg.getElectrificationPercent()).isEqualByComparingTo(new BigDecimal("62.5"));
        }

        @Test
        @DisplayName("Should estimate carbon savings")
        void shouldEstimateCarbonSavings() {
            // Given
            List<Vehicle> vehicles = Arrays.asList(
                    createVehicle(1L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("80")),
                    createVehicle(2L, FuelType.EV, Vehicle.VehicleStatus.ACTIVE, new BigDecimal("60"))
            );
            when(vehicleRepository.findByCompanyId(COMPANY_ID)).thenReturn(vehicles);
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDate(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            when(fleetSummaryRepository.findByCompanyIdAndSummaryDateBetween(eq(COMPANY_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardMetricsResponse response = dashboardMetricsService.getSummaryMetrics(COMPANY_ID, null);

            // Then
            EsgQuickStats esg = response.getEsg();
            assertThat(esg.getCarbonSavingsKgToday()).isGreaterThan(BigDecimal.ZERO);
            assertThat(esg.getTreesEquivalent()).isGreaterThanOrEqualTo(0);
        }
    }

    // ========== HELPER METHODS ==========

    private Vehicle createVehicle(Long id, FuelType fuelType, Vehicle.VehicleStatus status, BigDecimal batteryLevel) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setCompanyId(COMPANY_ID);
        vehicle.setVehicleNumber("VH" + id);
        vehicle.setLicensePlate("MH01AB" + String.format("%04d", id));
        vehicle.setFuelType(fuelType);
        vehicle.setStatus(status);
        vehicle.setBatteryLevel(batteryLevel);
        return vehicle;
    }
}
