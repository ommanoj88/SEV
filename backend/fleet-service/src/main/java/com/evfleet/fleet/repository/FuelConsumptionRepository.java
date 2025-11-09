package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.FuelConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FuelConsumption entity
 * Provides data access methods for fuel consumption tracking
 * 
 * @since 2.0.0 (Multi-fuel support)
 */
@Repository
public interface FuelConsumptionRepository extends JpaRepository<FuelConsumption, Long> {

    /**
     * Find all fuel consumption records for a specific vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return list of fuel consumption records
     */
    List<FuelConsumption> findByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Find fuel consumption records for a vehicle within a date range
     * 
     * @param vehicleId the vehicle ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of fuel consumption records
     */
    List<FuelConsumption> findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
        Long vehicleId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );

    /**
     * Find all fuel consumption records for a specific trip
     * 
     * @param tripId the trip ID
     * @return list of fuel consumption records
     */
    List<FuelConsumption> findByTripId(Long tripId);

    /**
     * Calculate total fuel consumed by a vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return total fuel consumed in liters
     */
    @Query("SELECT SUM(fc.fuelConsumedLiters) FROM FuelConsumption fc WHERE fc.vehicleId = :vehicleId")
    Double getTotalFuelConsumed(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total fuel cost for a vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return total cost in INR
     */
    @Query("SELECT SUM(fc.costInr) FROM FuelConsumption fc WHERE fc.vehicleId = :vehicleId")
    Double getTotalFuelCost(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total fuel consumed by a vehicle within a date range
     * 
     * @param vehicleId the vehicle ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @return total fuel consumed in liters
     */
    @Query("SELECT SUM(fc.fuelConsumedLiters) FROM FuelConsumption fc " +
           "WHERE fc.vehicleId = :vehicleId AND fc.timestamp BETWEEN :startDate AND :endDate")
    Double getTotalFuelConsumedInPeriod(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calculate average fuel efficiency for a vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return average fuel efficiency in kmpl
     */
    @Query("SELECT AVG(fc.fuelEfficiencyKmpl) FROM FuelConsumption fc " +
           "WHERE fc.vehicleId = :vehicleId AND fc.fuelEfficiencyKmpl IS NOT NULL")
    Double getAverageFuelEfficiency(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total CO2 emissions for a vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return total CO2 emissions in kg
     */
    @Query("SELECT SUM(fc.co2EmissionsKg) FROM FuelConsumption fc WHERE fc.vehicleId = :vehicleId")
    Double getTotalCO2Emissions(@Param("vehicleId") Long vehicleId);

    /**
     * Find latest fuel consumption record for a vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return latest fuel consumption record
     */
    @Query("SELECT fc FROM FuelConsumption fc " +
           "WHERE fc.vehicleId = :vehicleId " +
           "ORDER BY fc.timestamp DESC")
    List<FuelConsumption> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);
}
