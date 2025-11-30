package com.evfleet.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for initiating a refund
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayRefundRequest {
    
    /**
     * Razorpay Payment ID to refund (pay_xxx format)
     */
    @NotBlank(message = "Payment ID is required")
    private String paymentId;
    
    /**
     * Amount to refund in rupees (optional, full refund if not specified)
     */
    @Positive(message = "Refund amount must be positive")
    private BigDecimal amount;
    
    /**
     * Reason for refund
     */
    private String reason;
    
    /**
     * Whether to use instant refund (additional charges apply)
     */
    @Builder.Default
    private boolean instantRefund = false;
    
    /**
     * Additional notes for the refund
     */
    private String notes;
}
