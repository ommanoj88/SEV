package com.evfleet.fleet.service.impl;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.event.EventPublisher;
import com.evfleet.fleet.event.VehicleLocationEvent;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.FeatureAvailabilityService;
import com.evfleet.fleet.service.VehicleService;
import com.evfleet.fleet.validation.FuelTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VehicleService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final EventPublisher eventPublisher;
    private final FuelTypeValidator fuelTypeValidator;
    private final FeatureAvailabilityService featureAvailabilityService;

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        log.info("Creating new vehicle with number: {}", request.getVehicleNumber());

        // PR 5: Validate fuel type specific requirements
        fuelTypeValidator.validateVehicleRequest(request);

        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle with number " + request.getVehicleNumber() + " already exists");
        }

        Vehicle vehicle = new Vehicle();
        mapRequestToEntity(request, vehicle);
        vehicle.setTotalDistance(0.0);
        vehicle.setTotalEnergyConsumed(0.0);
        vehicle.setTotalFuelConsumed(0.0);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());

        // PR 5: Build response with available features
        return buildVehicleResponseWithFeatures(savedVehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        log.info("Updating vehicle with ID: {}", id);

        // PR 5: Validate fuel type specific requirements
        fuelTypeValidator.validateVehicleRequest(request);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        // Check if vehicle number is being changed and if it's unique
        if (!vehicle.getVehicleNumber().equals(request.getVehicleNumber())) {
            if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
                throw new IllegalArgumentException("Vehicle with number " + request.getVehicleNumber() + " already exists");
            }
        }

        mapRequestToEntity(request, vehicle);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully with ID: {}", updatedVehicle.getId());

        // PR 5: Build response with available features
        return buildVehicleResponseWithFeatures(updatedVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        log.debug("Fetching vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        // PR 5: Include available features in response
        return buildVehicleResponseWithFeatures(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        log.debug("Fetching all vehicles");

        // PR 5: Include available features in responses
        return vehicleRepository.findAll().stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCompany(Long companyId) {
        log.debug("Fetching vehicles for company ID: {}", companyId);

        // PR 5: Include available features in responses
        return vehicleRepository.findByCompanyId(companyId).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCompanyAndStatus(Long companyId, Vehicle.VehicleStatus status) {
        log.debug("Fetching vehicles for company ID: {} with status: {}", companyId, status);

        // PR 5: Include available features in responses
        return vehicleRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    public void updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        log.debug("Updating location for vehicle ID: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicle.setLastUpdated(LocalDateTime.now());

        vehicleRepository.save(vehicle);

        // Publish location update event
        VehicleLocationEvent event = new VehicleLocationEvent(
                vehicleId,
                vehicle.getCompanyId(),
                latitude,
                longitude,
                vehicle.getCurrentBatterySoc(),
                LocalDateTime.now()
        );
        eventPublisher.publishVehicleLocationUpdate(event);

        log.debug("Location updated for vehicle ID: {}", vehicleId);
    }

    @Override
    public void updateBatterySoc(Long vehicleId, Double soc) {
        log.debug("Updating battery SOC for vehicle ID: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        Double previousSoc = vehicle.getCurrentBatterySoc();
        vehicle.setCurrentBatterySoc(soc);
        vehicle.setLastUpdated(LocalDateTime.now());

        vehicleRepository.save(vehicle);

        // Publish low battery event if SOC drops below 20%
        if (soc != null && soc < 20.0 && (previousSoc == null || previousSoc >= 20.0)) {
            eventPublisher.publishLowBatteryAlert(vehicleId, vehicle.getCompanyId(), soc);
            log.warn("Low battery alert published for vehicle ID: {}, SOC: {}%", vehicleId, soc);
        }

        log.debug("Battery SOC updated for vehicle ID: {}", vehicleId);
    }

    @Override
    public void updateVehicleStatus(Long vehicleId, Vehicle.VehicleStatus status) {
        log.info("Updating status for vehicle ID: {} to {}", vehicleId, status);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);

        log.info("Status updated for vehicle ID: {}", vehicleId);
    }

    @Override
    public void assignDriver(Long vehicleId, Long driverId) {
        log.info("Assigning driver ID: {} to vehicle ID: {}", driverId, vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setCurrentDriverId(driverId);
        vehicleRepository.save(vehicle);

        log.info("Driver assigned successfully to vehicle ID: {}", vehicleId);
    }

    @Override
    public void removeDriver(Long vehicleId) {
        log.info("Removing driver from vehicle ID: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setCurrentDriverId(null);
        vehicleRepository.save(vehicle);

        log.info("Driver removed successfully from vehicle ID: {}", vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesWithLowBattery(Long companyId, Double threshold) {
        log.debug("Fetching vehicles with battery below: {}% for company ID: {}", threshold, companyId);

        // PR 5: Include available features in responses
        return vehicleRepository.findVehiclesWithLowBattery(companyId, threshold).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getActiveVehicles(Long companyId) {
        log.debug("Fetching active vehicles for company ID: {}", companyId);

        // PR 5: Include available features in responses
        return vehicleRepository.findActiveVehiclesByCompany(companyId).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByNumber(String vehicleNumber) {
        log.debug("Fetching vehicle with number: {}", vehicleNumber);

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with number: " + vehicleNumber));

        // PR 5: Include available features in response
        return buildVehicleResponseWithFeatures(vehicle);
    }

    // ===== PR 4: Multi-fuel Query Methods Implementation =====

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByFuelType(com.evfleet.fleet.model.FuelType fuelType) {
        log.debug("Fetching vehicles with fuel type: {}", fuelType);

        // PR 5: Include available features in responses
        return vehicleRepository.findByFuelType(fuelType).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCompanyAndFuelType(Long companyId, com.evfleet.fleet.model.FuelType fuelType) {
        log.debug("Fetching vehicles for company ID: {} with fuel type: {}", companyId, fuelType);

        // PR 5: Include available features in responses
        return vehicleRepository.findByCompanyIdAndFuelType(companyId, fuelType).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getFleetComposition(Long companyId) {
        log.debug("Fetching fleet composition for company ID: {}", companyId);

        List<Object[]> compositionData = vehicleRepository.getFleetCompositionByCompany(companyId);
        
        java.util.Map<String, Long> fuelTypeCounts = new java.util.HashMap<>();
        long totalVehicles = 0;

        // Process the query results
        for (Object[] row : compositionData) {
            com.evfleet.fleet.model.FuelType fuelType = (com.evfleet.fleet.model.FuelType) row[0];
            Long count = ((Number) row[1]).longValue();
            
            if (fuelType != null) {
                fuelTypeCounts.put(fuelType.name(), count);
                totalVehicles += count;
            }
        }

        // Calculate percentages
        java.util.Map<String, Double> fuelTypePercentages = new java.util.HashMap<>();
        if (totalVehicles > 0) {
            for (java.util.Map.Entry<String, Long> entry : fuelTypeCounts.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalVehicles;
                fuelTypePercentages.put(entry.getKey(), Math.round(percentage * 100.0) / 100.0);
            }
        }

        // Build the response
        java.util.Map<String, Object> composition = new java.util.HashMap<>();
        composition.put("totalVehicles", totalVehicles);
        composition.put("counts", fuelTypeCounts);
        composition.put("percentages", fuelTypePercentages);

        log.debug("Fleet composition for company ID {}: {} total vehicles", companyId, totalVehicles);
        return composition;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getLowBatteryVehicles(Long companyId, Double threshold) {
        log.debug("Fetching EV/HYBRID vehicles with battery below: {}% for company ID: {}", threshold, companyId);

        // PR 5: Include available features in responses
        return vehicleRepository.findLowBatteryVehicles(companyId, threshold).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getLowFuelVehicles(Long companyId, Double thresholdPercentage) {
        log.debug("Fetching ICE/HYBRID vehicles with fuel below: {}% for company ID: {}", thresholdPercentage, companyId);

        // PR 5: Include available features in responses
        return vehicleRepository.findLowFuelVehicles(companyId, thresholdPercentage).stream()
                .map(this::buildVehicleResponseWithFeatures)
                .collect(Collectors.toList());
    }

    /**
     * Build VehicleResponse with available features based on fuel type
     * PR 5: Include available features in response
     */
    private VehicleResponse buildVehicleResponseWithFeatures(Vehicle vehicle) {
        VehicleResponse response = VehicleResponse.fromEntity(vehicle);
        response.setAvailableFeatures(featureAvailabilityService.buildAvailableFeatures(vehicle));
        return response;
    }

    private void mapRequestToEntity(VehicleRequest request, Vehicle vehicle) {
        vehicle.setCompanyId(request.getCompanyId());
        vehicle.setVehicleNumber(request.getVehicleNumber());
        vehicle.setType(request.getType());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setBatteryCapacity(request.getBatteryCapacity());
        vehicle.setStatus(request.getStatus());
        vehicle.setDefaultChargerType(request.getDefaultChargerType());
        vehicle.setFuelTankCapacity(request.getFuelTankCapacity());
        vehicle.setFuelLevel(request.getFuelLevel());
        vehicle.setEngineType(request.getEngineType());
        vehicle.setVin(request.getVin());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setColor(request.getColor());

        if (request.getCurrentBatterySoc() != null) {
            vehicle.setCurrentBatterySoc(request.getCurrentBatterySoc());
        }
    }
}
