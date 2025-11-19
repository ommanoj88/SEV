package com.evfleet.charging.repository;

import com.evfleet.charging.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ChargingSession entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByVehicleId(Long vehicleId);

    List<ChargingSession> findByStationId(Long stationId);

    List<ChargingSession> findByCompanyId(Long companyId);

    List<ChargingSession> findByStatus(ChargingSession.SessionStatus status);

    Optional<ChargingSession> findByVehicleIdAndStatus(Long vehicleId, ChargingSession.SessionStatus status);

    @Query("SELECT s FROM ChargingSession s WHERE s.companyId = :companyId " +
            "AND s.startTime BETWEEN :startDate AND :endDate")
    List<ChargingSession> findByCompanyAndDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
    
    // E5 Energy Analytics support
    @Query("SELECT s FROM ChargingSession s WHERE s.vehicleId = :vehicleId " +
            "AND s.startTime BETWEEN :startDate AND :endDate")
    List<ChargingSession> findByVehicleIdAndStartTimeBetween(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
}
