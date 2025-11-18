package com.evfleet.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscription Request DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {

    @NotBlank(message = "Plan type is required")
    private String tier;  // BASIC, PRO, ENTERPRISE

    @NotBlank(message = "Billing cycle is required")
    private String cycle;  // MONTHLY, QUARTERLY, YEARLY

    @NotNull(message = "Vehicle count is required")
    @Positive(message = "Vehicle count must be positive")
    private Integer vehicleCount;
}
