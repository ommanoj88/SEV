package com.evfleet.routing.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.routing.dto.RoutePlanRequest;
import com.evfleet.routing.dto.WaypointRequest;
import com.evfleet.routing.model.RoutePlan;
import com.evfleet.routing.model.RouteWaypoint;
import com.evfleet.routing.repository.RoutePlanRepository;
import com.evfleet.routing.repository.RouteWaypointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Route Planning operations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteWaypointRepository routeWaypointRepository;

    /**
     * Get all routes for a company
     */
    public List<RoutePlan> getRoutesByCompany(Long companyId) {
        log.info("Getting routes for company: {}", companyId);
        return routePlanRepository.findByCompanyId(companyId);
    }

    /**
     * Get route by ID
     */
    public RoutePlan getRouteById(Long id) {
        log.info("Getting route by ID: {}", id);
        return routePlanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
    }

    /**
     * Create a new route plan
     */
    @Transactional
    public RoutePlan createRoute(RoutePlanRequest request) {
        log.info("Creating new route: {}", request.getRouteName());

        RoutePlan route = RoutePlan.builder()
            .companyId(request.getCompanyId())
            .vehicleId(request.getVehicleId())
            .driverId(request.getDriverId())
            .routeName(request.getRouteName())
            .optimizationCriteria(request.getOptimizationCriteria())
            .status(RoutePlan.RouteStatus.DRAFT)
            .notes(request.getNotes())
            .build();

        return routePlanRepository.save(route);
    }

    /**
     * Update an existing route plan
     */
    @Transactional
    public RoutePlan updateRoute(Long id, RoutePlanRequest request) {
        log.info("Updating route: {}", id);

        RoutePlan route = getRouteById(id);
        route.setRouteName(request.getRouteName());
        route.setVehicleId(request.getVehicleId());
        route.setDriverId(request.getDriverId());
        route.setOptimizationCriteria(request.getOptimizationCriteria());
        route.setNotes(request.getNotes());

        return routePlanRepository.save(route);
    }

    /**
     * Delete a route plan
     */
    @Transactional
    public void deleteRoute(Long id) {
        log.info("Deleting route: {}", id);
        RoutePlan route = getRouteById(id);
        routePlanRepository.delete(route);
    }

    /**
     * Add a waypoint to a route
     */
    @Transactional
    public RouteWaypoint addWaypoint(Long routeId, WaypointRequest request) {
        log.info("Adding waypoint to route: {}", routeId);

        RoutePlan route = getRouteById(routeId);

        RouteWaypoint waypoint = RouteWaypoint.builder()
            .routePlan(route)
            .sequence(request.getSequence())
            .waypointType(request.getWaypointType())
            .locationName(request.getLocationName())
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .scheduledArrival(request.getScheduledArrival())
            .scheduledDeparture(request.getScheduledDeparture())
            .contactName(request.getContactName())
            .contactPhone(request.getContactPhone())
            .notes(request.getNotes())
            .status(RouteWaypoint.WaypointStatus.PENDING)
            .build();

        route.addWaypoint(waypoint);
        return routeWaypointRepository.save(waypoint);
    }

    /**
     * Start a route
     */
    @Transactional
    public RoutePlan startRoute(Long id) {
        log.info("Starting route: {}", id);

        RoutePlan route = getRouteById(id);
        route.setStatus(RoutePlan.RouteStatus.IN_PROGRESS);
        route.setStartedAt(LocalDateTime.now());

        return routePlanRepository.save(route);
    }

    /**
     * Complete a route
     */
    @Transactional
    public RoutePlan completeRoute(Long id) {
        log.info("Completing route: {}", id);

        RoutePlan route = getRouteById(id);
        route.setStatus(RoutePlan.RouteStatus.COMPLETED);
        route.setCompletedAt(LocalDateTime.now());

        return routePlanRepository.save(route);
    }

    /**
     * Cancel a route
     */
    @Transactional
    public RoutePlan cancelRoute(Long id) {
        log.info("Cancelling route: {}", id);

        RoutePlan route = getRouteById(id);
        route.setStatus(RoutePlan.RouteStatus.CANCELLED);

        return routePlanRepository.save(route);
    }

    /**
     * Get waypoints for a route
     */
    public List<RouteWaypoint> getWaypointsByRoute(Long routeId) {
        log.info("Getting waypoints for route: {}", routeId);
        return routeWaypointRepository.findByRoutePlanIdOrderBySequenceAsc(routeId);
    }
}
