package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.common.exception.InvalidInputException;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.event.TripCompletedEvent;
import com.evfleet.fleet.event.TripStartedEvent;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.TripLocationHistory;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.TripLocationHistoryRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Trip Service
 * 
 * PR #6: Added teleportation prevention and trip path history
 * - updateTripLocation() validates speed between consecutive updates
 * - Stores complete path history for trip replay
 * - Rejects impossible location jumps (teleportation)
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final TripLocationHistoryRepository locationHistoryRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final EventPublisher eventPublisher;
    private final MaintenanceService maintenanceService;
    
    private static final double MAX_SPEED_KMH = 200.0; // Maximum realistic speed
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers
    private static final double MIN_TIME_BETWEEN_UPDATES_SECONDS = 1.0; // Minimum 1 second between updates

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

        // Validate and check driver availability (if driver is assigned)
        if (driverId != null) {
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));
            
            // Check if driver is already on a trip
            if (driver.getStatus() == Driver.DriverStatus.ON_TRIP) {
                throw new IllegalStateException("Driver is already assigned to another trip");
            }
            
            // Update driver status
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

        // Record the starting location in path history
        TripLocationHistory startLocation = TripLocationHistory.builder()
            .tripId(saved.getId())
            .latitude(startLat)
            .longitude(startLon)
            .recordedAt(LocalDateTime.now())
            .sequenceNumber(0)
            .distanceFromPrevious(0.0)
            .cumulativeDistance(0.0)
            .speed(0.0)
            .teleportationWarning(false)
            .build();
        locationHistoryRepository.save(startLocation);

        // Publish event
        eventPublisher.publish(new TripStartedEvent(this, saved.getId(), vehicleId, driverId));

        log.info("Trip started with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update trip location during an active trip.
     * Validates that the location update is physically possible (no teleportation).
     * Stores location in path history for trip replay.
     * 
     * @param tripId The trip ID
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @return Updated TripLocationHistory record
     * @throws ResourceNotFoundException if trip not found
     * @throws IllegalStateException if trip is not in progress
     * @throws InvalidInputException if location update implies impossible speed (teleportation)
     */
    public TripLocationHistory updateTripLocation(Long tripId, Double latitude, Double longitude) {
        log.info("Updating trip location: tripId={}, lat={}, lon={}", tripId, latitude, longitude);

        // Validate coordinates
        validateLatitude(latitude);
        validateLongitude(longitude);

        // Find the trip
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot update location for trip that is not in progress");
        }

        LocalDateTime now = LocalDateTime.now();
        Double speed = 0.0;
        Double distanceFromPrevious = 0.0;
        Double cumulativeDistance = 0.0;
        boolean teleportationWarning = false;
        int sequenceNumber = 0;

        // Get the last recorded location
        Optional<TripLocationHistory> lastLocationOpt = 
            locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(tripId);

        if (lastLocationOpt.isPresent()) {
            TripLocationHistory lastLocation = lastLocationOpt.get();
            sequenceNumber = lastLocation.getSequenceNumber() + 1;

            // Calculate distance and time delta
            distanceFromPrevious = calculateHaversineDistance(
                lastLocation.getLatitude(), lastLocation.getLongitude(),
                latitude, longitude
            );

            Duration timeDelta = Duration.between(lastLocation.getRecordedAt(), now);
            double timeDeltaSeconds = timeDelta.toMillis() / 1000.0;

            // Avoid division by zero and too-rapid updates
            if (timeDeltaSeconds < MIN_TIME_BETWEEN_UPDATES_SECONDS) {
                log.warn("Trip {}: Location update too rapid ({}ms since last update)", 
                    tripId, timeDelta.toMillis());
                throw new InvalidInputException("locationUpdate", 
                    "Location updates must be at least 1 second apart");
            }

            // Calculate speed in km/h
            double timeDeltaHours = timeDeltaSeconds / 3600.0;
            speed = distanceFromPrevious / timeDeltaHours;

            // Check for teleportation (impossible speed)
            if (speed > MAX_SPEED_KMH) {
                log.warn("Trip {}: TELEPORTATION DETECTED - Speed {} km/h from ({}, {}) to ({}, {}) in {} seconds",
                    tripId, speed, 
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    latitude, longitude, timeDeltaSeconds);

                teleportationWarning = true;

                throw new InvalidInputException("location",
                    String.format("Impossible speed detected: %.2f km/h. Maximum allowed: %.2f km/h. " +
                        "Distance: %.2f km in %.2f seconds. This appears to be teleportation.",
                        speed, MAX_SPEED_KMH, distanceFromPrevious, timeDeltaSeconds));
            }

            // Calculate cumulative distance
            cumulativeDistance = (lastLocation.getCumulativeDistance() != null ? 
                lastLocation.getCumulativeDistance() : 0.0) + distanceFromPrevious;
        }

        // Create and save the location history record
        TripLocationHistory locationRecord = TripLocationHistory.builder()
            .tripId(tripId)
            .latitude(latitude)
            .longitude(longitude)
            .recordedAt(now)
            .sequenceNumber(sequenceNumber)
            .distanceFromPrevious(distanceFromPrevious)
            .cumulativeDistance(cumulativeDistance)
            .speed(speed)
            .teleportationWarning(teleportationWarning)
            .build();

        TripLocationHistory saved = locationHistoryRepository.save(locationRecord);

        // Update vehicle's current location
        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", trip.getVehicleId()));
        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicle.setLastUpdated(now);
        vehicleRepository.save(vehicle);

        log.debug("Trip {}: Location updated - seq={}, dist={:.2f}km, speed={:.2f}km/h", 
            tripId, sequenceNumber, distanceFromPrevious, speed);

        return saved;
    }

    /**
     * Get the complete path history for a trip (for trip replay)
     */
    @Transactional(readOnly = true)
    public List<TripLocationHistory> getTripPath(Long tripId) {
        return locationHistoryRepository.findByTripIdOrderBySequenceNumberAsc(tripId);
    }

    /**
     * Get calculated distance from path history
     */
    @Transactional(readOnly = true)
    public Double getCalculatedTripDistance(Long tripId) {
        return locationHistoryRepository.getTotalDistanceByTripId(tripId);
    }

    private void validateLatitude(Double latitude) {
        if (latitude == null) {
            throw new InvalidInputException("latitude", "is required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidInputException("latitude", "must be between -90 and 90");
        }
    }

    private void validateLongitude(Double longitude) {
        if (longitude == null) {
            throw new InvalidInputException("longitude", "is required");
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidInputException("longitude", "must be between -180 and 180");
        }
    }

    public Trip completeTrip(Long tripId, Double endLat, Double endLon,
                            Double distance, BigDecimal energyConsumed, BigDecimal fuelConsumed) {
        log.info("Completing trip: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete trip that is not in progress");
        }

        // Validate distance
        if (distance == null || distance < 0) {
            throw new IllegalArgumentException("Distance must be a positive value");
        }

        // Calculate duration
        long durationSeconds = java.time.Duration.between(trip.getStartTime(), LocalDateTime.now()).getSeconds();
        
        // Validate speed (distance vs duration check)
        if (durationSeconds > 0) {
            double durationHours = durationSeconds / 3600.0;
            double averageSpeed = distance / durationHours;
            
            if (averageSpeed > MAX_SPEED_KMH) {
                log.warn("Trip {}: Suspicious speed detected - {} km/h (distance: {} km, duration: {} hours)", 
                    tripId, averageSpeed, distance, durationHours);
                throw new IllegalArgumentException(
                    String.format("Impossible average speed: %.2f km/h. Maximum allowed: %.2f km/h", 
                        averageSpeed, MAX_SPEED_KMH));
            }
        }
        
        // Geospatial validation using Haversine formula
        if (trip.getStartLatitude() != null && trip.getStartLongitude() != null 
            && endLat != null && endLon != null) {
            double haversineDistance = calculateHaversineDistance(
                trip.getStartLatitude(), trip.getStartLongitude(), endLat, endLon);
            
            // Check if claimed distance is suspiciously different from straight-line distance
            if (distance < haversineDistance * 0.8) {
                log.warn("Trip {}: Claimed distance ({} km) is less than straight-line distance ({} km)", 
                    tripId, distance, haversineDistance);
                throw new IllegalArgumentException(
                    String.format("Claimed distance (%.2f km) cannot be less than straight-line distance (%.2f km)", 
                        distance, haversineDistance));
            }
            
            // Log warning if distance is suspiciously high (more than 3x straight-line)
            if (distance > haversineDistance * 3.0 && haversineDistance > 1.0) {
                log.warn("Trip {}: Claimed distance ({} km) is much larger than straight-line distance ({} km). " +
                    "This might indicate circuitous routing.", tripId, distance, haversineDistance);
            }
        }

        // Complete trip
        trip.complete(endLat, endLon, distance, durationSeconds);
        trip.setEnergyConsumed(energyConsumed);
        trip.setFuelConsumed(fuelConsumed);

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
        
        // Update energy consumption for EV/Hybrid
        if (energyConsumed != null && energyConsumed.compareTo(BigDecimal.ZERO) > 0) {
            if (vehicle.getTotalEnergyConsumed() == null) {
                vehicle.setTotalEnergyConsumed(0.0);
            }
            vehicle.setTotalEnergyConsumed(vehicle.getTotalEnergyConsumed() + energyConsumed.doubleValue());
            
            // Update battery SOC if applicable
            if (vehicle.getBatteryCapacity() != null && vehicle.getBatteryCapacity() > 0) {
                double energyConsumedKwh = energyConsumed.doubleValue();
                double socDecrease = (energyConsumedKwh / vehicle.getBatteryCapacity()) * 100.0;
                double newSoc = Math.max(0, (vehicle.getCurrentBatterySoc() != null ? vehicle.getCurrentBatterySoc() : 100.0) - socDecrease);
                vehicle.setCurrentBatterySoc(newSoc);
            }
        }
        
        // Update fuel consumption for ICE/Hybrid
        if (fuelConsumed != null && fuelConsumed.compareTo(BigDecimal.ZERO) > 0) {
            if (vehicle.getTotalFuelConsumed() == null) {
                vehicle.setTotalFuelConsumed(0.0);
            }
            vehicle.setTotalFuelConsumed(vehicle.getTotalFuelConsumed() + fuelConsumed.doubleValue());
            
            // Update fuel level if applicable
            if (vehicle.getFuelLevel() != null) {
                double newFuelLevel = Math.max(0, vehicle.getFuelLevel() - fuelConsumed.doubleValue());
                vehicle.setFuelLevel(newFuelLevel);
            }
        }

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
            this, tripId, trip.getVehicleId(), distance, durationSeconds, energyConsumed
        ));

        // Check and auto-schedule maintenance based on mileage policies
        maintenanceService.checkAndCreateMaintenanceByMileage(
                vehicle.getId(),
                vehicle.getCompanyId(),
                vehicle.getType(),
                vehicle.getTotalDistance()
        );

        log.info("Trip completed: {} - Distance: {} km, Duration: {} sec", tripId, distance, durationSeconds);
        return completed;
    }
    
    /**
     * Calculate the great-circle distance between two points using the Haversine formula
     * @param lat1 Start latitude
     * @param lon1 Start longitude
     * @param lat2 End latitude
     * @param lon2 End longitude
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
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
