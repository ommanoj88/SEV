package com.evfleet.fleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "waypoint_id")
    private Long waypointId;

    @Column(name = "route_plan_id")
    private Long routePlanId;

    // Feedback Details
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "feedback_category", length = 50)
    private String feedbackCategory; // 'DELIVERY_QUALITY', 'DRIVER_BEHAVIOR', 'TIMELINESS', 'COMMUNICATION'

    // Response
    @Column(name = "is_addressed")
    private Boolean isAddressed = false;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "responded_by")
    private String respondedBy;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    // Helper methods
    public boolean isPositiveFeedback() {
        return rating != null && rating >= 4;
    }

    public boolean isNegativeFeedback() {
        return rating != null && rating <= 2;
    }

    public boolean isNeutralFeedback() {
        return rating != null && rating == 3;
    }

    public boolean hasResponse() {
        return responseText != null && !responseText.trim().isEmpty();
    }
}
