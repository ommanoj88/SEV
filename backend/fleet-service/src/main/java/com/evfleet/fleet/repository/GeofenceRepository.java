package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Geofence entity
 */
@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    /**
     * Find all geofences by company ID
     */
    List<Geofence> findByCompanyId(Long companyId);

    /**
     * Find active geofences by company ID
     */
    List<Geofence> findByCompanyIdAndActive(Long companyId, Boolean active);

    /**
     * Find geofences by type
     */
    List<Geofence> findByType(Geofence.GeofenceType type);

    /**
     * Find geofences by company and type
     */
    List<Geofence> findByCompanyIdAndType(Long companyId, Geofence.GeofenceType type);

    /**
     * Find active geofences by type
     */
    List<Geofence> findByCompanyIdAndTypeAndActive(Long companyId, Geofence.GeofenceType type, Boolean active);

    /**
     * Count geofences by company
     */
    long countByCompanyId(Long companyId);

    /**
     * Find geofences by shape
     */
    List<Geofence> findByShape(Geofence.GeofenceShape shape);

    /**
     * Find geofences with alerts enabled
     */
    @Query("SELECT g FROM Geofence g WHERE g.companyId = :companyId " +
           "AND g.active = true " +
           "AND (g.alertOnEntry = true OR g.alertOnExit = true)")
    List<Geofence> findGeofencesWithAlerts(@Param("companyId") Long companyId);

    /**
     * Find geofences near a location
     */
    @Query("SELECT g FROM Geofence g WHERE g.active = true " +
           "AND g.centerLatitude BETWEEN :minLat AND :maxLat " +
           "AND g.centerLongitude BETWEEN :minLng AND :maxLng")
    List<Geofence> findGeofencesNearLocation(@Param("minLat") Double minLat,
                                              @Param("maxLat") Double maxLat,
                                              @Param("minLng") Double minLng,
                                              @Param("maxLng") Double maxLng);
}
