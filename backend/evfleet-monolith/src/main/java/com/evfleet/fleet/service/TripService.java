package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.event.TripCompletedEvent;
import com.evfleet.fleet.event.TripStartedEvent;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Trip Service
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final EventPublisher eventPublisher;

    public Trip startTrip(Long vehicleId, Long driverId, Double startLat, Double startLon) {
        log.info("Starting trip for vehicle: {}", vehicleId);

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        // Check if vehicle already has active trip
        tripRepository.findByVehicleIdAndStatus(vehicleId, Trip.TripStatus.IN_PROGRESS)
            .ifPresent(t -> {
                throw new IllegalStateException("Vehicle already has an active trip");
            });

        // Create trip
        Trip trip = Trip.builder()
            .vehicleId(vehicleId)
            .driverId(driverId)
            .companyId(vehicle.getCompanyId())
            .startTime(LocalDateTime.now())
            .startLatitude(startLat)
            .startLongitude(startLon)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        Trip saved = tripRepository.save(trip);

        // Update vehicle status
        vehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);
        vehicle.setCurrentDriverId(driverId);
        vehicleRepository.save(vehicle);

        // Publish event
        eventPublisher.publish(new TripStartedEvent(this, saved.getId(), vehicleId, driverId));

        log.info("Trip started with ID: {}", saved.getId());
        return saved;
    }

    public Trip completeTrip(Long tripId, Double endLat, Double endLon,
                            Double distance, BigDecimal energyConsumed) {
        log.info("Completing trip: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete trip that is not in progress");
        }

        // Calculate duration
        long duration = java.time.Duration.between(trip.getStartTime(), LocalDateTime.now()).getSeconds();

        // Complete trip
        trip.complete(endLat, endLon, distance, duration);
        trip.setEnergyConsumed(energyConsumed);

        Trip completed = tripRepository.save(trip);

        // Update vehicle status
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", trip.getVehicleId()));

        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicle.setCurrentDriverId(null);
        vehicle.setLatitude(endLat);
        vehicle.setLongitude(endLon);

        // Update totals
        if (vehicle.getTotalDistance() == null) {
            vehicle.setTotalDistance(0.0);
        }
        vehicle.setTotalDistance(vehicle.getTotalDistance() + distance);

        vehicleRepository.save(vehicle);

        // Publish event
        eventPublisher.publish(new TripCompletedEvent(
            this, tripId, trip.getVehicleId(), distance, duration, energyConsumed
        ));

        log.info("Trip completed: {} - Distance: {} km, Duration: {} sec", tripId, distance, duration);
        return completed;
    }

    @Transactional(readOnly = true)
    public List<Trip> getTripsByVehicle(Long vehicleId) {
        return tripRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<Trip> getTripsByCompany(Long companyId) {
        return tripRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));
    }
}
