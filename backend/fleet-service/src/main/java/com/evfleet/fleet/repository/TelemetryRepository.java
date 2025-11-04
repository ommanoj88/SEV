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
}
