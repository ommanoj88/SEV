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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vehicle Service
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final EventPublisher eventPublisher;

    public Vehicle createVehicle(Vehicle vehicle) {
        log.info("Creating vehicle: {}", vehicle.getVehicleNumber());

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
        if (fuelLevel > tankCapacity) {
            throw new InvalidInputException("fuelLevel", 
                String.format("cannot exceed fuel tank capacity (%.2f liters)", tankCapacity));
        }
    }

    public Vehicle updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
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
