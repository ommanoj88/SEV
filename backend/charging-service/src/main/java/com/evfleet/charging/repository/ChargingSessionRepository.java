package com.evfleet.charging.repository;

import com.evfleet.charging.entity.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByVehicleId(Long vehicleId);

    List<ChargingSession> findByStationId(Long stationId);

    List<ChargingSession> findByStatus(ChargingSession.SessionStatus status);

    @Query("SELECT s FROM ChargingSession s WHERE s.vehicleId = :vehicleId AND s.status IN ('INITIATED', 'CHARGING')")
    Optional<ChargingSession> findActiveSessionByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT s FROM ChargingSession s WHERE s.stationId = :stationId AND s.status IN ('INITIATED', 'CHARGING')")
    List<ChargingSession> findActiveSessionsByStationId(@Param("stationId") Long stationId);

    @Query("SELECT s FROM ChargingSession s WHERE s.vehicleId = :vehicleId " +
           "AND s.startTime BETWEEN :startDate AND :endDate ORDER BY s.startTime DESC")
    List<ChargingSession> findByVehicleIdAndDateRange(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(s.energyConsumed), 0) FROM ChargingSession s " +
           "WHERE s.vehicleId = :vehicleId AND s.status = 'COMPLETED' " +
           "AND s.startTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalEnergyConsumedByVehicle(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(s.cost), 0) FROM ChargingSession s " +
           "WHERE s.vehicleId = :vehicleId AND s.status = 'COMPLETED' " +
           "AND s.startTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCostByVehicle(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(s) FROM ChargingSession s WHERE s.stationId = :stationId " +
           "AND s.status = 'COMPLETED'")
    Long countCompletedSessionsByStation(@Param("stationId") Long stationId);
}
