package com.evfleet.telematics.scheduler;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import com.evfleet.telematics.model.TelemetrySnapshot;
import com.evfleet.telematics.provider.TelemetryProvider;
import com.evfleet.telematics.repository.TelemetrySnapshotRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TelemetrySyncScheduler
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TelemetrySyncSchedulerTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TelemetrySnapshotRepository snapshotRepository;

    @Mock
    private TelemetryProvider mockProvider;

    private MeterRegistry meterRegistry;

    private TelemetrySyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        List<TelemetryProvider> providers = List.of(mockProvider);
        
        scheduler = new TelemetrySyncScheduler(
            vehicleRepository,
            snapshotRepository,
            providers,
            meterRegistry
        );
        
        // Set configuration values
        ReflectionTestUtils.setField(scheduler, "syncEnabled", true);
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
        ReflectionTestUtils.setField(scheduler, "maxRetries", 3);
        ReflectionTestUtils.setField(scheduler, "backoffMultiplier", 2);
        
        // Initialize the scheduler (simulates @PostConstruct)
        scheduler.init();
    }

    @Test
    @DisplayName("Should sync vehicles with telematics integration")
    void shouldSyncVehiclesWithTelematicsIntegration() {
        // Given
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(mockProvider.supports(vehicle)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        when(mockProvider.fetchLatestData(vehicle)).thenReturn(Optional.of(telemetryData));
        when(snapshotRepository.save(any(TelemetrySnapshot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        verify(snapshotRepository).save(any(TelemetrySnapshot.class));
        verify(vehicleRepository).save(any(Vehicle.class));
        
        TelemetrySyncScheduler.SyncStatistics stats = scheduler.getStatistics();
        assertThat(stats.totalVehiclesSynced()).isEqualTo(1);
        assertThat(stats.totalVehiclesFailed()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should skip vehicles without telematics source")
    void shouldSkipVehiclesWithoutTelematicsSource() {
        // Given
        Vehicle vehicleNoTelemetry = createTestVehicle(1L, Vehicle.TelemetrySource.NONE);
        Vehicle vehicleManual = createTestVehicle(2L, Vehicle.TelemetrySource.MANUAL);
        Vehicle vehicleWithDevice = createTestVehicle(3L, Vehicle.TelemetrySource.DEVICE);
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        
        when(vehicleRepository.findAll()).thenReturn(
            Arrays.asList(vehicleNoTelemetry, vehicleManual, vehicleWithDevice)
        );
        when(mockProvider.supports(vehicleWithDevice)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        when(mockProvider.fetchLatestData(vehicleWithDevice)).thenReturn(Optional.of(telemetryData));
        when(snapshotRepository.save(any(TelemetrySnapshot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then - only vehicleWithDevice should be synced
        verify(snapshotRepository, times(1)).save(any(TelemetrySnapshot.class));
    }

    @Test
    @DisplayName("Should skip inactive vehicles")
    void shouldSkipInactiveVehicles() {
        // Given
        Vehicle inactiveVehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        inactiveVehicle.setStatus(Vehicle.VehicleStatus.INACTIVE);
        
        when(vehicleRepository.findAll()).thenReturn(List.of(inactiveVehicle));
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        verify(snapshotRepository, never()).save(any(TelemetrySnapshot.class));
    }

    @Test
    @DisplayName("Should handle provider failure gracefully")
    void shouldHandleProviderFailureGracefully() {
        // Given
        Vehicle vehicle1 = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        Vehicle vehicle2 = createTestVehicle(2L, Vehicle.TelemetrySource.DEVICE);
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        
        when(vehicleRepository.findAll()).thenReturn(Arrays.asList(vehicle1, vehicle2));
        when(mockProvider.supports(any(Vehicle.class))).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        
        // First vehicle fails, second succeeds
        when(mockProvider.fetchLatestData(vehicle1)).thenThrow(new RuntimeException("Connection failed"));
        when(mockProvider.fetchLatestData(vehicle2)).thenReturn(Optional.of(telemetryData));
        when(snapshotRepository.save(any(TelemetrySnapshot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then - vehicle2 should still be synced despite vehicle1 failure
        verify(snapshotRepository, times(1)).save(any(TelemetrySnapshot.class));
        
        TelemetrySyncScheduler.SyncStatistics stats = scheduler.getStatistics();
        assertThat(stats.totalVehiclesFailed()).isGreaterThanOrEqualTo(1);
        assertThat(stats.totalVehiclesSynced()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should not sync when disabled")
    void shouldNotSyncWhenDisabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "syncEnabled", false);
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        verify(vehicleRepository, never()).findAll();
        verify(snapshotRepository, never()).save(any(TelemetrySnapshot.class));
    }

    @Test
    @DisplayName("Should update vehicle with telemetry data")
    void shouldUpdateVehicleWithTelemetryData() {
        // Given
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        telemetryData.setLatitude(12.9716);
        telemetryData.setLongitude(77.5946);
        telemetryData.setBatterySoc(85.0);
        telemetryData.setOdometer(15000.0);
        
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(mockProvider.supports(vehicle)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        when(mockProvider.fetchLatestData(vehicle)).thenReturn(Optional.of(telemetryData));
        when(snapshotRepository.save(any(TelemetrySnapshot.class))).thenAnswer(i -> i.getArguments()[0]);
        
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        when(vehicleRepository.save(vehicleCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.getLatitude()).isEqualTo(12.9716);
        assertThat(savedVehicle.getLongitude()).isEqualTo(77.5946);
        assertThat(savedVehicle.getCurrentBatterySoc()).isEqualTo(85.0);
        assertThat(savedVehicle.getOdometer()).isEqualTo(15000.0);
        assertThat(savedVehicle.getLastTelemetryUpdate()).isNotNull();
    }

    @Test
    @DisplayName("Should create snapshot with correct data")
    void shouldCreateSnapshotWithCorrectData() {
        // Given
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.OEM_API);
        vehicle.setOemVehicleId("OEM-123");
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(mockProvider.supports(vehicle)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Tata FleetEdge");
        when(mockProvider.fetchLatestData(vehicle)).thenReturn(Optional.of(telemetryData));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);
        
        ArgumentCaptor<TelemetrySnapshot> snapshotCaptor = ArgumentCaptor.forClass(TelemetrySnapshot.class);
        when(snapshotRepository.save(snapshotCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        TelemetrySnapshot snapshot = snapshotCaptor.getValue();
        assertThat(snapshot.getVehicleId()).isEqualTo(1L);
        assertThat(snapshot.getCompanyId()).isEqualTo(100L);
        assertThat(snapshot.getSource()).isEqualTo(Vehicle.TelemetrySource.OEM_API);
        assertThat(snapshot.getProviderName()).isEqualTo("Tata FleetEdge");
        assertThat(snapshot.getDeviceId()).isEqualTo("OEM-123");
        assertThat(snapshot.getBatterySoc()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Should handle empty provider return gracefully")
    void shouldHandleEmptyProviderReturnGracefully() {
        // Given
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(mockProvider.supports(vehicle)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        when(mockProvider.fetchLatestData(vehicle)).thenReturn(Optional.empty());
        
        // When
        scheduler.syncAllVehicleTelemetry();
        
        // Then
        verify(snapshotRepository, never()).save(any(TelemetrySnapshot.class));
        
        TelemetrySyncScheduler.SyncStatistics stats = scheduler.getStatistics();
        assertThat(stats.totalVehiclesFailed()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should sync specific vehicle by ID")
    void shouldSyncSpecificVehicleById() {
        // Given
        Vehicle vehicle = createTestVehicle(1L, Vehicle.TelemetrySource.DEVICE);
        VehicleTelemetryData telemetryData = createTestTelemetryData();
        
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(mockProvider.supports(vehicle)).thenReturn(true);
        when(mockProvider.getProviderName()).thenReturn("Test Provider");
        when(mockProvider.fetchLatestData(vehicle)).thenReturn(Optional.of(telemetryData));
        when(snapshotRepository.save(any(TelemetrySnapshot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        boolean result = scheduler.syncVehicleById(1L);
        
        // Then
        assertThat(result).isTrue();
        verify(snapshotRepository).save(any(TelemetrySnapshot.class));
    }

    @Test
    @DisplayName("Should return false when syncing non-existent vehicle")
    void shouldReturnFalseWhenSyncingNonExistentVehicle() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        boolean result = scheduler.syncVehicleById(999L);
        
        // Then
        assertThat(result).isFalse();
        verify(snapshotRepository, never()).save(any(TelemetrySnapshot.class));
    }

    @Test
    @DisplayName("Should track statistics correctly")
    void shouldTrackStatisticsCorrectly() {
        // Given
        TelemetrySyncScheduler.SyncStatistics initialStats = scheduler.getStatistics();
        
        // Then
        assertThat(initialStats.totalRuns()).isEqualTo(0);
        assertThat(initialStats.totalVehiclesSynced()).isEqualTo(0);
        assertThat(initialStats.totalVehiclesFailed()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should register metrics in meter registry")
    void shouldRegisterMetricsInMeterRegistry() {
        // Given/When - metrics registered in setUp via init()
        
        // Then
        assertThat(meterRegistry.find("telematics.sync.success").counter()).isNotNull();
        assertThat(meterRegistry.find("telematics.sync.failure").counter()).isNotNull();
        assertThat(meterRegistry.find("telematics.snapshots.saved").counter()).isNotNull();
        assertThat(meterRegistry.find("telematics.sync.duration").timer()).isNotNull();
    }

    @Test
    @DisplayName("Should reset backoff for vehicle")
    void shouldResetBackoffForVehicle() {
        // Given/When
        scheduler.resetVehicleBackoff(1L);
        
        // Then - no exception should be thrown
        TelemetrySyncScheduler.SyncStatistics stats = scheduler.getStatistics();
        assertThat(stats.vehiclesInBackoff()).isEqualTo(0);
    }

    // ===== HELPER METHODS =====

    private Vehicle createTestVehicle(Long id, Vehicle.TelemetrySource source) {
        return Vehicle.builder()
            .id(id)
            .companyId(100L)
            .vehicleNumber("KA-01-AB-" + String.format("%04d", id))
            .make("Tata")
            .model("Tigor EV")
            .type(Vehicle.VehicleType.LCV)
            .status(Vehicle.VehicleStatus.ACTIVE)
            .telemetrySource(source)
            .telematicsDeviceImei("86000000000" + id)
            .build();
    }

    private VehicleTelemetryData createTestTelemetryData() {
        VehicleTelemetryData data = new VehicleTelemetryData();
        data.setTimestamp(LocalDateTime.now());
        data.setLatitude(12.9716);
        data.setLongitude(77.5946);
        data.setSpeed(45.0);
        data.setBatterySoc(75.0);
        data.setOdometer(10000.0);
        data.setIsCharging(false);
        data.setEngineRunning(true);
        data.setGpsValid(true);
        return data;
    }
}
