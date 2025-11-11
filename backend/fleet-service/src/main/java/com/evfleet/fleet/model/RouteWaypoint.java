package com.evfleet.fleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_waypoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_plan_id", nullable = false)
    private RoutePlan routePlan;

    @NotNull(message = "Sequence number is required")
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "waypoint_name")
    private String waypointName;

    // Location
    @NotNull(message = "Latitude is required")
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Time Window
    @Column(name = "arrival_window_start")
    private LocalDateTime arrivalWindowStart;

    @Column(name = "arrival_window_end")
    private LocalDateTime arrivalWindowEnd;

    @Column(name = "planned_arrival_time")
    private LocalDateTime plannedArrivalTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    // Stop Details
    @Column(name = "stop_duration")
    private Integer stopDuration; // minutes

    @Column(name = "service_type", length = 100)
    private String serviceType; // 'PICKUP', 'DELIVERY', 'SERVICE', 'REST'

    // Customer Information
    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    // Delivery/Pickup Details
    @Column(name = "items_description", columnDefinition = "TEXT")
    private String itemsDescription;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal volume;

    // Proof of Delivery
    @Column(name = "pod_signature_path", length = 500)
    private String podSignaturePath;

    @Column(name = "pod_photo_path", length = 500)
    private String podPhotoPath;

    @Column(name = "pod_notes", columnDefinition = "TEXT")
    private String podNotes;

    @Column(name = "pod_timestamp")
    private LocalDateTime podTimestamp;

    @Column(name = "pod_captured_by")
    private String podCapturedBy;

    // Status
    @Column(length = 50)
    private String status = "PENDING"; // 'PENDING', 'IN_TRANSIT', 'COMPLETED', 'FAILED', 'SKIPPED'

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean hasPOD() {
        return podSignaturePath != null || podPhotoPath != null;
    }

    public boolean isWithinTimeWindow() {
        if (arrivalWindowStart == null || arrivalWindowEnd == null) {
            return true; // No time window constraint
        }
        if (actualArrivalTime == null) {
            return false; // Not yet arrived
        }
        return !actualArrivalTime.isBefore(arrivalWindowStart) && 
               !actualArrivalTime.isAfter(arrivalWindowEnd);
    }
}
