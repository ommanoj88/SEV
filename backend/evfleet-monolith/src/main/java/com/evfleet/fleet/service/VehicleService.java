package com.evfleet.fleet.service;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.event.VehicleCreatedEvent;
import com.evfleet.fleet.event.BatteryLowEvent;
import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
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

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", saved.getId());

        // Publish event
        eventPublisher.publish(new VehicleCreatedEvent(
            this, saved.getId(), saved.getVehicleNumber(), saved.getCompanyId()
        ));

        return saved;
    }

    public Vehicle updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        Vehicle updated = vehicleRepository.save(vehicle);

        // Check battery level and publish warning if low
        if (vehicle.getCurrentBatterySoc() != null && vehicle.getCurrentBatterySoc() < 20) {
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
