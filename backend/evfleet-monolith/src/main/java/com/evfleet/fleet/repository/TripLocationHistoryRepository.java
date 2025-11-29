package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.TripLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TripLocationHistory entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface TripLocationHistoryRepository extends JpaRepository<TripLocationHistory, Long> {

    /**
     * Find all location history for a trip ordered by sequence
     */
    List<TripLocationHistory> findByTripIdOrderBySequenceNumberAsc(Long tripId);

    /**
     * Find the last recorded location for a trip
     */
    Optional<TripLocationHistory> findFirstByTripIdOrderBySequenceNumberDesc(Long tripId);

    /**
     * Get the count of location points for a trip
     */
    @Query("SELECT COUNT(h) FROM TripLocationHistory h WHERE h.tripId = :tripId")
    Integer countByTripId(Long tripId);

    /**
     * Get the total distance traveled for a trip
     */
    @Query("SELECT COALESCE(SUM(h.distanceFromPrevious), 0) FROM TripLocationHistory h WHERE h.tripId = :tripId")
    Double getTotalDistanceByTripId(Long tripId);

    /**
     * Find location points with teleportation warnings
     */
    List<TripLocationHistory> findByTripIdAndTeleportationWarningTrue(Long tripId);

    /**
     * Delete all location history for a trip
     */
    void deleteByTripId(Long tripId);
}
