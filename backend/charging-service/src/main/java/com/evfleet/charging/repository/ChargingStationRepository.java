package com.evfleet.charging.repository;

import com.evfleet.charging.entity.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    List<ChargingStation> findByStatus(ChargingStation.StationStatus status);

    List<ChargingStation> findByProvider(String provider);

    @Query("SELECT s FROM ChargingStation s WHERE s.availableSlots > 0 AND s.status = 'ACTIVE'")
    List<ChargingStation> findAvailableStations();

    @Query(value = "SELECT * FROM charging_stations s WHERE s.status = 'ACTIVE' " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) * " +
           "cos(radians(s.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(s.latitude)))) ASC LIMIT :limit", nativeQuery = true)
    List<ChargingStation> findNearestStations(
        @Param("lat") Double latitude,
        @Param("lng") Double longitude,
        @Param("limit") Integer limit
    );

    @Query("SELECT s FROM ChargingStation s WHERE s.status = 'ACTIVE' AND s.availableSlots > 0 " +
           "AND s.latitude BETWEEN :minLat AND :maxLat " +
           "AND s.longitude BETWEEN :minLng AND :maxLng")
    List<ChargingStation> findStationsInBounds(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng
    );

    @Query("SELECT COUNT(s) FROM ChargingStation s WHERE s.status = :status")
    Long countByStatus(@Param("status") ChargingStation.StationStatus status);

    List<ChargingStation> findByConnectorType(String connectorType);
}
