package com.evfleet.customer.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Feedback Entity
 *
 * Represents feedback/rating from a customer.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "customer_feedback", indexes = {
    @Index(name = "idx_feedback_customer", columnList = "customer_id"),
    @Index(name = "idx_feedback_trip", columnList = "trip_id"),
    @Index(name = "idx_feedback_rating", columnList = "rating")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars

    @Column(name = "comment", length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", length = 20)
    private FeedbackType feedbackType;

    /**
     * Feedback types
     */
    public enum FeedbackType {
        SERVICE,         // Service quality feedback
        DRIVER,          // Driver behavior feedback
        VEHICLE,         // Vehicle condition feedback
        GENERAL          // General feedback
    }
}
