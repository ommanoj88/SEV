package com.evfleet.geofencing.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.geofencing.dto.GeofenceRequest;
import com.evfleet.geofencing.model.Geofence;
import com.evfleet.geofencing.repository.GeofenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Geofencing operations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;

    /**
     * Get all geofences for a company
     */
    public List<Geofence> getGeofencesByCompany(Long companyId) {
        log.info("Getting geofences for company: {}", companyId);
        return geofenceRepository.findByCompanyId(companyId);
    }

    /**
     * Get active geofences for a company
     */
    public List<Geofence> getActiveGeofencesByCompany(Long companyId) {
        log.info("Getting active geofences for company: {}", companyId);
        return geofenceRepository.findByCompanyIdAndIsActiveTrue(companyId);
    }

    /**
     * Get geofence by ID
     */
    public Geofence getGeofenceById(Long id) {
        log.info("Getting geofence by ID: {}", id);
        return geofenceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with id: " + id));
    }

    /**
     * Create a new geofence
     */
    @Transactional
    public Geofence createGeofence(GeofenceRequest request) {
        log.info("Creating new geofence: {}", request.getName());

        Geofence geofence = Geofence.builder()
            .companyId(request.getCompanyId())
            .name(request.getName())
            .geofenceType(request.getGeofenceType())
            .description(request.getDescription())
            .centerLatitude(request.getCenterLatitude())
            .centerLongitude(request.getCenterLongitude())
            .radius(request.getRadius())
            .speedLimit(request.getSpeedLimit())
            .alertOnEntry(request.getAlertOnEntry() != null ? request.getAlertOnEntry() : false)
            .alertOnExit(request.getAlertOnExit() != null ? request.getAlertOnExit() : false)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .color(request.getColor())
            .build();

        return geofenceRepository.save(geofence);
    }

    /**
     * Update an existing geofence
     */
    @Transactional
    public Geofence updateGeofence(Long id, GeofenceRequest request) {
        log.info("Updating geofence: {}", id);

        Geofence geofence = getGeofenceById(id);
        geofence.setName(request.getName());
        geofence.setGeofenceType(request.getGeofenceType());
        geofence.setDescription(request.getDescription());
        geofence.setCenterLatitude(request.getCenterLatitude());
        geofence.setCenterLongitude(request.getCenterLongitude());
        geofence.setRadius(request.getRadius());
        geofence.setSpeedLimit(request.getSpeedLimit());
        geofence.setAlertOnEntry(request.getAlertOnEntry());
        geofence.setAlertOnExit(request.getAlertOnExit());
        geofence.setIsActive(request.getIsActive());
        geofence.setColor(request.getColor());

        return geofenceRepository.save(geofence);
    }

    /**
     * Delete a geofence
     */
    @Transactional
    public void deleteGeofence(Long id) {
        log.info("Deleting geofence: {}", id);
        Geofence geofence = getGeofenceById(id);
        geofenceRepository.delete(geofence);
    }

    /**
     * Check if a vehicle location is within any geofence
     */
    public List<Geofence> checkVehicleInGeofences(Long companyId, double latitude, double longitude) {
        log.debug("Checking if location ({}, {}) is within any geofence", latitude, longitude);
        
        List<Geofence> activeGeofences = getActiveGeofencesByCompany(companyId);
        return activeGeofences.stream()
            .filter(geofence -> geofence.containsPoint(latitude, longitude))
            .toList();
    }
}
