package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.VehicleCurrentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for VehicleCurrentState entity
 * Provides data access for current vehicle state
 */
@Repository
public interface VehicleCurrentStateRepository extends JpaRepository<VehicleCurrentState, Long> {

    /**
     * Find current state by vehicle ID
     */
    Optional<VehicleCurrentState> findByVehicleId(Long vehicleId);

    /**
     * Find all current states for a company
     */
    List<VehicleCurrentState> findByCompanyId(Long companyId);

    /**
     * Find vehicles currently charging
     */
    List<VehicleCurrentState> findByIsChargingTrue();

    /**
     * Find vehicles currently in maintenance
     */
    List<VehicleCurrentState> findByIsInMaintenanceTrue();

    /**
     * Find vehicles currently in trip
     */
    List<VehicleCurrentState> findByIsInTripTrue();

    /**
     * Find vehicles with active alerts
     */
    @Query("SELECT v FROM VehicleCurrentState v WHERE v.activeAlertsCount > 0")
    List<VehicleCurrentState> findVehiclesWithActiveAlerts();

    /**
     * Find vehicles with critical alerts
     */
    @Query("SELECT v FROM VehicleCurrentState v WHERE v.criticalAlertsCount > 0")
    List<VehicleCurrentState> findVehiclesWithCriticalAlerts();

    /**
     * Find offline/disconnected vehicles
     */
    @Query("SELECT v FROM VehicleCurrentState v WHERE v.isConnected = false")
    List<VehicleCurrentState> findDisconnectedVehicles();

    /**
     * Find vehicles with low battery
     */
    @Query("SELECT v FROM VehicleCurrentState v WHERE v.batterySoc < :threshold AND v.batterySoc IS NOT NULL")
    List<VehicleCurrentState> findVehiclesWithLowBattery(@Param("threshold") Double threshold);

    /**
     * Find vehicles with low fuel
     */
    @Query("SELECT v FROM VehicleCurrentState v WHERE v.fuelLevel < :threshold AND v.fuelLevel IS NOT NULL")
    List<VehicleCurrentState> findVehiclesWithLowFuel(@Param("threshold") Double threshold);

    /**
     * Find vehicles by status
     */
    List<VehicleCurrentState> findByStatus(com.evfleet.fleet.model.Vehicle.VehicleStatus status);

    /**
     * Find vehicles by company and status
     */
    List<VehicleCurrentState> findByCompanyIdAndStatus(
            Long companyId, com.evfleet.fleet.model.Vehicle.VehicleStatus status);
}
