package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.model.Vehicle;

import java.util.List;

/**
 * Service interface for Vehicle operations
 */
public interface VehicleService {

    /**
     * Create a new vehicle
     */
    VehicleResponse createVehicle(VehicleRequest request);

    /**
     * Update an existing vehicle
     */
    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    /**
     * Delete a vehicle
     */
    void deleteVehicle(Long id);

    /**
     * Get vehicle by ID
     */
    VehicleResponse getVehicleById(Long id);

    /**
     * Get all vehicles
     */
    List<VehicleResponse> getAllVehicles();

    /**
     * Get vehicles by company
     */
    List<VehicleResponse> getVehiclesByCompany(Long companyId);

    /**
     * Get vehicles by company and status
     */
    List<VehicleResponse> getVehiclesByCompanyAndStatus(Long companyId, Vehicle.VehicleStatus status);

    /**
     * Update vehicle location
     */
    void updateVehicleLocation(Long vehicleId, Double latitude, Double longitude);

    /**
     * Update vehicle battery SOC
     */
    void updateBatterySoc(Long vehicleId, Double soc);

    /**
     * Update vehicle status
     */
    void updateVehicleStatus(Long vehicleId, Vehicle.VehicleStatus status);

    /**
     * Assign driver to vehicle
     */
    void assignDriver(Long vehicleId, Long driverId);

    /**
     * Remove driver from vehicle
     */
    void removeDriver(Long vehicleId);

    /**
     * Get vehicles with low battery
     */
    List<VehicleResponse> getVehiclesWithLowBattery(Long companyId, Double threshold);

    /**
     * Get active vehicles by company
     */
    List<VehicleResponse> getActiveVehicles(Long companyId);

    /**
     * Get vehicle by vehicle number
     */
    VehicleResponse getVehicleByNumber(String vehicleNumber);

    // ===== PR 4: Multi-fuel Query Methods =====

    /**
     * Get vehicles by fuel type
     * @param fuelType The fuel type to filter by (ICE, EV, HYBRID)
     * @return List of vehicles with the specified fuel type
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<VehicleResponse> getVehiclesByFuelType(com.evfleet.fleet.model.FuelType fuelType);

    /**
     * Get vehicles by company and fuel type
     * @param companyId The company ID
     * @param fuelType The fuel type to filter by
     * @return List of vehicles for the company with specified fuel type
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<VehicleResponse> getVehiclesByCompanyAndFuelType(Long companyId, com.evfleet.fleet.model.FuelType fuelType);

    /**
     * Get fleet composition by fuel type for a company
     * Returns the percentage and count of each fuel type in the fleet
     * @param companyId The company ID
     * @return Map containing fleet composition statistics
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    java.util.Map<String, Object> getFleetComposition(Long companyId);

    /**
     * Get EV/HYBRID vehicles with low battery
     * Only returns vehicles that support battery charging (EV and HYBRID)
     * @param companyId The company ID
     * @param threshold Battery threshold percentage (default 20%)
     * @return List of EV/HYBRID vehicles with battery below threshold
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<VehicleResponse> getLowBatteryVehicles(Long companyId, Double threshold);

    /**
     * Get ICE/HYBRID vehicles with low fuel
     * Only returns vehicles that use fuel (ICE and HYBRID)
     * @param companyId The company ID
     * @param thresholdPercentage Fuel level threshold as percentage of tank capacity (default 20%)
     * @return List of ICE/HYBRID vehicles with fuel below threshold
     * @since 2.0.0 (PR 4: Multi-fuel support)
     */
    List<VehicleResponse> getLowFuelVehicles(Long companyId, Double thresholdPercentage);

    /**
     * Get vehicle entity by ID (for internal use)
     * Returns the actual Vehicle entity instead of DTO
     * @param id Vehicle ID
     * @return Vehicle entity
     * @since 2.0.0 (PR 6: Telemetry API support)
     */
    Vehicle getVehicleEntityById(Long id);

    /**
     * Update vehicle fuel level
     * Updates the current fuel level for ICE and HYBRID vehicles
     * @param vehicleId Vehicle ID
     * @param fuelLevel New fuel level in liters
     * @since 2.0.0 (PR 6: Telemetry API support)
     */
    void updateFuelLevel(Long vehicleId, Double fuelLevel);
}
