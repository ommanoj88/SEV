package com.evfleet.telematics.repository;

import com.evfleet.telematics.model.TelemetrySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TelemetrySnapshot entity.
 * Provides efficient queries for historical telemetry data.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface TelemetrySnapshotRepository extends JpaRepository<TelemetrySnapshot, Long> {

    // ===== SINGLE VEHICLE QUERIES =====

    /**
     * Get latest telemetry snapshot for a vehicle
     */
    Optional<TelemetrySnapshot> findTopByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Get telemetry history for a vehicle within a time range
     */
    List<TelemetrySnapshot> findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
        Long vehicleId, 
        LocalDateTime start, 
        LocalDateTime end
    );

    /**
     * Get paginated telemetry history for a vehicle
     */
    Page<TelemetrySnapshot> findByVehicleIdOrderByTimestampDesc(Long vehicleId, Pageable pageable);

    /**
     * Count snapshots for a vehicle in a time range
     */
    long countByVehicleIdAndTimestampBetween(Long vehicleId, LocalDateTime start, LocalDateTime end);

    // ===== COMPANY-WIDE QUERIES =====

    /**
     * Get all snapshots for a company within a time range
     */
    List<TelemetrySnapshot> findByCompanyIdAndTimestampBetweenOrderByTimestampDesc(
        Long companyId, 
        LocalDateTime start, 
        LocalDateTime end
    );

    /**
     * Get latest snapshot for each vehicle in a company
     */
    @Query("""
        SELECT t FROM TelemetrySnapshot t 
        WHERE t.companyId = :companyId 
        AND t.timestamp = (
            SELECT MAX(t2.timestamp) FROM TelemetrySnapshot t2 
            WHERE t2.vehicleId = t.vehicleId
        )
        """)
    List<TelemetrySnapshot> findLatestByCompanyId(@Param("companyId") Long companyId);

    // ===== ANALYTICS QUERIES =====

    /**
     * Get average speed for a vehicle in a time range
     */
    @Query("""
        SELECT AVG(t.speed) FROM TelemetrySnapshot t 
        WHERE t.vehicleId = :vehicleId 
        AND t.timestamp BETWEEN :start AND :end 
        AND t.speed IS NOT NULL
        """)
    Double getAverageSpeedByVehicleAndTimeRange(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Get total distance traveled by a vehicle in a time range
     */
    @Query("""
        SELECT MAX(t.odometer) - MIN(t.odometer) FROM TelemetrySnapshot t 
        WHERE t.vehicleId = :vehicleId 
        AND t.timestamp BETWEEN :start AND :end 
        AND t.odometer IS NOT NULL
        """)
    Double getTotalDistanceByVehicleAndTimeRange(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Get average battery SOC for a vehicle in a time range
     */
    @Query("""
        SELECT AVG(t.batterySoc) FROM TelemetrySnapshot t 
        WHERE t.vehicleId = :vehicleId 
        AND t.timestamp BETWEEN :start AND :end 
        AND t.batterySoc IS NOT NULL
        """)
    Double getAverageBatterySocByVehicleAndTimeRange(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ===== GEOSPATIAL QUERIES =====

    /**
     * Find snapshots within a bounding box
     */
    @Query("""
        SELECT t FROM TelemetrySnapshot t 
        WHERE t.companyId = :companyId 
        AND t.latitude BETWEEN :minLat AND :maxLat 
        AND t.longitude BETWEEN :minLon AND :maxLon 
        AND t.timestamp BETWEEN :start AND :end
        ORDER BY t.timestamp DESC
        """)
    List<TelemetrySnapshot> findByLocationBoundingBox(
        @Param("companyId") Long companyId,
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon,
        @Param("maxLon") Double maxLon,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // ===== RETENTION POLICY =====

    /**
     * Delete all snapshots older than specified date (for retention policy)
     * Should be called by scheduled job
     */
    @Modifying
    @Query("DELETE FROM TelemetrySnapshot t WHERE t.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count snapshots older than specified date (for reporting)
     */
    long countByTimestampBefore(LocalDateTime cutoffDate);

    // ===== DEVICE-BASED QUERIES =====

    /**
     * Find snapshots by device IMEI
     */
    List<TelemetrySnapshot> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
        String deviceId, 
        LocalDateTime start, 
        LocalDateTime end
    );

    /**
     * Get latest snapshot by device IMEI
     */
    Optional<TelemetrySnapshot> findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    // ===== CHARGING SESSION ANALYSIS =====

    /**
     * Find charging snapshots for a vehicle
     */
    @Query("""
        SELECT t FROM TelemetrySnapshot t 
        WHERE t.vehicleId = :vehicleId 
        AND t.isCharging = true 
        AND t.timestamp BETWEEN :start AND :end
        ORDER BY t.timestamp
        """)
    List<TelemetrySnapshot> findChargingSnapshots(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Count charging events for a vehicle
     */
    @Query("""
        SELECT COUNT(DISTINCT DATE(t.timestamp)) FROM TelemetrySnapshot t 
        WHERE t.vehicleId = :vehicleId 
        AND t.isCharging = true 
        AND t.timestamp BETWEEN :start AND :end
        """)
    long countChargingDays(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
