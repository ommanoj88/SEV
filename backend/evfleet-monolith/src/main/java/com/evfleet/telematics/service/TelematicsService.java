package com.evfleet.telematics.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.telematics.dto.DrivingEventResponse;
import com.evfleet.telematics.dto.TelematicsEventRequest;
import com.evfleet.telematics.model.DrivingEvent;
import com.evfleet.telematics.provider.FlespiTelematicsProvider;
import com.evfleet.telematics.provider.TelemetryProvider;
import com.evfleet.telematics.repository.DrivingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Telematics Service
 * Handles ingestion and retrieval of driving events
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TelematicsService {

    private final DrivingEventRepository drivingEventRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;
    private final List<TelemetryProvider> telemetryProviders;
    private final Optional<FlespiTelematicsProvider> flespiProvider;

    /**
     * Ingest a telematics event from vehicle sensors
     */
    public DrivingEventResponse ingestEvent(TelematicsEventRequest request) {
        log.info("Ingesting telematics event: type={}, vehicleId={}", request.getType(), request.getVehicleId());

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        // Get current driver if vehicle is on a trip
        Long driverId = vehicle.getCurrentDriverId();
        if (driverId == null && request.getTripId() != null) {
            // Try to get driver from trip
            Trip trip = tripRepository.findById(request.getTripId()).orElse(null);
            if (trip != null) {
                driverId = trip.getDriverId();
            }
        }

        if (driverId == null) {
            log.warn("No driver assigned to vehicle {} during event {}", request.getVehicleId(), request.getType());
            throw new IllegalStateException("Cannot record driving event: No driver assigned to vehicle");
        }

        // Validate driver exists (make final for lambda)
        final Long finalDriverId = driverId;
        Driver driver = driverRepository.findById(finalDriverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", finalDriverId));

        // Create driving event
        DrivingEvent event = DrivingEvent.builder()
                .tripId(request.getTripId())
                .driverId(finalDriverId)
                .vehicleId(request.getVehicleId())
                .companyId(vehicle.getCompanyId())
                .eventType(DrivingEvent.EventType.valueOf(request.getType().toUpperCase()))
                .eventTime(request.getTimestamp())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .gForce(request.getGForce())
                .severity(request.getSeverity() != null ? 
                    DrivingEvent.Severity.valueOf(request.getSeverity().toUpperCase()) : null)
                .duration(request.getDuration())
                .speedLimit(request.getSpeedLimit())
                .description(request.getDescription())
                .build();

        DrivingEvent saved = drivingEventRepository.save(event);
        log.info("Telematics event recorded: id={}, type={}, driverId={}", 
            saved.getId(), saved.getEventType(), saved.getDriverId());

        return DrivingEventResponse.fromEntity(saved);
    }

    /**
     * Get all events for a specific trip
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByTrip(Long tripId) {
        log.info("Fetching driving events for trip: {}", tripId);
        List<DrivingEvent> events = drivingEventRepository.findByTripId(tripId);
        return events.stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all events for a specific driver
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByDriver(Long driverId, LocalDateTime start, LocalDateTime end) {
        log.info("Fetching driving events for driver: {} between {} and {}", driverId, start, end);
        
        List<DrivingEvent> events;
        if (start != null && end != null) {
            events = drivingEventRepository.findByDriverIdAndEventTimeBetween(driverId, start, end);
        } else {
            events = drivingEventRepository.findByDriverId(driverId);
        }
        
        return events.stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all events for a specific vehicle
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByVehicle(Long vehicleId) {
        log.info("Fetching driving events for vehicle: {}", vehicleId);
        List<DrivingEvent> events = drivingEventRepository.findByVehicleId(vehicleId);
        return events.stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get event statistics for a driver
     */
    @Transactional(readOnly = true)
    public DriverEventStats getDriverEventStats(Long driverId, LocalDateTime start, LocalDateTime end) {
        log.info("Calculating event statistics for driver: {} between {} and {}", driverId, start, end);
        
        long harshBraking = drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
            driverId, DrivingEvent.EventType.HARSH_BRAKING, start, end);
        long harshAcceleration = drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
            driverId, DrivingEvent.EventType.HARSH_ACCELERATION, start, end);
        long speeding = drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
            driverId, DrivingEvent.EventType.SPEEDING, start, end);
        long idling = drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
            driverId, DrivingEvent.EventType.IDLING, start, end);

        return new DriverEventStats(harshBraking, harshAcceleration, speeding, idling);
    }

    /**
     * Inner class for driver event statistics
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DriverEventStats {
        private long harshBrakingCount;
        private long harshAccelerationCount;
        private long speedingCount;
        private long idlingCount;

        public long getTotalEvents() {
            return harshBrakingCount + harshAccelerationCount + speedingCount + idlingCount;
        }
    }

    // ================ Provider Management Methods ================

    /**
     * Get health status of all configured telematics providers
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProviderHealthStatus() {
        log.info("Getting health status for all telematics providers");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("totalProviders", telemetryProviders.size());
        
        Map<String, Object> providerStatuses = new HashMap<>();
        for (TelemetryProvider provider : telemetryProviders) {
            Map<String, Object> status = new HashMap<>();
            status.put("name", provider.getProviderName());
            status.put("sourceType", provider.getSourceType());
            status.put("connected", provider.testConnection());
            status.put("updateIntervalSeconds", provider.getUpdateIntervalSeconds());
            status.put("supportedFields", provider.getSupportedDataFields());
            providerStatuses.put(provider.getProviderId(), status);
        }
        
        // Add flespi-specific health if available
        if (flespiProvider.isPresent()) {
            FlespiTelematicsProvider flespi = flespiProvider.get();
            providerStatuses.put("flespi_detailed", flespi.getHealthStatus());
        }
        
        healthStatus.put("providers", providerStatuses);
        return healthStatus;
    }

    /**
     * Get list of available telematics providers
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableProviders() {
        log.info("Listing available telematics providers");
        return telemetryProviders.stream()
            .map(p -> p.getProviderId() + " (" + p.getProviderName() + ")")
            .collect(Collectors.toList());
    }

    /**
     * Test connection to a specific provider
     */
    @Transactional(readOnly = true)
    public boolean testProviderConnection(String providerId) {
        log.info("Testing connection to provider: {}", providerId);
        
        return telemetryProviders.stream()
            .filter(p -> p.getProviderId().equals(providerId))
            .findFirst()
            .map(TelemetryProvider::testConnection)
            .orElseThrow(() -> new ResourceNotFoundException("TelemetryProvider", "id", providerId));
    }
}
