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

    // ===== PR 4: Multi-fuel Queries =====

    /**
     * Find vehicles by fuel type
     * @param fuelType The fuel type to filter by (ICE, EV, HYBRID)
     * @return List of vehicles with the specified fuel type
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<Vehicle> findByFuelType(com.evfleet.fleet.model.FuelType fuelType);

    /**
     * Find vehicles by company and fuel type
     * @param companyId The company ID
     * @param fuelType The fuel type to filter by
     * @return List of vehicles for the company with specified fuel type
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<Vehicle> findByCompanyIdAndFuelType(Long companyId, com.evfleet.fleet.model.FuelType fuelType);

    /**
     * Get fleet composition by fuel type for a company
     * Returns count of vehicles by each fuel type
     * @param companyId The company ID
     * @return Map of fuel type counts (FuelType -> Count)
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    @Query("SELECT v.fuelType as fuelType, COUNT(v) as count FROM Vehicle v WHERE v.companyId = :companyId GROUP BY v.fuelType")
    List<Object[]> getFleetCompositionByCompany(@Param("companyId") Long companyId);

    /**
     * Find EV/HYBRID vehicles with low battery
     * Filters only vehicles that support battery (EV and HYBRID)
     * @param companyId The company ID
     * @param threshold Battery threshold percentage
     * @return List of EV/HYBRID vehicles with battery below threshold
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId " +
           "AND (v.fuelType = 'EV' OR v.fuelType = 'HYBRID') " +
           "AND v.currentBatterySoc < :threshold")
    List<Vehicle> findLowBatteryVehicles(@Param("companyId") Long companyId, @Param("threshold") Double threshold);

    /**
     * Find ICE/HYBRID vehicles with low fuel
     * Filters only vehicles that support fuel (ICE and HYBRID)
     * @param companyId The company ID
     * @param thresholdPercentage Fuel level threshold as percentage of tank capacity
     * @return List of ICE/HYBRID vehicles with fuel below threshold
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId " +
           "AND (v.fuelType = 'ICE' OR v.fuelType = 'HYBRID') " +
           "AND v.fuelTankCapacity IS NOT NULL " +
           "AND v.fuelLevel IS NOT NULL " +
           "AND (v.fuelLevel / v.fuelTankCapacity * 100) < :thresholdPercentage")
    List<Vehicle> findLowFuelVehicles(@Param("companyId") Long companyId, @Param("thresholdPercentage") Double thresholdPercentage);

    /**
     * Count vehicles by fuel type for a company
     * @param companyId The company ID
     * @param fuelType The fuel type
     * @return Count of vehicles with specified fuel type
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    long countByCompanyIdAndFuelType(Long companyId, com.evfleet.fleet.model.FuelType fuelType);
}
