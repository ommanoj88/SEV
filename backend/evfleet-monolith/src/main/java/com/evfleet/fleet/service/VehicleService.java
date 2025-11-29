package com.evfleet.fleet.service;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.event.VehicleCreatedEvent;
import com.evfleet.fleet.event.BatteryLowEvent;
import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.common.exception.InvalidInputException;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vehicle Service
 * 
 * Handles vehicle CRUD operations with comprehensive validation for multi-fuel support.
 * 
 * PR #5 Fixes Applied:
 * - Added updateVehicle() method with fuel-type validation
 * - Added deleteVehicle() method with safety checks
 * - Added latitude/longitude validation
 * - Added year validation (1900 to current year + 1)
 * - Added charger type validation against allowed values
 * - Added validation when changing fuel types
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final EventPublisher eventPublisher;

    // Allowed charger types for validation
    private static final Set<String> ALLOWED_CHARGER_TYPES = Set.of(
        "CCS", "CCS2", "CHAdeMO", "Type2", "Type1", "GB/T", "Tesla", "J1772"
    );

    public Vehicle createVehicle(Vehicle vehicle) {
        log.info("Creating vehicle: {}", vehicle.getVehicleNumber());

        // Validate vehicle number not null/empty
        if (vehicle.getVehicleNumber() == null || vehicle.getVehicleNumber().trim().isEmpty()) {
            throw new InvalidInputException("vehicleNumber", "is required");
        }

        if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle number already exists");
        }

        // Check license plate uniqueness
        if (vehicle.getLicensePlate() != null && !vehicle.getLicensePlate().trim().isEmpty()) {
            if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
                throw new IllegalArgumentException("License plate already exists");
            }
        }

        // Check VIN uniqueness
        if (vehicle.getVin() != null && !vehicle.getVin().trim().isEmpty()) {
            if (vehicleRepository.existsByVin(vehicle.getVin())) {
                throw new IllegalArgumentException("VIN already exists");
            }
        }

        // Validate year
        validateYear(vehicle.getYear());

        // Validate fuel-type specific fields
        validateVehicleFuelTypeFields(vehicle);

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", saved.getId());

        // Publish event
        eventPublisher.publish(new VehicleCreatedEvent(
            this, saved.getId(), saved.getVehicleNumber(), saved.getCompanyId()
        ));

        return saved;
    }

    /**
     * Update an existing vehicle with comprehensive validation
     * 
     * @param id Vehicle ID to update
     * @param updatedVehicle Vehicle with updated fields
     * @return Updated vehicle
     * @throws ResourceNotFoundException if vehicle not found
     * @throws InvalidInputException if validation fails
     */
    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        log.info("Updating vehicle: {}", id);

        Vehicle existingVehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        // Check if vehicle number is being changed and if new one already exists
        if (updatedVehicle.getVehicleNumber() != null && 
            !updatedVehicle.getVehicleNumber().equals(existingVehicle.getVehicleNumber())) {
            if (vehicleRepository.existsByVehicleNumber(updatedVehicle.getVehicleNumber())) {
                throw new IllegalArgumentException("Vehicle number already exists");
            }
            existingVehicle.setVehicleNumber(updatedVehicle.getVehicleNumber());
        }

        // Check license plate uniqueness if changed
        if (updatedVehicle.getLicensePlate() != null && 
            !updatedVehicle.getLicensePlate().equals(existingVehicle.getLicensePlate())) {
            if (!updatedVehicle.getLicensePlate().trim().isEmpty() &&
                vehicleRepository.existsByLicensePlate(updatedVehicle.getLicensePlate())) {
                throw new IllegalArgumentException("License plate already exists");
            }
            existingVehicle.setLicensePlate(updatedVehicle.getLicensePlate());
        }

        // Check VIN uniqueness if changed
        if (updatedVehicle.getVin() != null && 
            !updatedVehicle.getVin().equals(existingVehicle.getVin())) {
            if (!updatedVehicle.getVin().trim().isEmpty() &&
                vehicleRepository.existsByVin(updatedVehicle.getVin())) {
                throw new IllegalArgumentException("VIN already exists");
            }
            existingVehicle.setVin(updatedVehicle.getVin());
        }

        // Handle fuel type change - requires all new type's required fields
        if (updatedVehicle.getFuelType() != null && 
            updatedVehicle.getFuelType() != existingVehicle.getFuelType()) {
            log.info("Fuel type changing from {} to {}", 
                existingVehicle.getFuelType(), updatedVehicle.getFuelType());
            // Validate new fuel type has required fields
            validateVehicleFuelTypeFields(updatedVehicle);
            existingVehicle.setFuelType(updatedVehicle.getFuelType());
        }

        // Update optional fields if provided
        if (updatedVehicle.getMake() != null) existingVehicle.setMake(updatedVehicle.getMake());
        if (updatedVehicle.getModel() != null) existingVehicle.setModel(updatedVehicle.getModel());
        if (updatedVehicle.getYear() != null) {
            validateYear(updatedVehicle.getYear());
            existingVehicle.setYear(updatedVehicle.getYear());
        }
        if (updatedVehicle.getColor() != null) existingVehicle.setColor(updatedVehicle.getColor());
        if (updatedVehicle.getType() != null) existingVehicle.setType(updatedVehicle.getType());
        if (updatedVehicle.getStatus() != null) existingVehicle.setStatus(updatedVehicle.getStatus());

        // Update fuel-type specific fields with validation
        updateFuelTypeSpecificFields(existingVehicle, updatedVehicle);

        existingVehicle.setLastUpdated(LocalDateTime.now());
        Vehicle saved = vehicleRepository.save(existingVehicle);
        log.info("Vehicle {} updated successfully", id);

        return saved;
    }

    /**
     * Delete a vehicle with safety checks
     * 
     * @param id Vehicle ID to delete
     * @throws ResourceNotFoundException if vehicle not found
     * @throws IllegalStateException if vehicle is in active trip or has assigned driver
     */
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        // Safety check: Cannot delete vehicle in active trip
        if (vehicle.getStatus() == Vehicle.VehicleStatus.IN_TRIP) {
            throw new IllegalStateException("Cannot delete vehicle while it is in an active trip");
        }

        // Safety check: Cannot delete vehicle with assigned driver
        if (vehicle.getCurrentDriverId() != null) {
            throw new IllegalStateException("Cannot delete vehicle with assigned driver. Unassign driver first.");
        }

        // Safety check: Cannot delete vehicle currently charging
        if (vehicle.getStatus() == Vehicle.VehicleStatus.CHARGING) {
            throw new IllegalStateException("Cannot delete vehicle while it is charging");
        }

        vehicleRepository.delete(vehicle);
        log.info("Vehicle {} deleted successfully", id);
    }

    /**
     * Update fuel-type specific fields with validation
     */
    private void updateFuelTypeSpecificFields(Vehicle existing, Vehicle updated) {
        FuelType fuelType = existing.getFuelType();

        if (fuelType == FuelType.EV || fuelType == FuelType.HYBRID) {
            if (updated.getBatteryCapacity() != null) {
                if (updated.getBatteryCapacity() <= 0) {
                    throw new InvalidInputException("batteryCapacity", "must be greater than 0");
                }
                existing.setBatteryCapacity(updated.getBatteryCapacity());
            }
            if (updated.getCurrentBatterySoc() != null) {
                validateBatterySoc(updated.getCurrentBatterySoc());
                existing.setCurrentBatterySoc(updated.getCurrentBatterySoc());
            }
            if (updated.getDefaultChargerType() != null) {
                validateChargerType(updated.getDefaultChargerType());
                existing.setDefaultChargerType(updated.getDefaultChargerType());
            }
        }

        if (fuelType == FuelType.ICE || fuelType == FuelType.HYBRID) {
            if (updated.getFuelTankCapacity() != null) {
                if (updated.getFuelTankCapacity() <= 0) {
                    throw new InvalidInputException("fuelTankCapacity", "must be greater than 0");
                }
                existing.setFuelTankCapacity(updated.getFuelTankCapacity());
            }
            if (updated.getFuelLevel() != null) {
                Double tankCapacity = updated.getFuelTankCapacity() != null ? 
                    updated.getFuelTankCapacity() : existing.getFuelTankCapacity();
                validateFuelLevel(updated.getFuelLevel(), tankCapacity);
                existing.setFuelLevel(updated.getFuelLevel());
            }
            if (updated.getEngineType() != null) {
                existing.setEngineType(updated.getEngineType());
            }
        }
    }

    /**
     * Validates that vehicle has required fields based on fuel type
     * @param vehicle Vehicle to validate
     * @throws InvalidInputException if validation fails
     */
    private void validateVehicleFuelTypeFields(Vehicle vehicle) {
        if (vehicle.getFuelType() == null) {
            throw new InvalidInputException("fuelType", "must not be null");
        }

        switch (vehicle.getFuelType()) {
            case EV:
                validateEVFields(vehicle);
                break;
            case ICE:
                validateICEFields(vehicle);
                break;
            case HYBRID:
                validateHybridFields(vehicle);
                break;
        }
    }

    /**
     * Validates EV-specific required fields
     */
    private void validateEVFields(Vehicle vehicle) {
        if (vehicle.getBatteryCapacity() == null || vehicle.getBatteryCapacity() <= 0) {
            throw new InvalidInputException("batteryCapacity", "must be greater than 0 for EV vehicles");
        }
        if (vehicle.getDefaultChargerType() == null || vehicle.getDefaultChargerType().trim().isEmpty()) {
            throw new InvalidInputException("defaultChargerType", "is required for EV vehicles");
        }
        validateChargerType(vehicle.getDefaultChargerType());
        // Validate battery SOC if provided
        if (vehicle.getCurrentBatterySoc() != null) {
            validateBatterySoc(vehicle.getCurrentBatterySoc());
        }
    }

    /**
     * Validates ICE-specific required fields
     */
    private void validateICEFields(Vehicle vehicle) {
        if (vehicle.getFuelTankCapacity() == null || vehicle.getFuelTankCapacity() <= 0) {
            throw new InvalidInputException("fuelTankCapacity", "must be greater than 0 for ICE vehicles");
        }
        // Validate fuel level if provided
        if (vehicle.getFuelLevel() != null) {
            validateFuelLevel(vehicle.getFuelLevel(), vehicle.getFuelTankCapacity());
        }
    }

    /**
     * Validates HYBRID-specific required fields (both EV and ICE fields)
     */
    private void validateHybridFields(Vehicle vehicle) {
        // Hybrid vehicles need both battery and fuel tank
        if (vehicle.getBatteryCapacity() == null || vehicle.getBatteryCapacity() <= 0) {
            throw new InvalidInputException("batteryCapacity", "must be greater than 0 for HYBRID vehicles");
        }
        if (vehicle.getFuelTankCapacity() == null || vehicle.getFuelTankCapacity() <= 0) {
            throw new InvalidInputException("fuelTankCapacity", "must be greater than 0 for HYBRID vehicles");
        }
        // Validate battery SOC if provided
        if (vehicle.getCurrentBatterySoc() != null) {
            validateBatterySoc(vehicle.getCurrentBatterySoc());
        }
        // Validate fuel level if provided
        if (vehicle.getFuelLevel() != null) {
            validateFuelLevel(vehicle.getFuelLevel(), vehicle.getFuelTankCapacity());
        }
    }

    /**
     * Validates battery state of charge is within valid range (0-100)
     */
    private void validateBatterySoc(Double soc) {
        if (soc < 0 || soc > 100) {
            throw new InvalidInputException("currentBatterySoc", "must be between 0 and 100");
        }
    }

    /**
     * Validates fuel level does not exceed tank capacity
     */
    private void validateFuelLevel(Double fuelLevel, Double tankCapacity) {
        if (fuelLevel < 0) {
            throw new InvalidInputException("fuelLevel", "must be greater than or equal to 0");
        }
        if (tankCapacity != null && fuelLevel > tankCapacity) {
            throw new InvalidInputException("fuelLevel", 
                String.format("cannot exceed fuel tank capacity (%.2f liters)", tankCapacity));
        }
    }

    /**
     * Validates charger type against allowed values
     */
    private void validateChargerType(String chargerType) {
        if (chargerType == null || chargerType.trim().isEmpty()) {
            return; // Null/empty is checked elsewhere
        }
        String normalized = chargerType.trim().toUpperCase();
        boolean valid = ALLOWED_CHARGER_TYPES.stream()
            .anyMatch(allowed -> allowed.equalsIgnoreCase(chargerType.trim()));
        if (!valid) {
            throw new InvalidInputException("defaultChargerType", 
                "must be one of: " + String.join(", ", ALLOWED_CHARGER_TYPES));
        }
    }

    /**
     * Validates vehicle year is reasonable
     */
    private void validateYear(Integer year) {
        if (year == null) {
            throw new InvalidInputException("year", "is required");
        }
        int currentYear = Year.now().getValue();
        if (year < 1900 || year > currentYear + 1) {
            throw new InvalidInputException("year", 
                String.format("must be between 1900 and %d", currentYear + 1));
        }
    }

    /**
     * Validates latitude is within valid range
     */
    private void validateLatitude(Double latitude) {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new InvalidInputException("latitude", "must be between -90 and 90");
        }
    }

    /**
     * Validates longitude is within valid range
     */
    private void validateLongitude(Double longitude) {
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new InvalidInputException("longitude", "must be between -180 and 180");
        }
    }

    public Vehicle updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        // Validate coordinates
        validateLatitude(latitude);
        validateLongitude(longitude);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicle.setLastUpdated(LocalDateTime.now());
        Vehicle updated = vehicleRepository.save(vehicle);

        // Check battery level and publish warning if low
        // Only for 4-wheelers (LCV) with battery tracking - 2W/3W use GPS-only
        if (vehicle.getType() == Vehicle.VehicleType.LCV && 
            vehicle.getCurrentBatterySoc() != null && vehicle.getCurrentBatterySoc() < 20) {
            eventPublisher.publish(new BatteryLowEvent(
                this, vehicleId, vehicle.getVehicleNumber(),
                vehicle.getCurrentBatterySoc(), latitude, longitude
            ));
        }

        return updated;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByCompany(Long companyId) {
        return vehicleRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
    }

    /**
     * Get vehicles by company with driver names populated
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesWithDriverNames(Long companyId) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        return vehicles.stream()
            .map(this::toResponseWithDriverName)
            .collect(Collectors.toList());
    }

    /**
     * Helper method to convert Vehicle to VehicleResponse with driver name
     */
    private VehicleResponse toResponseWithDriverName(Vehicle vehicle) {
        String driverName = null;
        if (vehicle.getCurrentDriverId() != null) {
            driverName = driverRepository.findById(vehicle.getCurrentDriverId())
                .map(Driver::getName)
                .orElse(null);
        }
        return VehicleResponse.from(vehicle, driverName);
    }
}
