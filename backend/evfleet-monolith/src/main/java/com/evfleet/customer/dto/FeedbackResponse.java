package com.evfleet.customer.dto;

import com.evfleet.customer.model.CustomerFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for customer feedback
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private Long customerId;
    private Long tripId;
    private Long driverId;
    private Long vehicleId;
    private Integer rating;
    private String comment;
    private CustomerFeedback.FeedbackType feedbackType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static FeedbackResponse from(CustomerFeedback feedback) {
        return FeedbackResponse.builder()
            .id(feedback.getId())
            .customerId(feedback.getCustomerId())
            .tripId(feedback.getTripId())
            .driverId(feedback.getDriverId())
            .vehicleId(feedback.getVehicleId())
            .rating(feedback.getRating())
            .comment(feedback.getComment())
            .feedbackType(feedback.getFeedbackType())
            .createdAt(feedback.getCreatedAt())
            .updatedAt(feedback.getUpdatedAt())
            .build();
    }
}
