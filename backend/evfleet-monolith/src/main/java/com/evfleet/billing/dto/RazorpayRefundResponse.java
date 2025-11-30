package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for refund result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayRefundResponse {
    
    /**
     * Whether refund was successful
     */
    private boolean success;
    
    /**
     * Razorpay Refund ID (rfnd_xxx format)
     */
    private String refundId;
    
    /**
     * Razorpay Payment ID that was refunded
     */
    private String paymentId;
    
    /**
     * Amount refunded (in rupees)
     */
    private BigDecimal amount;
    
    /**
     * Refund status from Razorpay
     */
    private String status;
    
    /**
     * Result message
     */
    private String message;
    
    /**
     * Speed of refund (normal/optimum)
     */
    private String speed;
    
    /**
     * Currency of refund
     */
    private String currency;
}
