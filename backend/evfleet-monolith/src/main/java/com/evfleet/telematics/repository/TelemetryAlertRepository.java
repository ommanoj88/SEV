package com.evfleet.telematics.repository;

import com.evfleet.telematics.model.TelemetryAlert;
import com.evfleet.telematics.model.TelemetryAlert.AlertPriority;
import com.evfleet.telematics.model.TelemetryAlert.AlertStatus;
import com.evfleet.telematics.model.TelemetryAlert.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TelemetryAlert entity.
 * Provides queries for alert management and deduplication.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface TelemetryAlertRepository extends JpaRepository<TelemetryAlert, Long> {

    // ===== BASIC QUERIES =====

    /**
     * Find all alerts for a vehicle
     */
    List<TelemetryAlert> findByVehicleIdOrderByTriggeredAtDesc(Long vehicleId);

    /**
     * Find all alerts for a company
     */
    Page<TelemetryAlert> findByCompanyIdOrderByTriggeredAtDesc(Long companyId, Pageable pageable);

    /**
     * Find alerts by status
     */
    List<TelemetryAlert> findByCompanyIdAndStatusOrderByPriorityDescTriggeredAtDesc(
        Long companyId, 
        AlertStatus status
    );

    /**
     * Find active alerts for a vehicle
     */
    List<TelemetryAlert> findByVehicleIdAndStatusOrderByPriorityDescTriggeredAtDesc(
        Long vehicleId, 
        AlertStatus status
    );

    // ===== DEDUPLICATION QUERIES =====

    /**
     * Find most recent alert of a specific type for a vehicle
     * Used for deduplication check
     */
    Optional<TelemetryAlert> findTopByVehicleIdAndAlertTypeAndStatusOrderByTriggeredAtDesc(
        Long vehicleId, 
        AlertType alertType,
        AlertStatus status
    );

    /**
     * Check if an active alert of same type exists within cooldown period
     */
    @Query("""
        SELECT COUNT(a) > 0 FROM TelemetryAlert a 
        WHERE a.vehicleId = :vehicleId 
        AND a.alertType = :alertType 
        AND a.status IN ('ACTIVE', 'ACKNOWLEDGED')
        AND a.triggeredAt > :cooldownStart
        """)
    boolean existsRecentAlert(
        @Param("vehicleId") Long vehicleId,
        @Param("alertType") AlertType alertType,
        @Param("cooldownStart") LocalDateTime cooldownStart
    );

    // ===== PRIORITY-BASED QUERIES =====

    /**
     * Find all critical and high priority active alerts for a company
     */
    @Query("""
        SELECT a FROM TelemetryAlert a 
        WHERE a.companyId = :companyId 
        AND a.status = 'ACTIVE'
        AND a.priority IN ('CRITICAL', 'HIGH')
        ORDER BY a.priority DESC, a.triggeredAt DESC
        """)
    List<TelemetryAlert> findUrgentAlerts(@Param("companyId") Long companyId);

    /**
     * Count active alerts by priority for a company
     */
    long countByCompanyIdAndStatusAndPriority(Long companyId, AlertStatus status, AlertPriority priority);

    // ===== TIME-BASED QUERIES =====

    /**
     * Find alerts triggered within a time range
     */
    List<TelemetryAlert> findByCompanyIdAndTriggeredAtBetweenOrderByTriggeredAtDesc(
        Long companyId, 
        LocalDateTime start, 
        LocalDateTime end
    );

    /**
     * Find alerts that need notification to be sent
     */
    @Query("""
        SELECT a FROM TelemetryAlert a 
        WHERE a.notificationSent = false 
        AND a.status = 'ACTIVE'
        ORDER BY a.priority DESC, a.triggeredAt ASC
        """)
    List<TelemetryAlert> findPendingNotifications();

    // ===== BULK OPERATIONS =====

    /**
     * Bulk acknowledge alerts for a vehicle
     */
    @Modifying
    @Query("""
        UPDATE TelemetryAlert a 
        SET a.status = 'ACKNOWLEDGED', 
            a.acknowledgedAt = :now, 
            a.acknowledgedBy = :userId 
        WHERE a.vehicleId = :vehicleId 
        AND a.status = 'ACTIVE'
        """)
    int acknowledgeAllForVehicle(
        @Param("vehicleId") Long vehicleId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Expire old unresolved alerts (auto-cleanup)
     */
    @Modifying
    @Query("""
        UPDATE TelemetryAlert a 
        SET a.status = 'EXPIRED' 
        WHERE a.status = 'ACTIVE' 
        AND a.triggeredAt < :expireThreshold
        """)
    int expireOldAlerts(@Param("expireThreshold") LocalDateTime expireThreshold);

    // ===== STATISTICS QUERIES =====

    /**
     * Count alerts by type for a vehicle in a time range
     */
    @Query("""
        SELECT a.alertType, COUNT(a) 
        FROM TelemetryAlert a 
        WHERE a.vehicleId = :vehicleId 
        AND a.triggeredAt BETWEEN :start AND :end 
        GROUP BY a.alertType
        """)
    List<Object[]> countByTypeForVehicle(
        @Param("vehicleId") Long vehicleId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Count total active alerts for a company
     */
    long countByCompanyIdAndStatus(Long companyId, AlertStatus status);

    /**
     * Get alert summary by priority for a company
     */
    @Query("""
        SELECT a.priority, COUNT(a) 
        FROM TelemetryAlert a 
        WHERE a.companyId = :companyId 
        AND a.status = 'ACTIVE' 
        GROUP BY a.priority
        """)
    List<Object[]> getAlertSummaryByPriority(@Param("companyId") Long companyId);
}
