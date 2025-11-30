package com.evfleet.telematics.model;

import com.evfleet.common.entity.BaseEntity;
import com.evfleet.fleet.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TelemetryAlert Entity
 * Represents an alert generated from telemetry data analysis.
 * 
 * Supports deduplication - same alert type for same vehicle won't be repeated
 * within the cooldown period (default 30 minutes).
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "telemetry_alerts", indexes = {
    @Index(name = "idx_alert_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_alert_company", columnList = "company_id"),
    @Index(name = "idx_alert_type", columnList = "alert_type"),
    @Index(name = "idx_alert_priority", columnList = "priority"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_created", columnList = "created_at"),
    @Index(name = "idx_alert_dedup", columnList = "vehicle_id, alert_type, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "driver_id")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    // ===== CONTEXT DATA =====

    @Column(name = "current_value")
    private Double currentValue;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "speed")
    private Double speed;

    // ===== TIMESTAMPS =====

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    // ===== NOTIFICATION TRACKING =====

    @Column(name = "notification_sent")
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    // ===== ENUMS =====

    public enum AlertType {
        // Battery Alerts
        LOW_BATTERY,           // < 20%
        CRITICAL_BATTERY,      // < 10%
        BATTERY_TEMPERATURE,   // Temperature too high/low
        
        // Speed Alerts
        EXCESSIVE_SPEED,       // Over speed limit
        SUDDEN_ACCELERATION,   // Rapid acceleration event
        HARSH_BRAKING,         // Sudden braking event
        
        // Location Alerts
        GEOFENCE_ENTRY,        // Entered restricted area
        GEOFENCE_EXIT,         // Left designated area
        ROUTE_DEVIATION,       // Off planned route
        
        // Connectivity Alerts
        CONNECTION_LOST,       // No update in threshold time
        CONNECTION_RESTORED,   // Connection back after lost
        GPS_SIGNAL_LOST,       // GPS not valid
        
        // Vehicle Health
        ENGINE_WARNING,        // Check engine light
        MAINTENANCE_DUE,       // Scheduled maintenance
        TIRE_PRESSURE,         // Abnormal tire pressure
        
        // Fuel/Charging
        LOW_FUEL,              // Low fuel level (ICE/Hybrid)
        CHARGING_COMPLETE,     // EV fully charged
        CHARGING_INTERRUPTED,  // Charging stopped unexpectedly
        
        // General
        CUSTOM                 // User-defined alert
    }

    public enum AlertPriority {
        LOW,      // Informational, no immediate action needed
        MEDIUM,   // Attention required, not urgent
        HIGH,     // Urgent, requires prompt action
        CRITICAL  // Emergency, requires immediate action
    }

    public enum AlertStatus {
        ACTIVE,       // Alert is active, not yet addressed
        ACKNOWLEDGED, // Someone has seen the alert
        RESOLVED,     // Issue has been resolved
        EXPIRED,      // Alert auto-expired (for time-limited alerts)
        SUPPRESSED    // Alert was intentionally suppressed
    }

    // ===== HELPER METHODS =====

    public void acknowledge(Long userId) {
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = userId;
    }

    public void resolve(Long userId, String notes) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = userId;
        this.resolutionNotes = notes;
    }

    public void markNotificationSent() {
        this.notificationSent = true;
        this.notificationSentAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == AlertStatus.ACTIVE;
    }

    public boolean needsAttention() {
        return this.status == AlertStatus.ACTIVE && 
               (this.priority == AlertPriority.HIGH || this.priority == AlertPriority.CRITICAL);
    }
}
