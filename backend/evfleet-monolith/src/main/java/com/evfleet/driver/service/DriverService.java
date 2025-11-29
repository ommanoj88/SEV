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

        // Validate license expiry date is not in the past
        if (request.getLicenseExpiry() != null && request.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("License expiry date cannot be in the past");
        }

        // Validate license number format (basic validation)
        if (request.getLicenseNumber() != null && !isValidLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("Invalid license number format");
        }

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
                // Initialize performance metrics
                .safetyScore(100.0) // Start with perfect score
                .fuelEfficiency(0.0)
                .harshBrakingEvents(0)
                .speedingEvents(0)
                .idlingTimeMinutes(0)
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

    @Transactional(readOnly = true)
    public List<DriverResponse> getDriverLeaderboard(Long companyId) {
        log.info("GET /api/v1/drivers/leaderboard - Fetching driver leaderboard for company: {}", companyId);
        List<Driver> drivers = driverRepository.findByCompanyId(companyId);
        
        // Sort by safety score (descending), then by total trips (descending)
        return drivers.stream()
                .filter(d -> d.getTotalTrips() != null && d.getTotalTrips() > 0) // Only include drivers with trips
                .sorted((d1, d2) -> {
                    // Primary sort by safety score (higher is better)
                    int safetyCompare = Double.compare(
                        d2.getSafetyScore() != null ? d2.getSafetyScore() : 0.0,
                        d1.getSafetyScore() != null ? d1.getSafetyScore() : 0.0
                    );
                    if (safetyCompare != 0) return safetyCompare;
                    
                    // Secondary sort by total trips (more is better)
                    return Integer.compare(
                        d2.getTotalTrips() != null ? d2.getTotalTrips() : 0,
                        d1.getTotalTrips() != null ? d1.getTotalTrips() : 0
                    );
                })
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
            // Validate license number format
            if (!isValidLicenseNumber(request.getLicenseNumber())) {
                throw new IllegalArgumentException("Invalid license number format");
            }
            driver.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getLicenseExpiry() != null) {
            // Warn if license is already expired or will expire soon
            if (request.getLicenseExpiry().isBefore(LocalDate.now())) {
                log.warn("Updating driver {} with expired license date", id);
            }
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

        // VALIDATION 1: Check if driver status is ACTIVE
        if (driver.getStatus() != Driver.DriverStatus.ACTIVE) {
            log.warn("AUDIT: Assignment blocked - Driver {} has status {} (not ACTIVE)", driverId, driver.getStatus());
            throw new IllegalStateException(
                String.format("Driver %s is not active (current status: %s). Only active drivers can be assigned to vehicles.",
                    driver.getName(), driver.getStatus())
            );
        }

        // VALIDATION 2: Check if driver's license is expired
        if (driver.getLicenseExpiry() != null && driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            log.warn("AUDIT: Assignment blocked - Driver {} license expired on {}", driverId, driver.getLicenseExpiry());
            throw new IllegalStateException(
                String.format("Driver %s has an expired license (expired on %s). Please renew the license before assigning to a vehicle.",
                    driver.getName(), driver.getLicenseExpiry())
            );
        }

        // VALIDATION 3: Warn if license will expire within 7 days
        if (driver.getLicenseExpiry() != null && 
            driver.getLicenseExpiry().isBefore(LocalDate.now().plusDays(7)) &&
            !driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            log.warn("AUDIT: License expiry warning - Driver {} license expires on {} (within 7 days)", 
                driverId, driver.getLicenseExpiry());
        }

        // VALIDATION 4: Check if driver is already assigned to a vehicle
        if (driver.getCurrentVehicleId() != null) {
            log.warn("AUDIT: Assignment blocked - Driver {} already assigned to vehicle {}", driverId, driver.getCurrentVehicleId());
            throw new IllegalStateException(
                String.format("Driver %s is already assigned to vehicle %d. Please unassign first.", 
                    driver.getName(), driver.getCurrentVehicleId())
            );
        }

        // VALIDATION 5: Check if vehicle is already assigned to another driver
        if (vehicle.getCurrentDriverId() != null) {
            log.warn("AUDIT: Assignment blocked - Vehicle {} already assigned to driver {}", vehicleId, vehicle.getCurrentDriverId());
            throw new IllegalStateException(
                String.format("Vehicle %s is already assigned to driver %d. Please unassign first.", 
                    vehicle.getLicensePlate(), vehicle.getCurrentDriverId())
            );
        }

        // VALIDATION 6: Check if vehicle status allows assignment
        if (vehicle.getStatus() != null && 
            (vehicle.getStatus() == Vehicle.VehicleStatus.MAINTENANCE || 
             vehicle.getStatus() == Vehicle.VehicleStatus.CHARGING)) {
            log.warn("AUDIT: Assignment blocked - Vehicle {} has status {} (not assignable)", vehicleId, vehicle.getStatus());
            throw new IllegalStateException(
                String.format("Vehicle %s is currently in %s status and cannot be assigned to a driver.",
                    vehicle.getLicensePlate(), vehicle.getStatus())
            );
        }

        // VALIDATION 7: Verify company ownership - driver and vehicle must belong to same company
        if (!driver.getCompanyId().equals(vehicle.getCompanyId())) {
            log.error("AUDIT: SECURITY VIOLATION - Attempted cross-company assignment: Driver {} (company {}) to Vehicle {} (company {})",
                driverId, driver.getCompanyId(), vehicleId, vehicle.getCompanyId());
            throw new IllegalStateException(
                "Security violation: Driver and vehicle must belong to the same company."
            );
        }

        // Update driver
        driver.setCurrentVehicleId(vehicleId);
        driver.setStatus(Driver.DriverStatus.ON_TRIP);

        // Update vehicle
        vehicle.setCurrentDriverId(driverId);
        vehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);

        // Save both entities
        Driver updated = driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        log.info("AUDIT: Vehicle {} assigned to driver {} successfully (company: {})", 
            vehicleId, driverId, driver.getCompanyId());
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

    /**
     * Validates license number format.
     * This is a basic validation. In production, this should be configurable based on region/country.
     * 
     * Current validation:
     * - Minimum 5 characters
     * - Maximum 50 characters
     * - Contains alphanumeric characters and hyphens
     * 
     * For specific regions, patterns like:
     * - India: ^[A-Z]{2}[0-9]{13}$ (e.g., MH0120210012345)
     * - US: varies by state
     * - UK: varies by format
     */
    private boolean isValidLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove whitespace
        licenseNumber = licenseNumber.trim();
        
        // Check length
        if (licenseNumber.length() < 5 || licenseNumber.length() > 50) {
            return false;
        }
        
        // Check for valid characters (alphanumeric and hyphens/spaces)
        return licenseNumber.matches("^[A-Za-z0-9\\-\\s]+$");
    }
}
