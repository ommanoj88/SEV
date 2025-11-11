package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.VehicleCurrentStateResponse;
import com.evfleet.fleet.model.VehicleCurrentState;

import java.util.List;

/**
 * Service interface for managing vehicle current state
 */
public interface VehicleCurrentStateService {

    /**
     * Update vehicle current state
     */
    VehicleCurrentStateResponse updateState(VehicleCurrentState state);

    /**
     * Get current state for a vehicle
     */
    VehicleCurrentStateResponse getVehicleCurrentState(Long vehicleId);

    /**
     * Get all current states for a company
     */
    List<VehicleCurrentStateResponse> getCompanyVehicleStates(Long companyId);

    /**
     * Get vehicles currently charging
     */
    List<VehicleCurrentStateResponse> getChargingVehicles();

    /**
     * Get vehicles currently in maintenance
     */
    List<VehicleCurrentStateResponse> getMaintenanceVehicles();

    /**
     * Get vehicles currently in trip
     */
    List<VehicleCurrentStateResponse> getVehiclesInTrip();

    /**
     * Get vehicles with active alerts
     */
    List<VehicleCurrentStateResponse> getVehiclesWithAlerts();

    /**
     * Get vehicles with critical alerts
     */
    List<VehicleCurrentStateResponse> getVehiclesWithCriticalAlerts();

    /**
     * Get offline/disconnected vehicles
     */
    List<VehicleCurrentStateResponse> getDisconnectedVehicles();

    /**
     * Initialize current state for a new vehicle
     */
    VehicleCurrentStateResponse initializeVehicleState(Long vehicleId, Long companyId);
}
