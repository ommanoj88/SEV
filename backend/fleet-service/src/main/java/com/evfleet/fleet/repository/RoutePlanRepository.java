package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {

    // Find by vehicle
    List<RoutePlan> findByVehicleId(Long vehicleId);

    // Find by driver
    List<RoutePlan> findByDriverId(Long driverId);

    // Find by status
    List<RoutePlan> findByStatus(String status);

    // Find by vehicle and status
    List<RoutePlan> findByVehicleIdAndStatus(Long vehicleId, String status);

    // Find by driver and status
    List<RoutePlan> findByDriverIdAndStatus(Long driverId, String status);

    // Find active routes (PLANNED or IN_PROGRESS)
    @Query("SELECT r FROM RoutePlan r WHERE r.status IN ('PLANNED', 'IN_PROGRESS')")
    List<RoutePlan> findActiveRoutes();

    // Find routes by date range
    List<RoutePlan> findByPlannedStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find routes for today
    @Query("SELECT r FROM RoutePlan r WHERE DATE(r.plannedStartTime) = CURRENT_DATE")
    List<RoutePlan> findTodaysRoutes();

    // Find routes by optimization criteria
    List<RoutePlan> findByOptimizationCriteria(String criteria);

    // Find completed routes
    @Query("SELECT r FROM RoutePlan r WHERE r.status = 'COMPLETED' ORDER BY r.updatedAt DESC")
    List<RoutePlan> findCompletedRoutes();

    // Find routes needing optimization
    @Query("SELECT r FROM RoutePlan r WHERE r.status = 'PLANNED' AND r.totalDistance IS NULL")
    List<RoutePlan> findRoutesNeedingOptimization();

    // Count routes by status
    @Query("SELECT COUNT(r) FROM RoutePlan r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);

    // Find routes by vehicle and date range
    @Query("SELECT r FROM RoutePlan r WHERE r.vehicleId = :vehicleId " +
           "AND r.plannedStartTime BETWEEN :startDate AND :endDate")
    List<RoutePlan> findByVehicleAndDateRange(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
