package com.evfleet.telematics.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.telematics.dto.DrivingEventRequest;
import com.evfleet.telematics.dto.DrivingEventResponse;
import com.evfleet.telematics.model.DrivingEvent;
import com.evfleet.telematics.repository.DrivingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Driving Event Service
 * 
 * Handles all driving event-related business logic including:
 * - Recording driving events from telematics
 * - Retrieving events by trip, driver, or vehicle
 * - Calculating driver safety scores
 *
 * Safety Score Algorithm:
 * - Base score: 100
 * - Harsh braking: -5 (LOW), -7 (MEDIUM), -10 (HIGH), -15 (CRITICAL)
 * - Speeding: -5 (LOW), -10 (MEDIUM), -15 (HIGH), -20 (CRITICAL)
 * - Harsh acceleration: -3 (LOW), -5 (MEDIUM), -7 (HIGH), -10 (CRITICAL)
 * - Harsh cornering: -3 (LOW), -5 (MEDIUM), -7 (HIGH), -10 (CRITICAL)
 * - Rapid lane change: -2 (LOW), -4 (MEDIUM), -6 (HIGH), -8 (CRITICAL)
 * - Idling: -1 per 5 minutes
 * - Minimum score: 0, Maximum score: 100
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DrivingEventService {

    private final DrivingEventRepository drivingEventRepository;
    private final DriverRepository driverRepository;

    private static final int BASE_SAFETY_SCORE = 100;
    private static final int MIN_SAFETY_SCORE = 0;
    private static final int MAX_SAFETY_SCORE = 100;

    // Penalty points by event type and severity
    private static final int[][] HARSH_BRAKING_PENALTIES = {{5, 7, 10, 15}};
    private static final int[][] SPEEDING_PENALTIES = {{5, 10, 15, 20}};
    private static final int[][] HARSH_ACCELERATION_PENALTIES = {{3, 5, 7, 10}};
    private static final int[][] HARSH_CORNERING_PENALTIES = {{3, 5, 7, 10}};
    private static final int[][] RAPID_LANE_CHANGE_PENALTIES = {{2, 4, 6, 8}};
    private static final int IDLING_PENALTY_PER_5_MINUTES = 1;

    /**
     * Record a new driving event from telematics data.
     * 
     * @param request The driving event request DTO
     * @return The created event response
     */
    public DrivingEventResponse recordEvent(DrivingEventRequest request) {
        log.info("Recording driving event: type={}, driver={}, vehicle={}", 
            request.getEventType(), request.getDriverId(), request.getVehicleId());

        // Validate driver exists
        if (!driverRepository.existsById(request.getDriverId())) {
            throw new ResourceNotFoundException("Driver", "id", request.getDriverId());
        }

        // Validate coordinates if provided
        if (request.getLatitude() != null) {
            if (request.getLatitude() < -90 || request.getLatitude() > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
        }
        if (request.getLongitude() != null) {
            if (request.getLongitude() < -180 || request.getLongitude() > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
        }

        // Validate speed is non-negative
        if (request.getSpeed() != null && request.getSpeed() < 0) {
            throw new IllegalArgumentException("Speed cannot be negative");
        }

        // Validate g-force is non-negative
        if (request.getGForce() != null && request.getGForce().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("G-force cannot be negative");
        }

        // Determine severity based on event type if not provided
        DrivingEvent.Severity severity = request.getSeverity();
        if (severity == null) {
            severity = determineSeverity(request);
        }

        DrivingEvent event = DrivingEvent.builder()
                .tripId(request.getTripId())
                .driverId(request.getDriverId())
                .vehicleId(request.getVehicleId())
                .companyId(request.getCompanyId())
                .eventType(request.getEventType())
                .eventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .gForce(request.getGForce())
                .severity(severity)
                .duration(request.getDuration())
                .speedLimit(request.getSpeedLimit())
                .description(request.getDescription())
                .build();

        DrivingEvent saved = drivingEventRepository.save(event);
        log.info("Driving event recorded: id={}, severity={}", saved.getId(), severity);

        // Update driver safety score
        updateDriverSafetyScore(request.getDriverId());

        return DrivingEventResponse.fromEntity(saved);
    }

    /**
     * Determine event severity based on event type and metrics.
     */
    private DrivingEvent.Severity determineSeverity(DrivingEventRequest request) {
        if (request.getEventType() == DrivingEvent.EventType.SPEEDING) {
            Double speed = request.getSpeed();
            Double speedLimit = request.getSpeedLimit();
            if (speed != null && speedLimit != null && speedLimit > 0) {
                double excessPercent = ((speed - speedLimit) / speedLimit) * 100;
                if (excessPercent > 50) return DrivingEvent.Severity.CRITICAL;
                if (excessPercent > 30) return DrivingEvent.Severity.HIGH;
                if (excessPercent > 15) return DrivingEvent.Severity.MEDIUM;
                return DrivingEvent.Severity.LOW;
            }
        }

        if (request.getGForce() != null) {
            double gForce = request.getGForce().doubleValue();
            if (gForce > 1.0) return DrivingEvent.Severity.CRITICAL;
            if (gForce > 0.7) return DrivingEvent.Severity.HIGH;
            if (gForce > 0.5) return DrivingEvent.Severity.MEDIUM;
            return DrivingEvent.Severity.LOW;
        }

        if (request.getEventType() == DrivingEvent.EventType.IDLING) {
            Integer duration = request.getDuration();
            if (duration != null) {
                if (duration > 900) return DrivingEvent.Severity.CRITICAL;  // > 15 min
                if (duration > 600) return DrivingEvent.Severity.HIGH;      // > 10 min
                if (duration > 300) return DrivingEvent.Severity.MEDIUM;    // > 5 min
                return DrivingEvent.Severity.LOW;
            }
        }

        // Default to MEDIUM if can't determine
        return DrivingEvent.Severity.MEDIUM;
    }

    /**
     * Get all driving events for a specific trip.
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByTrip(Long tripId) {
        log.info("Fetching driving events for trip: {}", tripId);
        return drivingEventRepository.findByTripId(tripId).stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all driving events for a driver within a time range.
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByDriver(Long driverId, LocalDateTime start, LocalDateTime end) {
        log.info("Fetching driving events for driver {} from {} to {}", driverId, start, end);
        
        if (start == null) {
            start = LocalDateTime.now().minusDays(30); // Default to last 30 days
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        
        return drivingEventRepository.findByDriverIdAndEventTimeBetween(driverId, start, end).stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all driving events for a specific vehicle.
     */
    @Transactional(readOnly = true)
    public List<DrivingEventResponse> getEventsByVehicle(Long vehicleId) {
        log.info("Fetching driving events for vehicle: {}", vehicleId);
        return drivingEventRepository.findByVehicleId(vehicleId).stream()
                .map(DrivingEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Calculate driver safety score based on recent events.
     * Uses a weighted algorithm considering event types and severities.
     * 
     * @param driverId The driver ID
     * @return Safety score (0-100)
     */
    @Transactional(readOnly = true)
    public double calculateDriverSafetyScore(Long driverId) {
        log.info("Calculating safety score for driver: {}", driverId);
        
        // Look at events from last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        List<DrivingEvent> recentEvents = drivingEventRepository.findByDriverIdAndEventTimeBetween(
            driverId, thirtyDaysAgo, now);

        if (recentEvents.isEmpty()) {
            log.info("No recent events for driver {}, returning base score", driverId);
            return BASE_SAFETY_SCORE;
        }

        int totalPenalty = 0;
        
        for (DrivingEvent event : recentEvents) {
            totalPenalty += calculateEventPenalty(event);
        }

        int score = BASE_SAFETY_SCORE - totalPenalty;
        
        // Clamp score to valid range
        score = Math.max(MIN_SAFETY_SCORE, Math.min(MAX_SAFETY_SCORE, score));
        
        log.info("Driver {} safety score: {} (penalty: {}, events: {})", 
            driverId, score, totalPenalty, recentEvents.size());
        
        return score;
    }

    /**
     * Calculate penalty points for a single event.
     */
    private int calculateEventPenalty(DrivingEvent event) {
        int severityIndex = getSeverityIndex(event.getSeverity());
        
        switch (event.getEventType()) {
            case HARSH_BRAKING:
                return getPenalty(HARSH_BRAKING_PENALTIES, severityIndex);
            case SPEEDING:
                return getPenalty(SPEEDING_PENALTIES, severityIndex);
            case HARSH_ACCELERATION:
                return getPenalty(HARSH_ACCELERATION_PENALTIES, severityIndex);
            case HARSH_CORNERING:
                return getPenalty(HARSH_CORNERING_PENALTIES, severityIndex);
            case RAPID_LANE_CHANGE:
                return getPenalty(RAPID_LANE_CHANGE_PENALTIES, severityIndex);
            case IDLING:
                // Penalty based on duration
                if (event.getDuration() != null) {
                    return (event.getDuration() / 300) * IDLING_PENALTY_PER_5_MINUTES;
                }
                return IDLING_PENALTY_PER_5_MINUTES;
            case DISTRACTED_DRIVING:
                // High penalty for distracted driving
                return 10 + (severityIndex * 5);
            default:
                return 0;
        }
    }

    private int getSeverityIndex(DrivingEvent.Severity severity) {
        if (severity == null) return 1; // Default to MEDIUM
        switch (severity) {
            case LOW: return 0;
            case MEDIUM: return 1;
            case HIGH: return 2;
            case CRITICAL: return 3;
            default: return 1;
        }
    }

    private int getPenalty(int[][] penaltyTable, int severityIndex) {
        if (penaltyTable.length > 0 && penaltyTable[0].length > severityIndex) {
            return penaltyTable[0][severityIndex];
        }
        return 0;
    }

    /**
     * Update driver's safety score in the database.
     */
    private void updateDriverSafetyScore(Long driverId) {
        double newScore = calculateDriverSafetyScore(driverId);
        
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver != null) {
            driver.setSafetyScore(newScore);
            driverRepository.save(driver);
            log.info("Updated driver {} safety score to {}", driverId, newScore);
        }
    }

    /**
     * Get event count by type for a driver within a time range.
     */
    @Transactional(readOnly = true)
    public long getEventCountByType(Long driverId, DrivingEvent.EventType eventType, 
                                    LocalDateTime start, LocalDateTime end) {
        return drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
            driverId, eventType, start, end);
    }

    /**
     * Get event breakdown for a driver (count per event type).
     */
    @Transactional(readOnly = true)
    public java.util.Map<DrivingEvent.EventType, Long> getEventBreakdown(Long driverId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        java.util.Map<DrivingEvent.EventType, Long> breakdown = new java.util.HashMap<>();
        
        for (DrivingEvent.EventType eventType : DrivingEvent.EventType.values()) {
            long count = getEventCountByType(driverId, eventType, thirtyDaysAgo, now);
            if (count > 0) {
                breakdown.put(eventType, count);
            }
        }
        
        return breakdown;
    }
}
