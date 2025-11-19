package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.BatteryHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Battery Health data access
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface BatteryHealthRepository extends JpaRepository<BatteryHealth, Long> {

    /**
     * Find all battery health records for a specific vehicle
     */
    List<BatteryHealth> findByVehicleIdOrderByRecordedAtDesc(Long vehicleId);

    /**
     * Find battery health records for a vehicle within a date range
     */
    List<BatteryHealth> findByVehicleIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find the most recent battery health record for a vehicle
     */
    Optional<BatteryHealth> findFirstByVehicleIdOrderByRecordedAtDesc(Long vehicleId);

    /**
     * Find vehicles with SOH below a threshold
     */
    @Query("SELECT DISTINCT bh.vehicleId FROM BatteryHealth bh WHERE bh.id IN " +
           "(SELECT MAX(bh2.id) FROM BatteryHealth bh2 GROUP BY bh2.vehicleId) " +
           "AND bh.soh < :threshold")
    List<Long> findVehicleIdsWithSohBelow(Double threshold);
}
