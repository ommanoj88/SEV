package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vehicle entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find vehicle by vehicle number
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    /**
     * Find all vehicles belonging to a company
     */
    List<Vehicle> findByCompanyId(Long companyId);

    /**
     * Find vehicles by status
     */
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    /**
     * Find vehicles by fuel type
     */
    List<Vehicle> findByFuelType(FuelType fuelType);

    /**
     * Find vehicles by company and status
     */
    List<Vehicle> findByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);

    /**
     * Check if vehicle number exists
     */
    boolean existsByVehicleNumber(String vehicleNumber);

    /**
     * Check if license plate exists
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Check if VIN exists
     */
    boolean existsByVin(String vin);

    /**
     * Find all active vehicles for a company
     */
    @Query("SELECT v FROM Vehicle v WHERE v.companyId = :companyId AND v.status = 'ACTIVE'")
    List<Vehicle> findActiveVehiclesByCompany(Long companyId);
}
