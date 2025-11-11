package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.VehicleEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for VehicleEvent entity
 * Provides data access for vehicle event history
 */
@Repository
public interface VehicleEventRepository extends JpaRepository<VehicleEvent, Long> {

    /**
     * Find all events for a specific vehicle
     */
    Page<VehicleEvent> findByVehicleIdOrderByEventTimestampDesc(Long vehicleId, Pageable pageable);

    /**
     * Find all events for a specific vehicle and event type
     */
    Page<VehicleEvent> findByVehicleIdAndEventTypeOrderByEventTimestampDesc(
            Long vehicleId, VehicleEvent.EventType eventType, Pageable pageable);

    /**
     * Find all events for a company
     */
    Page<VehicleEvent> findByCompanyIdOrderByEventTimestampDesc(Long companyId, Pageable pageable);

    /**
     * Find events by severity
     */
    Page<VehicleEvent> findByVehicleIdAndSeverityOrderByEventTimestampDesc(
            Long vehicleId, VehicleEvent.EventSeverity severity, Pageable pageable);

    /**
     * Find events within a time range
     */
    @Query("SELECT e FROM VehicleEvent e WHERE e.vehicleId = :vehicleId " +
           "AND e.eventTimestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY e.eventTimestamp DESC")
    List<VehicleEvent> findEventsByVehicleIdAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find critical events for a vehicle
     */
    @Query("SELECT e FROM VehicleEvent e WHERE e.vehicleId = :vehicleId " +
           "AND e.severity IN ('HIGH', 'CRITICAL') " +
           "ORDER BY e.eventTimestamp DESC")
    List<VehicleEvent> findCriticalEventsByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find recent events for a vehicle (last N days)
     */
    @Query("SELECT e FROM VehicleEvent e WHERE e.vehicleId = :vehicleId " +
           "AND e.eventTimestamp >= :sinceDate " +
           "ORDER BY e.eventTimestamp DESC")
    List<VehicleEvent> findRecentEventsByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Count events by type for a vehicle
     */
    @Query("SELECT COUNT(e) FROM VehicleEvent e WHERE e.vehicleId = :vehicleId " +
           "AND e.eventType = :eventType")
    Long countEventsByVehicleIdAndEventType(
            @Param("vehicleId") Long vehicleId,
            @Param("eventType") VehicleEvent.EventType eventType);

    /**
     * Find all events by type
     */
    List<VehicleEvent> findByEventTypeOrderByEventTimestampDesc(VehicleEvent.EventType eventType);

    /**
     * Find events by multiple types
     */
    @Query("SELECT e FROM VehicleEvent e WHERE e.vehicleId = :vehicleId " +
           "AND e.eventType IN :eventTypes " +
           "ORDER BY e.eventTimestamp DESC")
    List<VehicleEvent> findByVehicleIdAndEventTypeIn(
            @Param("vehicleId") Long vehicleId,
            @Param("eventTypes") List<VehicleEvent.EventType> eventTypes);
}
