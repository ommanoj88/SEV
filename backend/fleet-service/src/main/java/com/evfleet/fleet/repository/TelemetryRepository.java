package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.TelemetryData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TelemetryData entity
 */
@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryData, Long> {

    /**
     * Find telemetry data by vehicle ID
     */
    List<TelemetryData> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find telemetry data by vehicle ID within time range
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findByVehicleIdAndTimestampBetween(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find latest telemetry data for a vehicle
     */
    Optional<TelemetryData> findFirstByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Find telemetry data by trip ID
     */
    List<TelemetryData> findByTripIdOrderByTimestampAsc(Long tripId);

    /**
     * Find telemetry data with errors
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.errorCodes IS NOT NULL AND t.errorCodes != '' " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findTelemetryWithErrors(@Param("vehicleId") Long vehicleId, Pageable pageable);

    /**
     * Calculate average speed for a trip
     */
    @Query("SELECT AVG(t.speed) FROM TelemetryData t WHERE t.tripId = :tripId AND t.speed > 0")
    Double calculateAverageSpeedForTrip(@Param("tripId") Long tripId);

    /**
     * Find maximum speed for a trip
     */
    @Query("SELECT MAX(t.speed) FROM TelemetryData t WHERE t.tripId = :tripId")
    Double findMaxSpeedForTrip(@Param("tripId") Long tripId);

    /**
     * Calculate total distance for a trip
     */
    @Query("SELECT MAX(t.odometer) - MIN(t.odometer) FROM TelemetryData t WHERE t.tripId = :tripId")
    Double calculateDistanceForTrip(@Param("tripId") Long tripId);

    /**
     * Delete old telemetry data before a certain date
     */
    void deleteByTimestampBefore(LocalDateTime timestamp);

    /**
     * Count telemetry records by vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Find telemetry with low battery
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.batterySoc < :threshold " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findLowBatteryTelemetry(@Param("vehicleId") Long vehicleId,
                                                 @Param("threshold") Double threshold,
                                                 Pageable pageable);

    // ===== ICE-SPECIFIC TELEMETRY QUERIES =====
    /**
     * Find telemetry with low fuel level
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.fuelLevel IS NOT NULL AND t.fuelLevel < :threshold " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findLowFuelTelemetry(@Param("vehicleId") Long vehicleId,
                                             @Param("threshold") Double threshold,
                                             Pageable pageable);

    /**
     * Find telemetry with high engine temperature
     * Used for engine overheating alerts
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.engineTemperature IS NOT NULL AND t.engineTemperature > :threshold " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findHighEngineTemperature(@Param("vehicleId") Long vehicleId,
                                                   @Param("threshold") Double threshold,
                                                   Pageable pageable);

    /**
     * Calculate average engine RPM for a trip
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT AVG(t.engineRpm) FROM TelemetryData t WHERE t.tripId = :tripId " +
           "AND t.engineRpm IS NOT NULL AND t.engineRpm > 0")
    Double calculateAverageEngineRpmForTrip(@Param("tripId") Long tripId);

    /**
     * Calculate average engine load for a trip
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT AVG(t.engineLoad) FROM TelemetryData t WHERE t.tripId = :tripId " +
           "AND t.engineLoad IS NOT NULL")
    Double calculateAverageEngineLoadForTrip(@Param("tripId") Long tripId);

    /**
     * Find latest telemetry with engine data for a vehicle
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND t.engineRpm IS NOT NULL " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findLatestEngineTelemetry(@Param("vehicleId") Long vehicleId,
                                                   Pageable pageable);

    /**
     * Find telemetry with engine diagnostics issues
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t FROM TelemetryData t WHERE t.vehicleId = :vehicleId " +
           "AND (t.engineTemperature > :tempThreshold OR t.engineLoad > :loadThreshold) " +
           "ORDER BY t.timestamp DESC")
    List<TelemetryData> findEngineDiagnosticIssues(@Param("vehicleId") Long vehicleId,
                                                     @Param("tempThreshold") Double tempThreshold,
                                                     @Param("loadThreshold") Double loadThreshold,
                                                     Pageable pageable);

    /**
     * Get starting fuel level for a time period (earliest timestamp)
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t.fuelLevel FROM TelemetryData t " +
           "WHERE t.vehicleId = :vehicleId " +
           "AND t.timestamp >= :startTime " +
           "AND t.fuelLevel IS NOT NULL " +
           "ORDER BY t.timestamp ASC")
    List<Double> findStartingFuelLevel(@Param("vehicleId") Long vehicleId,
                                       @Param("startTime") LocalDateTime startTime,
                                       Pageable pageable);

    /**
     * Get ending fuel level for a time period (latest timestamp)
     * @since 2.0.0 (Multi-fuel support)
     */
    @Query("SELECT t.fuelLevel FROM TelemetryData t " +
           "WHERE t.vehicleId = :vehicleId " +
           "AND t.timestamp <= :endTime " +
           "AND t.fuelLevel IS NOT NULL " +
           "ORDER BY t.timestamp DESC")
    List<Double> findEndingFuelLevel(@Param("vehicleId") Long vehicleId,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);
}
