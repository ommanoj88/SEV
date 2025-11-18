package com.evfleet.charging.repository;

import com.evfleet.charging.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChargingStation entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    List<ChargingStation> findByStatus(ChargingStation.StationStatus status);

    @Query("SELECT s FROM ChargingStation s WHERE s.status = 'AVAILABLE' AND s.availableSlots > 0")
    List<ChargingStation> findAvailableStations();

    @Query(value = "SELECT * FROM charging_stations WHERE status = 'AVAILABLE' " +
            "AND available_slots > 0 " +
            "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(latitude)))) LIMIT :limit", nativeQuery = true)
    List<ChargingStation> findNearbyStations(Double latitude, Double longitude, int limit);
}
