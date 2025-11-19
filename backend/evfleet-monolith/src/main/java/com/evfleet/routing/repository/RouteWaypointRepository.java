package com.evfleet.routing.repository;

import com.evfleet.routing.model.RouteWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RouteWaypoint entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface RouteWaypointRepository extends JpaRepository<RouteWaypoint, Long> {

    /**
     * Find all waypoints for a route
     */
    List<RouteWaypoint> findByRoutePlanIdOrderBySequenceAsc(Long routePlanId);

    /**
     * Find waypoints by status
     */
    List<RouteWaypoint> findByRoutePlanIdAndStatus(Long routePlanId, RouteWaypoint.WaypointStatus status);
}
