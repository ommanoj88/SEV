package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TripRequest;
import com.evfleet.fleet.dto.TripResponse;
import com.evfleet.fleet.event.EventPublisher;
import com.evfleet.fleet.event.TripCompletedEvent;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TelemetryRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling trip operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final TelemetryRepository telemetryRepository;
    private final EventPublisher eventPublisher;

    /**
     * Start a new trip
     */
    public TripResponse startTrip(TripRequest request) {
        log.info("Starting new trip for vehicle ID: {}", request.getVehicleId());

        // Verify vehicle exists
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        // Check if there's already an ongoing trip for this vehicle
        tripRepository.findByVehicleIdAndStatus(request.getVehicleId(), Trip.TripStatus.ONGOING)
                .ifPresent(trip -> {
                    throw new IllegalStateException("Vehicle already has an ongoing trip with ID: " + trip.getId());
                });

        Trip trip = new Trip();
        trip.setVehicleId(request.getVehicleId());
        trip.setDriverId(request.getDriverId());
        trip.setStartTime(LocalDateTime.now());
        trip.setStartLocation(request.getStartLocation());
        trip.setStatus(Trip.TripStatus.ONGOING);
        trip.setCompanyId(vehicle.getCompanyId());
        trip.setPurpose(request.getPurpose());
        trip.setNotes(request.getNotes());

        // Capture initial vehicle metrics
        trip.setStartBatterySoc(vehicle.getCurrentBatterySoc());
        trip.setStartOdometer(vehicle.getTotalDistance());

        // Initialize counters
        trip.setHarshAccelerationCount(0);
        trip.setHarshBrakingCount(0);
        trip.setOverspeedingCount(0);
        trip.setIdleTimeMinutes(0);

        Trip savedTrip = tripRepository.save(trip);

        // Update vehicle status
        vehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);
        if (request.getDriverId() != null) {
            vehicle.setCurrentDriverId(request.getDriverId());
        }
        vehicleRepository.save(vehicle);

        log.info("Trip started successfully with ID: {}", savedTrip.getId());
        return TripResponse.fromEntity(savedTrip);
    }

    /**
     * End a trip
     */
    public TripResponse endTrip(Long tripId, String endLocation) {
        log.info("Ending trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.ONGOING) {
            throw new IllegalStateException("Cannot end trip with status: " + trip.getStatus());
        }

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + trip.getVehicleId()));

        // Set end time and location
        trip.setEndTime(LocalDateTime.now());
        trip.setEndLocation(endLocation);
        trip.setStatus(Trip.TripStatus.COMPLETED);

        // Capture final vehicle metrics
        trip.setEndBatterySoc(vehicle.getCurrentBatterySoc());
        trip.setEndOdometer(vehicle.getTotalDistance());

        // Calculate trip statistics from telemetry data
        calculateTripStatistics(trip);

        // Calculate duration
        Duration duration = Duration.between(trip.getStartTime(), trip.getEndTime());
        trip.setDurationMinutes((int) duration.toMinutes());

        // Calculate efficiency score
        trip.setEfficiencyScore(calculateEfficiencyScore(trip));

        Trip savedTrip = tripRepository.save(trip);

        // Update vehicle status
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        if (trip.getDistance() != null) {
            vehicle.setTotalDistance((vehicle.getTotalDistance() != null ? vehicle.getTotalDistance() : 0.0) + trip.getDistance());
        }
        if (trip.getEnergyConsumed() != null) {
            vehicle.setTotalEnergyConsumed((vehicle.getTotalEnergyConsumed() != null ? vehicle.getTotalEnergyConsumed() : 0.0) + trip.getEnergyConsumed());
        }
        vehicleRepository.save(vehicle);

        // Publish trip completed event
        TripCompletedEvent event = new TripCompletedEvent(
                savedTrip.getId(),
                savedTrip.getVehicleId(),
                savedTrip.getDriverId(),
                savedTrip.getCompanyId(),
                savedTrip.getDistance(),
                savedTrip.getEnergyConsumed(),
                savedTrip.getDurationMinutes(),
                savedTrip.getEfficiencyScore(),
                savedTrip.getEndTime()
        );
        eventPublisher.publishTripCompleted(event);

        log.info("Trip ended successfully with ID: {}", tripId);
        return TripResponse.fromEntity(savedTrip);
    }

    /**
     * Pause a trip
     */
    public TripResponse pauseTrip(Long tripId) {
        log.info("Pausing trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.ONGOING) {
            throw new IllegalStateException("Cannot pause trip with status: " + trip.getStatus());
        }

        trip.setStatus(Trip.TripStatus.PAUSED);
        Trip savedTrip = tripRepository.save(trip);

        log.info("Trip paused successfully with ID: {}", tripId);
        return TripResponse.fromEntity(savedTrip);
    }

    /**
     * Resume a paused trip
     */
    public TripResponse resumeTrip(Long tripId) {
        log.info("Resuming trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.PAUSED) {
            throw new IllegalStateException("Cannot resume trip with status: " + trip.getStatus());
        }

        trip.setStatus(Trip.TripStatus.ONGOING);
        Trip savedTrip = tripRepository.save(trip);

        log.info("Trip resumed successfully with ID: {}", tripId);
        return TripResponse.fromEntity(savedTrip);
    }

    /**
     * Cancel a trip
     */
    public void cancelTrip(Long tripId) {
        log.info("Cancelling trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        trip.setStatus(Trip.TripStatus.CANCELLED);
        trip.setEndTime(LocalDateTime.now());
        tripRepository.save(trip);

        // Update vehicle status
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + trip.getVehicleId()));
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);

        log.info("Trip cancelled successfully with ID: {}", tripId);
    }

    /**
     * Get trip by ID
     */
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId) {
        log.debug("Fetching trip with ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        return TripResponse.fromEntity(trip);
    }

    /**
     * Get trips by vehicle
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByVehicle(Long vehicleId, int limit) {
        log.debug("Fetching trips for vehicle ID: {}", vehicleId);

        Pageable pageable = PageRequest.of(0, limit);
        return tripRepository.findByVehicleId(vehicleId, pageable).stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by driver
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByDriver(Long driverId, int limit) {
        log.debug("Fetching trips for driver ID: {}", driverId);

        Pageable pageable = PageRequest.of(0, limit);
        return tripRepository.findByDriverId(driverId, pageable).stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by company
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByCompany(Long companyId, int limit) {
        log.debug("Fetching trips for company ID: {}", companyId);

        Pageable pageable = PageRequest.of(0, limit);
        return tripRepository.findByCompanyId(companyId, pageable).stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get ongoing trips by company
     */
    @Transactional(readOnly = true)
    public List<TripResponse> getOngoingTrips(Long companyId) {
        log.debug("Fetching ongoing trips for company ID: {}", companyId);

        return tripRepository.findOngoingTripsByCompany(companyId).stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Calculate trip statistics from telemetry data
     */
    private void calculateTripStatistics(Trip trip) {
        // Calculate average and max speed
        Double avgSpeed = telemetryRepository.calculateAverageSpeedForTrip(trip.getId());
        Double maxSpeed = telemetryRepository.findMaxSpeedForTrip(trip.getId());
        Double distance = telemetryRepository.calculateDistanceForTrip(trip.getId());

        trip.setAverageSpeed(avgSpeed != null ? avgSpeed : 0.0);
        trip.setMaxSpeed(maxSpeed != null ? maxSpeed : 0.0);
        trip.setDistance(distance != null ? distance : 0.0);

        // Calculate energy consumed based on battery SOC difference
        if (trip.getStartBatterySoc() != null && trip.getEndBatterySoc() != null) {
            Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId()).orElse(null);
            if (vehicle != null && vehicle.getBatteryCapacity() != null) {
                double socDifference = trip.getStartBatterySoc() - trip.getEndBatterySoc();
                trip.setEnergyConsumed((socDifference / 100.0) * vehicle.getBatteryCapacity());
            }
        }
    }

    /**
     * Calculate efficiency score based on various factors
     */
    private Double calculateEfficiencyScore(Trip trip) {
        double score = 100.0;

        // Deduct points for harsh events
        if (trip.getHarshAccelerationCount() != null) {
            score -= trip.getHarshAccelerationCount() * 2.0;
        }
        if (trip.getHarshBrakingCount() != null) {
            score -= trip.getHarshBrakingCount() * 2.0;
        }
        if (trip.getOverspeedingCount() != null) {
            score -= trip.getOverspeedingCount() * 3.0;
        }

        // Deduct points for excessive idle time (more than 10% of trip)
        if (trip.getIdleTimeMinutes() != null && trip.getDurationMinutes() != null) {
            double idlePercentage = (trip.getIdleTimeMinutes().doubleValue() / trip.getDurationMinutes()) * 100;
            if (idlePercentage > 10) {
                score -= (idlePercentage - 10) * 0.5;
            }
        }

        // Reward efficient energy consumption (if available)
        if (trip.getDistance() != null && trip.getEnergyConsumed() != null && trip.getDistance() > 0) {
            double energyEfficiency = trip.getEnergyConsumed() / trip.getDistance(); // kWh per km
            // Lower is better; typical EV efficiency is 0.15-0.20 kWh/km
            if (energyEfficiency > 0.20) {
                score -= (energyEfficiency - 0.20) * 50;
            }
        }

        // Ensure score is between 0 and 100
        return Math.max(0.0, Math.min(100.0, score));
    }
}
