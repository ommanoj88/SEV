package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.dto.TelemetryResponse;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.TelemetryData;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TelemetryRepository;
import com.evfleet.fleet.validation.TelemetryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling telemetry data operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final VehicleService vehicleService;
    private final TelemetryValidator telemetryValidator;
    private final TelemetryProcessingService telemetryProcessingService;

    /**
     * Process and store telemetry data
     */
    public TelemetryResponse processTelemetryData(TelemetryRequest request) {
        log.debug("Processing telemetry data for vehicle ID: {}", request.getVehicleId());

        // Fetch vehicle to get fuel type
        Vehicle vehicle;
        try {
            vehicle = vehicleService.getVehicleEntityById(request.getVehicleId());
        } catch (ResourceNotFoundException e) {
            log.error("Vehicle not found while processing telemetry: {}", request.getVehicleId());
            throw e;
        }

        // Validate telemetry data based on vehicle fuel type
        telemetryValidator.validateTelemetryForVehicle(request, vehicle);

        // Process telemetry data based on fuel type (routes EV vs ICE metrics)
        TelemetryData telemetry = telemetryProcessingService.processTelemetryData(request, vehicle);

        TelemetryData savedTelemetry = telemetryRepository.save(telemetry);

        // Update vehicle location
        try {
            vehicleService.updateVehicleLocation(request.getVehicleId(), request.getLatitude(), request.getLongitude());

            // Update battery SOC for EV and HYBRID vehicles
            if (request.getBatterySoc() != null) {
                vehicleService.updateBatterySoc(request.getVehicleId(), request.getBatterySoc());
            }

            // Update fuel level for ICE and HYBRID vehicles
            if (request.getFuelLevel() != null) {
                vehicleService.updateFuelLevel(request.getVehicleId(), request.getFuelLevel());
            }
        } catch (ResourceNotFoundException e) {
            log.error("Vehicle not found while updating location/fuel: {}", request.getVehicleId());
        }

        log.debug("Telemetry data processed for vehicle ID: {}", request.getVehicleId());
        return TelemetryResponse.fromEntity(savedTelemetry);
    }

    /**
     * Get telemetry data by vehicle ID
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetryByVehicle(Long vehicleId, int limit) {
        log.debug("Fetching telemetry data for vehicle ID: {}", vehicleId);

        Pageable pageable = PageRequest.of(0, limit);
        return telemetryRepository.findByVehicleId(vehicleId, pageable).stream()
                .map(TelemetryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get telemetry data within time range
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetryByVehicleAndTimeRange(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Fetching telemetry data for vehicle ID: {} between {} and {}",
                vehicleId, startTime, endTime);

        return telemetryRepository.findByVehicleIdAndTimestampBetween(vehicleId, startTime, endTime).stream()
                .map(TelemetryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get latest telemetry for a vehicle
     */
    @Transactional(readOnly = true)
    public TelemetryResponse getLatestTelemetry(Long vehicleId) {
        log.debug("Fetching latest telemetry for vehicle ID: {}", vehicleId);

        TelemetryData telemetry = telemetryRepository.findFirstByVehicleIdOrderByTimestampDesc(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("No telemetry data found for vehicle ID: " + vehicleId));

        return TelemetryResponse.fromEntity(telemetry);
    }

    /**
     * Get telemetry data by trip
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetryByTrip(Long tripId) {
        log.debug("Fetching telemetry data for trip ID: {}", tripId);

        return telemetryRepository.findByTripIdOrderByTimestampAsc(tripId).stream()
                .map(TelemetryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get telemetry with errors
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetryWithErrors(Long vehicleId, int limit) {
        log.debug("Fetching telemetry with errors for vehicle ID: {}", vehicleId);

        Pageable pageable = PageRequest.of(0, limit);
        return telemetryRepository.findTelemetryWithErrors(vehicleId, pageable).stream()
                .map(TelemetryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Clean up old telemetry data
     */
    public void cleanupOldTelemetry(int daysToKeep) {
        log.info("Cleaning up telemetry data older than {} days", daysToKeep);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        telemetryRepository.deleteByTimestampBefore(cutoffDate);

        log.info("Old telemetry data cleaned up successfully");
    }
}
