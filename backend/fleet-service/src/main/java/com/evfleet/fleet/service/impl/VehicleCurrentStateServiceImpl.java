package com.evfleet.fleet.service.impl;

import com.evfleet.fleet.dto.VehicleCurrentStateResponse;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.model.VehicleCurrentState;
import com.evfleet.fleet.repository.VehicleCurrentStateRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.VehicleCurrentStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VehicleCurrentStateService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleCurrentStateServiceImpl implements VehicleCurrentStateService {

    private final VehicleCurrentStateRepository currentStateRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public VehicleCurrentStateResponse updateState(VehicleCurrentState state) {
        log.info("Updating current state for vehicle: {}", state.getVehicleId());
        state.setLastUpdated(LocalDateTime.now());
        VehicleCurrentState savedState = currentStateRepository.save(state);
        return convertToResponse(savedState);
    }

    @Override
    public VehicleCurrentStateResponse getVehicleCurrentState(Long vehicleId) {
        log.debug("Fetching current state for vehicle: {}", vehicleId);
        return currentStateRepository.findByVehicleId(vehicleId)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Vehicle current state not found for ID: " + vehicleId));
    }

    @Override
    public List<VehicleCurrentStateResponse> getCompanyVehicleStates(Long companyId) {
        log.debug("Fetching vehicle states for company: {}", companyId);
        return currentStateRepository.findByCompanyId(companyId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getChargingVehicles() {
        log.debug("Fetching vehicles currently charging");
        return currentStateRepository.findByIsChargingTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getMaintenanceVehicles() {
        log.debug("Fetching vehicles currently in maintenance");
        return currentStateRepository.findByIsInMaintenanceTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getVehiclesInTrip() {
        log.debug("Fetching vehicles currently in trip");
        return currentStateRepository.findByIsInTripTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getVehiclesWithAlerts() {
        log.debug("Fetching vehicles with active alerts");
        return currentStateRepository.findVehiclesWithActiveAlerts()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getVehiclesWithCriticalAlerts() {
        log.debug("Fetching vehicles with critical alerts");
        return currentStateRepository.findVehiclesWithCriticalAlerts()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleCurrentStateResponse> getDisconnectedVehicles() {
        log.debug("Fetching disconnected vehicles");
        return currentStateRepository.findDisconnectedVehicles()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleCurrentStateResponse initializeVehicleState(Long vehicleId, Long companyId) {
        log.info("Initializing current state for vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));
        
        VehicleCurrentState state = new VehicleCurrentState();
        state.setVehicleId(vehicleId);
        state.setCompanyId(companyId);
        state.setStatus(vehicle.getStatus());
        state.setLastUpdated(LocalDateTime.now());
        state.setBatterySoc(vehicle.getCurrentBatterySoc());
        state.setLatitude(vehicle.getLatitude());
        state.setLongitude(vehicle.getLongitude());
        state.setIsConnected(true);
        state.setIsCharging(false);
        state.setIsInMaintenance(false);
        state.setIsInTrip(false);
        state.setActiveAlertsCount(0);
        state.setCriticalAlertsCount(0);
        
        VehicleCurrentState savedState = currentStateRepository.save(state);
        return convertToResponse(savedState);
    }

    /**
     * Convert VehicleCurrentState entity to VehicleCurrentStateResponse DTO
     */
    private VehicleCurrentStateResponse convertToResponse(VehicleCurrentState state) {
        VehicleCurrentStateResponse response = new VehicleCurrentStateResponse();
        response.setVehicleId(state.getVehicleId());
        
        // Fetch vehicle details
        vehicleRepository.findById(state.getVehicleId()).ifPresent(vehicle -> {
            response.setVehicleNumber(vehicle.getVehicleNumber());
        });
        
        response.setStatus(state.getStatus());
        response.setLastUpdated(state.getLastUpdated());
        
        response.setLatitude(state.getLatitude());
        response.setLongitude(state.getLongitude());
        response.setLocationName(state.getLocationName());
        response.setHeading(state.getHeading());
        response.setSpeed(state.getSpeed());
        
        response.setBatterySoc(state.getBatterySoc());
        response.setBatteryHealth(state.getBatteryHealth());
        response.setBatteryTemperature(state.getBatteryTemperature());
        response.setFuelLevel(state.getFuelLevel());
        
        response.setOdometer(state.getOdometer());
        response.setTotalDistance(state.getTotalDistance());
        response.setTotalEnergyConsumed(state.getTotalEnergyConsumed());
        response.setTotalFuelConsumed(state.getTotalFuelConsumed());
        
        response.setCurrentDriverId(state.getCurrentDriverId());
        response.setCurrentTripId(state.getCurrentTripId());
        response.setIsCharging(state.getIsCharging());
        response.setIsInMaintenance(state.getIsInMaintenance());
        response.setIsInTrip(state.getIsInTrip());
        
        response.setChargingStationId(state.getChargingStationId());
        response.setChargingStartedAt(state.getChargingStartedAt());
        response.setEstimatedChargingCompletion(state.getEstimatedChargingCompletion());
        
        response.setLastMaintenanceDate(state.getLastMaintenanceDate());
        response.setNextMaintenanceDueDate(state.getNextMaintenanceDueDate());
        response.setMaintenanceStatus(state.getMaintenanceStatus());
        
        response.setActiveAlertsCount(state.getActiveAlertsCount());
        response.setCriticalAlertsCount(state.getCriticalAlertsCount());
        response.setLastAlertTimestamp(state.getLastAlertTimestamp());
        
        response.setIsConnected(state.getIsConnected());
        response.setLastTelemetryReceived(state.getLastTelemetryReceived());
        response.setSignalStrength(state.getSignalStrength());
        
        response.setAverageSpeedLastTrip(state.getAverageSpeedLastTrip());
        response.setEfficiencyScore(state.getEfficiencyScore());
        
        response.setCompanyId(state.getCompanyId());
        response.setCreatedAt(state.getCreatedAt());
        response.setUpdatedAt(state.getUpdatedAt());
        
        return response;
    }
}
