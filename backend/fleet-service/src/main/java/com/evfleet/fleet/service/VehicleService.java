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
}
