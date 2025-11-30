package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for handling payment failure callback
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailureRequest {

    /**
     * Razorpay order ID
     */
    private String razorpayOrderId;

    /**
     * Error code from Razorpay
     */
    private String errorCode;

    /**
     * Error description from Razorpay
     */
    private String errorDescription;

    /**
     * Error source (payment, bank, etc.)
     */
    private String errorSource;

    /**
     * Error reason
     */
    private String errorReason;

    /**
     * Error step at which failure occurred
     */
    private String errorStep;

    /**
     * Additional metadata
     */
    private String metadata;
}
