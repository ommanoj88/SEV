package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.GeofenceRequest;
import com.evfleet.fleet.dto.GeofenceResponse;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Geofence;
import com.evfleet.fleet.repository.GeofenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling geofence operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new geofence
     */
    public GeofenceResponse createGeofence(GeofenceRequest request) {
        log.info("Creating new geofence: {}", request.getName());

        Geofence geofence = new Geofence();
        mapRequestToEntity(request, geofence);

        // Calculate center coordinates if not provided
        if (geofence.getCenterLatitude() == null || geofence.getCenterLongitude() == null) {
            calculateCenter(geofence);
        }

        Geofence savedGeofence = geofenceRepository.save(geofence);
        log.info("Geofence created successfully with ID: {}", savedGeofence.getId());

        return GeofenceResponse.fromEntity(savedGeofence);
    }

    /**
     * Update an existing geofence
     */
    public GeofenceResponse updateGeofence(Long id, GeofenceRequest request) {
        log.info("Updating geofence with ID: {}", id);

        Geofence geofence = geofenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with ID: " + id));

        mapRequestToEntity(request, geofence);

        // Recalculate center if coordinates changed
        calculateCenter(geofence);

        Geofence updatedGeofence = geofenceRepository.save(geofence);
        log.info("Geofence updated successfully with ID: {}", updatedGeofence.getId());

        return GeofenceResponse.fromEntity(updatedGeofence);
    }

    /**
     * Delete a geofence
     */
    public void deleteGeofence(Long id) {
        log.info("Deleting geofence with ID: {}", id);

        Geofence geofence = geofenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with ID: " + id));

        geofenceRepository.delete(geofence);
        log.info("Geofence deleted successfully with ID: {}", id);
    }

    /**
     * Get geofence by ID
     */
    @Transactional(readOnly = true)
    public GeofenceResponse getGeofenceById(Long id) {
        log.debug("Fetching geofence with ID: {}", id);

        Geofence geofence = geofenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with ID: " + id));

        return GeofenceResponse.fromEntity(geofence);
    }

    /**
     * Get all geofences by company
     */
    @Transactional(readOnly = true)
    public List<GeofenceResponse> getGeofencesByCompany(Long companyId) {
        log.debug("Fetching geofences for company ID: {}", companyId);

        return geofenceRepository.findByCompanyId(companyId).stream()
                .map(GeofenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get active geofences by company
     */
    @Transactional(readOnly = true)
    public List<GeofenceResponse> getActiveGeofences(Long companyId) {
        log.debug("Fetching active geofences for company ID: {}", companyId);

        return geofenceRepository.findByCompanyIdAndActive(companyId, true).stream()
                .map(GeofenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get geofences by type
     */
    @Transactional(readOnly = true)
    public List<GeofenceResponse> getGeofencesByType(Long companyId, Geofence.GeofenceType type) {
        log.debug("Fetching geofences of type {} for company ID: {}", type, companyId);

        return geofenceRepository.findByCompanyIdAndType(companyId, type).stream()
                .map(GeofenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Activate or deactivate a geofence
     */
    public void toggleGeofence(Long id, Boolean active) {
        log.info("Toggling geofence ID: {} to active: {}", id, active);

        Geofence geofence = geofenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with ID: " + id));

        geofence.setActive(active);
        geofenceRepository.save(geofence);

        log.info("Geofence toggled successfully");
    }

    /**
     * Check if a point is inside a geofence
     */
    @Transactional(readOnly = true)
    public boolean isPointInGeofence(Long geofenceId, Double latitude, Double longitude) {
        Geofence geofence = geofenceRepository.findById(geofenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Geofence not found with ID: " + geofenceId));

        if (!geofence.getActive()) {
            return false;
        }

        if (geofence.getShape() == Geofence.GeofenceShape.CIRCLE) {
            return isPointInCircle(latitude, longitude, geofence);
        } else if (geofence.getShape() == Geofence.GeofenceShape.POLYGON) {
            return isPointInPolygon(latitude, longitude, geofence);
        }

        return false;
    }

    /**
     * Get geofences containing a specific point
     */
    @Transactional(readOnly = true)
    public List<GeofenceResponse> getGeofencesContainingPoint(Long companyId, Double latitude, Double longitude) {
        log.debug("Finding geofences containing point ({}, {}) for company ID: {}", latitude, longitude, companyId);

        List<Geofence> activeGeofences = geofenceRepository.findByCompanyIdAndActive(companyId, true);

        return activeGeofences.stream()
                .filter(geofence -> {
                    if (geofence.getShape() == Geofence.GeofenceShape.CIRCLE) {
                        return isPointInCircle(latitude, longitude, geofence);
                    } else if (geofence.getShape() == Geofence.GeofenceShape.POLYGON) {
                        return isPointInPolygon(latitude, longitude, geofence);
                    }
                    return false;
                })
                .map(GeofenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Map request to entity
     */
    private void mapRequestToEntity(GeofenceRequest request, Geofence geofence) {
        geofence.setCompanyId(request.getCompanyId());
        geofence.setName(request.getName());
        geofence.setType(request.getType());
        geofence.setCoordinates(request.getCoordinates());
        geofence.setRadius(request.getRadius());
        geofence.setActive(request.getActive());
        geofence.setDescription(request.getDescription());
        geofence.setShape(request.getShape());
        geofence.setColor(request.getColor());
        geofence.setAlertOnEntry(request.getAlertOnEntry());
        geofence.setAlertOnExit(request.getAlertOnExit());
        geofence.setSpeedLimit(request.getSpeedLimit());
        geofence.setAllowedVehicles(request.getAllowedVehicles());
        geofence.setRestrictedVehicles(request.getRestrictedVehicles());
        geofence.setScheduleStartTime(request.getScheduleStartTime());
        geofence.setScheduleEndTime(request.getScheduleEndTime());
        geofence.setScheduleDays(request.getScheduleDays());
    }

    /**
     * Calculate center coordinates from geofence coordinates
     */
    private void calculateCenter(Geofence geofence) {
        try {
            JsonNode coordinates = objectMapper.readTree(geofence.getCoordinates());

            if (geofence.getShape() == Geofence.GeofenceShape.CIRCLE) {
                // For circle, coordinates should be a single point
                geofence.setCenterLatitude(coordinates.get("lat").asDouble());
                geofence.setCenterLongitude(coordinates.get("lng").asDouble());
            } else if (geofence.getShape() == Geofence.GeofenceShape.POLYGON) {
                // For polygon, calculate centroid
                double totalLat = 0;
                double totalLng = 0;
                int count = 0;

                if (coordinates.isArray()) {
                    for (JsonNode point : coordinates) {
                        totalLat += point.get("lat").asDouble();
                        totalLng += point.get("lng").asDouble();
                        count++;
                    }
                }

                if (count > 0) {
                    geofence.setCenterLatitude(totalLat / count);
                    geofence.setCenterLongitude(totalLng / count);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing geofence coordinates", e);
        }
    }

    /**
     * Check if point is inside a circular geofence
     */
    private boolean isPointInCircle(Double latitude, Double longitude, Geofence geofence) {
        if (geofence.getCenterLatitude() == null || geofence.getCenterLongitude() == null || geofence.getRadius() == null) {
            return false;
        }

        double distance = calculateDistance(
                latitude, longitude,
                geofence.getCenterLatitude(), geofence.getCenterLongitude()
        );

        return distance <= geofence.getRadius();
    }

    /**
     * Check if point is inside a polygon geofence using ray-casting algorithm
     */
    private boolean isPointInPolygon(Double latitude, Double longitude, Geofence geofence) {
        try {
            JsonNode coordinates = objectMapper.readTree(geofence.getCoordinates());

            if (!coordinates.isArray() || coordinates.size() < 3) {
                return false;
            }

            int intersections = 0;
            int n = coordinates.size();

            for (int i = 0; i < n; i++) {
                JsonNode p1 = coordinates.get(i);
                JsonNode p2 = coordinates.get((i + 1) % n);

                double lat1 = p1.get("lat").asDouble();
                double lng1 = p1.get("lng").asDouble();
                double lat2 = p2.get("lat").asDouble();
                double lng2 = p2.get("lng").asDouble();

                if (lng1 == lng2) continue;

                if (longitude < Math.min(lng1, lng2) || longitude >= Math.max(lng1, lng2)) continue;

                double intersectLat = lat1 + (longitude - lng1) * (lat2 - lat1) / (lng2 - lng1);

                if (latitude < intersectLat) {
                    intersections++;
                }
            }

            return (intersections % 2) == 1;
        } catch (JsonProcessingException e) {
            log.error("Error parsing polygon coordinates", e);
            return false;
        }
    }

    /**
     * Calculate distance between two points using Haversine formula (in meters)
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
