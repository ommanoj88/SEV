package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.VehicleEventResponse;
import com.evfleet.fleet.model.VehicleEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing vehicle events
 */
public interface VehicleEventService {

    /**
     * Record a new vehicle event
     */
    VehicleEventResponse recordEvent(VehicleEvent event);

    /**
     * Get all events for a vehicle
     */
    Page<VehicleEventResponse> getVehicleEvents(Long vehicleId, Pageable pageable);

    /**
     * Get events by type for a vehicle
     */
    Page<VehicleEventResponse> getVehicleEventsByType(
            Long vehicleId, VehicleEvent.EventType eventType, Pageable pageable);

    /**
     * Get events within a time range
     */
    List<VehicleEventResponse> getVehicleEventsByTimeRange(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get critical events for a vehicle
     */
    List<VehicleEventResponse> getCriticalEvents(Long vehicleId);

    /**
     * Get recent events (last N days)
     */
    List<VehicleEventResponse> getRecentEvents(Long vehicleId, int days);

    /**
     * Get event count by type
     */
    Long getEventCountByType(Long vehicleId, VehicleEvent.EventType eventType);

    /**
     * Get all events for a company
     */
    Page<VehicleEventResponse> getCompanyEvents(Long companyId, Pageable pageable);
}
