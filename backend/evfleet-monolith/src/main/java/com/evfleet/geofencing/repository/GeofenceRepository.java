package com.evfleet.geofencing.repository;

import com.evfleet.geofencing.model.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Geofence entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    /**
     * Find all geofences for a company
     */
    List<Geofence> findByCompanyId(Long companyId);

    /**
     * Find active geofences for a company
     */
    List<Geofence> findByCompanyIdAndIsActiveTrue(Long companyId);

    /**
     * Find geofences by type
     */
    List<Geofence> findByGeofenceType(Geofence.GeofenceType geofenceType);

    /**
     * Find active geofences by type for a company
     */
    List<Geofence> findByCompanyIdAndGeofenceTypeAndIsActiveTrue(
        Long companyId, Geofence.GeofenceType geofenceType);

    /**
     * Check if geofence name exists for a company
     */
    boolean existsByCompanyIdAndName(Long companyId, String name);
}
