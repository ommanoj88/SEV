package com.evfleet.telematics.repository;

import com.evfleet.telematics.model.DrivingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for DrivingEvent entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface DrivingEventRepository extends JpaRepository<DrivingEvent, Long> {

    List<DrivingEvent> findByTripId(Long tripId);
    
    List<DrivingEvent> findByDriverId(Long driverId);
    
    List<DrivingEvent> findByVehicleId(Long vehicleId);
    
    List<DrivingEvent> findByCompanyId(Long companyId);
    
    List<DrivingEvent> findByEventType(DrivingEvent.EventType eventType);
    
    List<DrivingEvent> findByDriverIdAndEventTimeBetween(Long driverId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT e FROM DrivingEvent e WHERE e.tripId = :tripId AND e.eventType = :eventType")
    List<DrivingEvent> findByTripIdAndEventType(@Param("tripId") Long tripId, @Param("eventType") DrivingEvent.EventType eventType);
    
    @Query("SELECT COUNT(e) FROM DrivingEvent e WHERE e.driverId = :driverId AND e.eventType = :eventType AND e.eventTime BETWEEN :start AND :end")
    long countByDriverIdAndEventTypeAndEventTimeBetween(
        @Param("driverId") Long driverId, 
        @Param("eventType") DrivingEvent.EventType eventType,
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
}
