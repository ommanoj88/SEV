package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
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
    private final DriverRepository driverRepository;
    private final EventPublisher eventPublisher;

    // Constants for validation
    private static final double MAX_REASONABLE_SPEED_KMH = 200.0; // km/h
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in km
    private static final double DISTANCE_TOLERANCE_FACTOR = 2.0; // Allow claimed distance up to 2x Haversine distance

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

        // Validate and check driver availability if driver is provided
        if (driverId != null) {
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

            // Check if driver is already on a trip
            if (driver.getStatus() == Driver.DriverStatus.ON_TRIP) {
                throw new IllegalStateException("Driver is already assigned to another trip");
            }

            // Update driver status to ON_TRIP
            driver.setStatus(Driver.DriverStatus.ON_TRIP);
            driver.setCurrentVehicleId(vehicleId);
            driverRepository.save(driver);
        }

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

        // Validate distance is not negative
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }

        // Calculate duration in seconds
        long duration = java.time.Duration.between(trip.getStartTime(), LocalDateTime.now()).getSeconds();

        // Validate speed is reasonable (distance / duration should not exceed MAX_REASONABLE_SPEED_KMH)
        if (duration > 0) {
            double speedKmh = (distance / duration) * 3600; // Convert to km/h
            if (speedKmh > MAX_REASONABLE_SPEED_KMH) {
                throw new IllegalArgumentException(
                    String.format("Impossible speed detected: %.2f km/h. Distance: %.2f km, Duration: %d sec. " +
                        "Maximum allowed speed is %.2f km/h", 
                        speedKmh, distance, duration, MAX_REASONABLE_SPEED_KMH)
                );
            }
        }

        // Validate geospatial consistency using Haversine formula
        if (trip.getStartLatitude() != null && trip.getStartLongitude() != null) {
            double haversineDistance = calculateHaversineDistance(
                trip.getStartLatitude(), trip.getStartLongitude(),
                endLat, endLon
            );

            // Check if claimed distance is suspiciously larger than straight-line distance
            if (distance > haversineDistance * DISTANCE_TOLERANCE_FACTOR) {
                log.warn("Suspicious distance for trip {}: Claimed {} km vs Haversine {} km", 
                    tripId, distance, haversineDistance);
            }

            // Check if claimed distance is impossibly smaller than straight-line distance
            if (distance < haversineDistance * 0.5) {
                throw new IllegalArgumentException(
                    String.format("Claimed distance (%.2f km) is impossibly smaller than straight-line distance (%.2f km)", 
                        distance, haversineDistance)
                );
            }
        }

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

        // Update driver status if driver was assigned
        if (trip.getDriverId() != null) {
            driverRepository.findById(trip.getDriverId()).ifPresent(driver -> {
                driver.setStatus(Driver.DriverStatus.ACTIVE);
                driver.setCurrentVehicleId(null);
                
                // Update driver statistics
                if (driver.getTotalTrips() == null) {
                    driver.setTotalTrips(0);
                }
                driver.setTotalTrips(driver.getTotalTrips() + 1);
                
                if (driver.getTotalDistance() == null) {
                    driver.setTotalDistance(0.0);
                }
                driver.setTotalDistance(driver.getTotalDistance() + distance);
                
                driverRepository.save(driver);
            });
        }

        // Publish event
        eventPublisher.publish(new TripCompletedEvent(
            this, tripId, trip.getVehicleId(), distance, duration, energyConsumed
        ));

        log.info("Trip completed: {} - Distance: {} km, Duration: {} sec", tripId, distance, duration);
        return completed;
    }

    /**
     * Calculate the Haversine distance between two points on Earth
     * @param lat1 Start latitude
     * @param lon1 Start longitude
     * @param lat2 End latitude
     * @param lon2 End longitude
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        return EARTH_RADIUS_KM * c;
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
