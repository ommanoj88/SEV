package com.evfleet.driver.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.dto.DriverRequest;
import com.evfleet.driver.dto.DriverResponse;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Driver Service
 * Handles all driver-related business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverResponse createDriver(Long companyId, DriverRequest request) {
        log.info("POST /api/v1/drivers - Creating driver for company: {}", companyId);

        Driver driver = Driver.builder()
                .companyId(companyId)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry())
                .status(request.getStatus() != null ?
                        Driver.DriverStatus.valueOf(request.getStatus().toUpperCase()) :
                        Driver.DriverStatus.ACTIVE)
                .currentVehicleId(request.getCurrentVehicleId())
                .totalTrips(0)
                .totalDistance(0.0)
                .build();

        Driver saved = driverRepository.save(driver);
        log.info("Driver created successfully: {}", saved.getId());
        return DriverResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers(Long companyId) {
        log.info("GET /api/v1/drivers - Fetching all drivers for company: {}", companyId);
        List<Driver> drivers = driverRepository.findByCompanyId(companyId);
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {
        log.info("GET /api/v1/drivers/{} - Fetching driver", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        return DriverResponse.fromEntity(driver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getActiveDrivers(Long companyId) {
        log.info("GET /api/v1/drivers/active - Fetching active drivers for company: {}", companyId);
        List<Driver> drivers = driverRepository.findByCompanyIdAndStatus(companyId, Driver.DriverStatus.ACTIVE);
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers(Long companyId) {
        log.info("GET /api/v1/drivers/available - Fetching available drivers for company: {}", companyId);
        List<Driver> drivers = driverRepository.findByCompanyIdAndStatusAndCurrentVehicleIdIsNull(
                companyId, Driver.DriverStatus.ACTIVE);
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversWithExpiringLicenses(Long companyId, int daysAhead) {
        log.info("GET /api/v1/drivers/expiring-licenses - Fetching drivers with expiring licenses for company: {}", companyId);
        LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
        List<Driver> drivers = driverRepository.findByCompanyIdAndLicenseExpiryBefore(companyId, cutoffDate);
        return drivers.stream()
                .map(DriverResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public DriverResponse updateDriver(Long id, DriverRequest request) {
        log.info("PUT /api/v1/drivers/{} - Updating driver", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        if (request.getName() != null) {
            driver.setName(request.getName());
        }
        if (request.getPhone() != null) {
            driver.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            driver.setEmail(request.getEmail());
        }
        if (request.getLicenseNumber() != null) {
            driver.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getLicenseExpiry() != null) {
            driver.setLicenseExpiry(request.getLicenseExpiry());
        }
        if (request.getStatus() != null) {
            driver.setStatus(Driver.DriverStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getCurrentVehicleId() != null) {
            driver.setCurrentVehicleId(request.getCurrentVehicleId());
        }

        Driver updated = driverRepository.save(driver);
        log.info("Driver updated successfully: {}", id);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse assignVehicle(Long driverId, Long vehicleId) {
        log.info("POST /api/v1/drivers/{}/assign - Assigning vehicle: {}", driverId, vehicleId);

        // Find driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        // Find vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        // Update driver
        driver.setCurrentVehicleId(vehicleId);
        driver.setStatus(Driver.DriverStatus.ON_TRIP);

        // Update vehicle - THIS IS THE FIX!
        vehicle.setCurrentDriverId(driverId);
        vehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);

        // Save both entities
        Driver updated = driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} assigned to driver {} successfully", vehicleId, driverId);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse unassignVehicle(Long driverId) {
        log.info("POST /api/v1/drivers/{}/unassign - Unassigning vehicle", driverId);

        // Find driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        // Get the vehicle ID before clearing
        Long vehicleId = driver.getCurrentVehicleId();

        // Update driver
        driver.setCurrentVehicleId(null);
        driver.setStatus(Driver.DriverStatus.ACTIVE);

        // Update vehicle if one was assigned
        if (vehicleId != null) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElse(null);
            if (vehicle != null) {
                vehicle.setCurrentDriverId(null);
                vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
                vehicleRepository.save(vehicle);
            }
        }

        Driver updated = driverRepository.save(driver);
        log.info("Vehicle unassigned from driver: {}", driverId);
        return DriverResponse.fromEntity(updated);
    }

    public void deleteDriver(Long id) {
        log.info("DELETE /api/v1/drivers/{} - Deleting driver", id);

        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver", "id", id);
        }

        driverRepository.deleteById(id);
        log.info("Driver deleted successfully: {}", id);
    }
}
