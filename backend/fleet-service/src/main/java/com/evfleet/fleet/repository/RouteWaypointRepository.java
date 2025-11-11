package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.RouteWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteWaypointRepository extends JpaRepository<RouteWaypoint, Long> {

    // Find by route plan
    List<RouteWaypoint> findByRoutePlanIdOrderBySequenceNumber(Long routePlanId);

    // Find by status
    List<RouteWaypoint> findByStatus(String status);

    // Find by service type
    List<RouteWaypoint> findByServiceType(String serviceType);

    // Find pending waypoints for a route
    @Query("SELECT w FROM RouteWaypoint w WHERE w.routePlan.id = :routePlanId AND w.status = 'PENDING' " +
           "ORDER BY w.sequenceNumber")
    List<RouteWaypoint> findPendingWaypointsByRoute(@Param("routePlanId") Long routePlanId);

    // Find completed waypoints for a route
    @Query("SELECT w FROM RouteWaypoint w WHERE w.routePlan.id = :routePlanId AND w.status = 'COMPLETED' " +
           "ORDER BY w.sequenceNumber")
    List<RouteWaypoint> findCompletedWaypointsByRoute(@Param("routePlanId") Long routePlanId);

    // Find waypoints with POD
    @Query("SELECT w FROM RouteWaypoint w WHERE w.podSignaturePath IS NOT NULL OR w.podPhotoPath IS NOT NULL")
    List<RouteWaypoint> findWaypointsWithPOD();

    // Find waypoints without POD
    @Query("SELECT w FROM RouteWaypoint w WHERE w.status = 'COMPLETED' " +
           "AND w.podSignaturePath IS NULL AND w.podPhotoPath IS NULL")
    List<RouteWaypoint> findCompletedWaypointsWithoutPOD();

    // Count waypoints by route and status
    @Query("SELECT COUNT(w) FROM RouteWaypoint w WHERE w.routePlan.id = :routePlanId AND w.status = :status")
    Long countByRoutePlanAndStatus(@Param("routePlanId") Long routePlanId, @Param("status") String status);

    // Find next pending waypoint
    @Query("SELECT w FROM RouteWaypoint w WHERE w.routePlan.id = :routePlanId AND w.status = 'PENDING' " +
           "ORDER BY w.sequenceNumber LIMIT 1")
    RouteWaypoint findNextPendingWaypoint(@Param("routePlanId") Long routePlanId);
}
