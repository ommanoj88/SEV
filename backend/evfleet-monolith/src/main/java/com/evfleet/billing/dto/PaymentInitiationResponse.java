package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for payment initiation
 * Contains all details needed for frontend Razorpay checkout
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiationResponse {

    /**
     * Invoice ID
     */
    private Long invoiceId;

    /**
     * Invoice number for display
     */
    private String invoiceNumber;

    /**
     * Razorpay order ID
     */
    private String orderId;

    /**
     * Razorpay key ID for checkout
     */
    private String keyId;

    /**
     * Payment amount in rupees
     */
    private BigDecimal amount;

    /**
     * Payment amount in paise (smallest currency unit)
     */
    private Long amountInPaise;

    /**
     * Currency code (INR)
     */
    private String currency;

    /**
     * Remaining invoice amount before this payment
     */
    private BigDecimal remainingAmount;

    /**
     * Whether this is a partial payment
     */
    private boolean isPartialPayment;

    /**
     * Razorpay checkout URL
     */
    private String checkoutUrl;

    /**
     * Prefill data for checkout form
     */
    private Map<String, String> prefill;

    /**
     * Order expiry time
     */
    private LocalDateTime expiresAt;

    /**
     * Status message
     */
    @Builder.Default
    private String status = "READY";

    /**
     * Additional message for frontend
     */
    private String message;
}
