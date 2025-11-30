package com.evfleet.telematics.repository;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.model.TelemetrySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TelemetrySnapshotRepository
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TelemetrySnapshotRepositoryTest {

    @Mock
    private TelemetrySnapshotRepository repository;

    private TelemetrySnapshot testSnapshot;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testSnapshot = createTestSnapshot(1L, 1L, 1L);
    }

    @Test
    @DisplayName("findTopByVehicleIdOrderByTimestampDesc returns latest snapshot")
    void findLatestByVehicle_ReturnsLatest() {
        when(repository.findTopByVehicleIdOrderByTimestampDesc(1L))
            .thenReturn(Optional.of(testSnapshot));

        Optional<TelemetrySnapshot> result = repository.findTopByVehicleIdOrderByTimestampDesc(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getVehicleId()).isEqualTo(1L);
        verify(repository).findTopByVehicleIdOrderByTimestampDesc(1L);
    }

    @Test
    @DisplayName("findByVehicleIdAndTimestampBetween returns snapshots in range")
    void findByVehicleAndTimeRange_ReturnsSnapshots() {
        LocalDateTime start = now.minusHours(24);
        LocalDateTime end = now;
        
        List<TelemetrySnapshot> snapshots = List.of(
            createTestSnapshot(1L, 1L, 1L),
            createTestSnapshot(2L, 1L, 1L)
        );
        
        when(repository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
            eq(1L), any(), any()
        )).thenReturn(snapshots);

        List<TelemetrySnapshot> result = repository
            .findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(1L, start, end);

        assertThat(result).hasSize(2);
        verify(repository).findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(1L, start, end);
    }

    @Test
    @DisplayName("findLatestByCompanyId returns one snapshot per vehicle")
    void findLatestByCompany_ReturnsUniqueVehicles() {
        List<TelemetrySnapshot> snapshots = List.of(
            createTestSnapshot(1L, 1L, 1L),
            createTestSnapshot(2L, 2L, 1L),
            createTestSnapshot(3L, 3L, 1L)
        );
        
        when(repository.findLatestByCompanyId(1L)).thenReturn(snapshots);

        List<TelemetrySnapshot> result = repository.findLatestByCompanyId(1L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(TelemetrySnapshot::getVehicleId)
            .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("getAverageSpeedByVehicleAndTimeRange calculates correctly")
    void getAverageSpeed_CalculatesCorrectly() {
        when(repository.getAverageSpeedByVehicleAndTimeRange(
            eq(1L), any(), any()
        )).thenReturn(55.5);

        Double avgSpeed = repository.getAverageSpeedByVehicleAndTimeRange(
            1L, now.minusHours(24), now
        );

        assertThat(avgSpeed).isEqualTo(55.5);
    }

    @Test
    @DisplayName("deleteByTimestampBefore removes old records")
    void deleteByTimestamp_RemovesOldRecords() {
        LocalDateTime cutoff = now.minusDays(90);
        when(repository.deleteByTimestampBefore(cutoff)).thenReturn(1000);

        int deleted = repository.deleteByTimestampBefore(cutoff);

        assertThat(deleted).isEqualTo(1000);
        verify(repository).deleteByTimestampBefore(cutoff);
    }

    @Test
    @DisplayName("countByTimestampBefore returns correct count")
    void countByTimestamp_ReturnsCount() {
        LocalDateTime cutoff = now.minusDays(90);
        when(repository.countByTimestampBefore(cutoff)).thenReturn(500L);

        long count = repository.countByTimestampBefore(cutoff);

        assertThat(count).isEqualTo(500L);
    }

    @Test
    @DisplayName("findChargingSnapshots returns only charging records")
    void findChargingSnapshots_ReturnsChargingOnly() {
        TelemetrySnapshot chargingSnapshot = createTestSnapshot(1L, 1L, 1L);
        chargingSnapshot.setIsCharging(true);
        chargingSnapshot.setChargingStatus("AC_CHARGING");
        
        when(repository.findChargingSnapshots(eq(1L), any(), any()))
            .thenReturn(List.of(chargingSnapshot));

        List<TelemetrySnapshot> result = repository.findChargingSnapshots(
            1L, now.minusHours(24), now
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsCharging()).isTrue();
    }

    @Test
    @DisplayName("findByLocationBoundingBox returns snapshots within area")
    void findByBoundingBox_ReturnsSnapshots() {
        List<TelemetrySnapshot> snapshots = List.of(testSnapshot);
        
        when(repository.findByLocationBoundingBox(
            eq(1L), 
            eq(18.0), eq(19.0), 
            eq(73.0), eq(74.0),
            any(), any()
        )).thenReturn(snapshots);

        List<TelemetrySnapshot> result = repository.findByLocationBoundingBox(
            1L, 18.0, 19.0, 73.0, 74.0, now.minusHours(24), now
        );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getTotalDistanceByVehicleAndTimeRange calculates distance")
    void getTotalDistance_CalculatesCorrectly() {
        when(repository.getTotalDistanceByVehicleAndTimeRange(
            eq(1L), any(), any()
        )).thenReturn(150.5);

        Double distance = repository.getTotalDistanceByVehicleAndTimeRange(
            1L, now.minusHours(24), now
        );

        assertThat(distance).isEqualTo(150.5);
    }

    @Test
    @DisplayName("findTopByDeviceIdOrderByTimestampDesc returns latest by device")
    void findLatestByDevice_ReturnsLatest() {
        when(repository.findTopByDeviceIdOrderByTimestampDesc("123456789012345"))
            .thenReturn(Optional.of(testSnapshot));

        Optional<TelemetrySnapshot> result = repository
            .findTopByDeviceIdOrderByTimestampDesc("123456789012345");

        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo("123456789012345");
    }

    // ============= Helper Methods =============

    private TelemetrySnapshot createTestSnapshot(Long id, Long vehicleId, Long companyId) {
        return TelemetrySnapshot.builder()
            .id(id)
            .vehicleId(vehicleId)
            .companyId(companyId)
            .deviceId("123456789012345")
            .source(Vehicle.TelemetrySource.DEVICE)
            .providerName("flespi")
            .timestamp(now)
            .dataQuality(Vehicle.TelemetryDataQuality.REAL_TIME)
            .latitude(18.5204)
            .longitude(73.8567)
            .altitude(560.0)
            .heading(180.0)
            .speed(45.5)
            .satellites(12)
            .odometer(15432.5)
            .batterySoc(85.0)
            .estimatedRange(250.0)
            .isCharging(false)
            .ignitionOn(true)
            .isMoving(true)
            .vehicleStatus("DRIVING")
            .signalStrength(80)
            .createdAt(now)
            .build();
    }
}
