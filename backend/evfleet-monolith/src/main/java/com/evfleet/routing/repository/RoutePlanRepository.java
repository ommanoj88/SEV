package com.evfleet.routing.repository;

import com.evfleet.routing.model.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RoutePlan entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {

    /**
     * Find all routes for a company
     */
    List<RoutePlan> findByCompanyId(Long companyId);

    /**
     * Find routes by vehicle
     */
    List<RoutePlan> findByVehicleId(Long vehicleId);

    /**
     * Find routes by driver
     */
    List<RoutePlan> findByDriverId(Long driverId);

    /**
     * Find routes by status
     */
    List<RoutePlan> findByStatus(RoutePlan.RouteStatus status);

    /**
     * Find routes by company and status
     */
    List<RoutePlan> findByCompanyIdAndStatus(Long companyId, RoutePlan.RouteStatus status);

    /**
     * Find active routes (scheduled or in progress)
     */
    @Query("SELECT r FROM RoutePlan r WHERE r.companyId = :companyId AND r.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<RoutePlan> findActiveRoutesByCompany(Long companyId);
}
