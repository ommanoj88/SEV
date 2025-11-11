package com.evfleet.fleet.service.impl;

import com.evfleet.fleet.dto.VehicleEventResponse;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.model.VehicleEvent;
import com.evfleet.fleet.repository.VehicleEventRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.VehicleEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VehicleEventService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleEventServiceImpl implements VehicleEventService {

    private final VehicleEventRepository vehicleEventRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public VehicleEventResponse recordEvent(VehicleEvent event) {
        log.info("Recording event: {} for vehicle: {}", event.getEventType(), event.getVehicleId());
        
        // Set default timestamp if not provided
        if (event.getEventTimestamp() == null) {
            event.setEventTimestamp(LocalDateTime.now());
        }
        
        VehicleEvent savedEvent = vehicleEventRepository.save(event);
        return convertToResponse(savedEvent);
    }

    @Override
    public Page<VehicleEventResponse> getVehicleEvents(Long vehicleId, Pageable pageable) {
        log.debug("Fetching events for vehicle: {}", vehicleId);
        return vehicleEventRepository
                .findByVehicleIdOrderByEventTimestampDesc(vehicleId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public Page<VehicleEventResponse> getVehicleEventsByType(
            Long vehicleId, VehicleEvent.EventType eventType, Pageable pageable) {
        log.debug("Fetching events of type {} for vehicle: {}", eventType, vehicleId);
        return vehicleEventRepository
                .findByVehicleIdAndEventTypeOrderByEventTimestampDesc(vehicleId, eventType, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public List<VehicleEventResponse> getVehicleEventsByTimeRange(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Fetching events for vehicle: {} between {} and {}", vehicleId, startTime, endTime);
        return vehicleEventRepository
                .findEventsByVehicleIdAndTimeRange(vehicleId, startTime, endTime)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleEventResponse> getCriticalEvents(Long vehicleId) {
        log.debug("Fetching critical events for vehicle: {}", vehicleId);
        return vehicleEventRepository
                .findCriticalEventsByVehicleId(vehicleId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleEventResponse> getRecentEvents(Long vehicleId, int days) {
        log.debug("Fetching recent events (last {} days) for vehicle: {}", days, vehicleId);
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return vehicleEventRepository
                .findRecentEventsByVehicleId(vehicleId, sinceDate)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long getEventCountByType(Long vehicleId, VehicleEvent.EventType eventType) {
        return vehicleEventRepository.countEventsByVehicleIdAndEventType(vehicleId, eventType);
    }

    @Override
    public Page<VehicleEventResponse> getCompanyEvents(Long companyId, Pageable pageable) {
        log.debug("Fetching events for company: {}", companyId);
        return vehicleEventRepository
                .findByCompanyIdOrderByEventTimestampDesc(companyId, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Convert VehicleEvent entity to VehicleEventResponse DTO
     */
    private VehicleEventResponse convertToResponse(VehicleEvent event) {
        VehicleEventResponse response = new VehicleEventResponse();
        response.setId(event.getId());
        response.setVehicleId(event.getVehicleId());
        
        // Fetch vehicle number if available
        vehicleRepository.findById(event.getVehicleId()).ifPresent(vehicle -> {
            response.setVehicleNumber(vehicle.getVehicleNumber());
        });
        
        response.setEventType(event.getEventType());
        response.setEventSubtype(event.getEventSubtype());
        response.setEventTimestamp(event.getEventTimestamp());
        response.setSeverity(event.getSeverity());
        response.setEventData(event.getEventData());
        
        response.setLatitude(event.getLatitude());
        response.setLongitude(event.getLongitude());
        response.setLocationName(event.getLocationName());
        
        response.setDriverId(event.getDriverId());
        response.setTripId(event.getTripId());
        response.setMaintenanceId(event.getMaintenanceId());
        response.setChargingSessionId(event.getChargingSessionId());
        
        response.setBatterySoc(event.getBatterySoc());
        response.setFuelLevel(event.getFuelLevel());
        response.setOdometer(event.getOdometer());
        response.setSpeed(event.getSpeed());
        
        response.setSource(event.getSource());
        response.setUserId(event.getUserId());
        response.setCompanyId(event.getCompanyId());
        
        response.setDescription(event.getDescription());
        response.setNotes(event.getNotes());
        response.setCreatedAt(event.getCreatedAt());
        
        return response;
    }
}
