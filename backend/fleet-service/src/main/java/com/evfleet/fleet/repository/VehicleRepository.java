package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vehicle entity
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find all vehicles by company ID
     */
    List<Vehicle> findByCompanyId(Long companyId);

    /**
     * Find vehicle by vehicle number
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    /**
     * Find vehicles by company and status
     */
    List<Vehicle> findByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);

    /**
     * Find vehicles by type
     */
    List<Vehicle> findByType(Vehicle.VehicleType type);

    /**
     * Find vehicles by company and type
     */
    List<Vehicle> findByCompanyIdAndType(Long companyId, Vehicle.VehicleType type);

    /**
     * Find vehicles with low battery
     */
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND v.currentBatterySoc < :threshold")
    List<Vehicle> findVehiclesWithLowBattery(@Param("companyId") Long companyId, @Param("threshold") Double threshold);

    /**
     * Find active vehicles by company
     */
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND v.status = 'ACTIVE'")
    List<Vehicle> findActiveVehiclesByCompany(@Param("companyId") Long companyId);

    /**
     * Find vehicles currently in trip
     */
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    /**
     * Count vehicles by company and status
     */
    long countByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);

    /**
     * Check if vehicle number exists
     */
    boolean existsByVehicleNumber(String vehicleNumber);

    /**
     * Find vehicles by current driver
     */
    Optional<Vehicle> findByCurrentDriverId(Long driverId);

    /**
     * Find vehicles within a geographic area
     */
    @Query("SELECT v FROM Vehicle v WHERE v.latitude BETWEEN :minLat AND :maxLat " +
           "AND v.longitude BETWEEN :minLng AND :maxLng")
    List<Vehicle> findVehiclesInArea(@Param("minLat") Double minLat,
                                     @Param("maxLat") Double maxLat,
                                     @Param("minLng") Double minLng,
                                     @Param("maxLng") Double maxLng);
}
