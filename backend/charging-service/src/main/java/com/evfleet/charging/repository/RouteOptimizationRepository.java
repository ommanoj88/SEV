package com.evfleet.charging.repository;

import com.evfleet.charging.entity.RouteOptimization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteOptimizationRepository extends JpaRepository<RouteOptimization, Long> {

    List<RouteOptimization> findByVehicleId(Long vehicleId);

    List<RouteOptimization> findByStatus(RouteOptimization.OptimizationStatus status);

    @Query("SELECT r FROM RouteOptimization r WHERE r.vehicleId = :vehicleId " +
           "ORDER BY r.createdAt DESC")
    List<RouteOptimization> findRecentByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT r FROM RouteOptimization r WHERE r.createdAt >= :since " +
           "ORDER BY r.createdAt DESC")
    List<RouteOptimization> findRecentRoutes(@Param("since") LocalDateTime since);
}
