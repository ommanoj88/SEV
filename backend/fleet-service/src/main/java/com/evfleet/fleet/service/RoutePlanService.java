package com.evfleet.fleet.service;

import com.evfleet.fleet.model.RoutePlan;
import com.evfleet.fleet.model.RouteWaypoint;
import com.evfleet.fleet.repository.RoutePlanRepository;
import com.evfleet.fleet.repository.RouteWaypointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutePlanService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteWaypointRepository waypointRepository;

    @Transactional
    public RoutePlan createRoutePlan(RoutePlan routePlan) {
        // Set default values if not provided
        if (routePlan.getStatus() == null) {
            routePlan.setStatus("PLANNED");
        }
        if (routePlan.getTrafficConsidered() == null) {
            routePlan.setTrafficConsidered(true);
        }
        if (routePlan.getTollRoadsAllowed() == null) {
            routePlan.setTollRoadsAllowed(true);
        }

        RoutePlan saved = routePlanRepository.save(routePlan);
        log.info("Route plan created: {} with {} waypoints", saved.getId(), saved.getWaypoints().size());
        return saved;
    }

    @Transactional(readOnly = true)
    public RoutePlan getRoutePlanById(Long id) {
        return routePlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route plan not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getAllRoutePlans() {
        return routePlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getRoutePlansByVehicle(Long vehicleId) {
        return routePlanRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getRoutePlansByDriver(Long driverId) {
        return routePlanRepository.findByDriverId(driverId);
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getRoutePlansByStatus(String status) {
        return routePlanRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getActiveRoutes() {
        return routePlanRepository.findActiveRoutes();
    }

    @Transactional(readOnly = true)
    public List<RoutePlan> getTodaysRoutes() {
        return routePlanRepository.findTodaysRoutes();
    }

    @Transactional
    public RoutePlan updateRoutePlan(Long id, RoutePlan routePlan) {
        RoutePlan existing = getRoutePlanById(id);
        
        // Update fields
        if (routePlan.getRouteName() != null) {
            existing.setRouteName(routePlan.getRouteName());
        }
        if (routePlan.getRouteDescription() != null) {
            existing.setRouteDescription(routePlan.getRouteDescription());
        }
        if (routePlan.getVehicleId() != null) {
            existing.setVehicleId(routePlan.getVehicleId());
        }
        if (routePlan.getDriverId() != null) {
            existing.setDriverId(routePlan.getDriverId());
        }
        if (routePlan.getStatus() != null) {
            existing.setStatus(routePlan.getStatus());
        }
        if (routePlan.getPlannedStartTime() != null) {
            existing.setPlannedStartTime(routePlan.getPlannedStartTime());
        }
        if (routePlan.getPlannedEndTime() != null) {
            existing.setPlannedEndTime(routePlan.getPlannedEndTime());
        }

        RoutePlan updated = routePlanRepository.save(existing);
        log.info("Route plan updated: {}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteRoutePlan(Long id) {
        RoutePlan routePlan = getRoutePlanById(id);
        routePlanRepository.delete(routePlan);
        log.info("Route plan deleted: {}", id);
    }

    @Transactional
    public RouteWaypoint addWaypoint(Long routePlanId, RouteWaypoint waypoint) {
        RoutePlan routePlan = getRoutePlanById(routePlanId);
        waypoint.setRoutePlan(routePlan);
        
        // Auto-assign sequence number if not provided
        if (waypoint.getSequenceNumber() == null) {
            List<RouteWaypoint> existingWaypoints = waypointRepository.findByRoutePlanIdOrderBySequenceNumber(routePlanId);
            waypoint.setSequenceNumber(existingWaypoints.size() + 1);
        }

        RouteWaypoint saved = waypointRepository.save(waypoint);
        log.info("Waypoint added to route plan {}: {} at sequence {}", routePlanId, saved.getId(), saved.getSequenceNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RouteWaypoint> getWaypoints(Long routePlanId) {
        return waypointRepository.findByRoutePlanIdOrderBySequenceNumber(routePlanId);
    }

    @Transactional
    public RouteWaypoint updateWaypoint(Long waypointId, RouteWaypoint waypoint) {
        RouteWaypoint existing = waypointRepository.findById(waypointId)
                .orElseThrow(() -> new RuntimeException("Waypoint not found with id: " + waypointId));

        // Update fields
        if (waypoint.getWaypointName() != null) {
            existing.setWaypointName(waypoint.getWaypointName());
        }
        if (waypoint.getStatus() != null) {
            existing.setStatus(waypoint.getStatus());
        }
        if (waypoint.getActualArrivalTime() != null) {
            existing.setActualArrivalTime(waypoint.getActualArrivalTime());
        }
        if (waypoint.getPodSignaturePath() != null) {
            existing.setPodSignaturePath(waypoint.getPodSignaturePath());
        }
        if (waypoint.getPodPhotoPath() != null) {
            existing.setPodPhotoPath(waypoint.getPodPhotoPath());
        }
        if (waypoint.getPodNotes() != null) {
            existing.setPodNotes(waypoint.getPodNotes());
        }
        if (waypoint.getCompletionNotes() != null) {
            existing.setCompletionNotes(waypoint.getCompletionNotes());
        }

        RouteWaypoint updated = waypointRepository.save(existing);
        log.info("Waypoint updated: {}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteWaypoint(Long waypointId) {
        waypointRepository.deleteById(waypointId);
        log.info("Waypoint deleted: {}", waypointId);
    }

    @Transactional
    public RoutePlan startRoute(Long routePlanId) {
        RoutePlan routePlan = getRoutePlanById(routePlanId);
        routePlan.setStatus("IN_PROGRESS");
        RoutePlan updated = routePlanRepository.save(routePlan);
        log.info("Route plan started: {}", routePlanId);
        return updated;
    }

    @Transactional
    public RoutePlan completeRoute(Long routePlanId, BigDecimal actualDistance, Integer actualDuration, 
                                    BigDecimal actualFuelConsumption, BigDecimal actualCost) {
        RoutePlan routePlan = getRoutePlanById(routePlanId);
        routePlan.setStatus("COMPLETED");
        routePlan.setActualDistance(actualDistance);
        routePlan.setActualDuration(actualDuration);
        routePlan.setActualFuelConsumption(actualFuelConsumption);
        routePlan.setActualCost(actualCost);
        
        RoutePlan updated = routePlanRepository.save(routePlan);
        log.info("Route plan completed: {} - Distance: {} km, Duration: {} min", 
                 routePlanId, actualDistance, actualDuration);
        return updated;
    }

    @Transactional
    public RoutePlan cancelRoute(Long routePlanId) {
        RoutePlan routePlan = getRoutePlanById(routePlanId);
        routePlan.setStatus("CANCELLED");
        RoutePlan updated = routePlanRepository.save(routePlan);
        log.info("Route plan cancelled: {}", routePlanId);
        return updated;
    }
}
