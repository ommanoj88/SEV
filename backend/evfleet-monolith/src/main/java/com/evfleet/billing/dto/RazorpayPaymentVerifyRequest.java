package com.evfleet.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying Razorpay payment
 * Contains the signature data received from Razorpay checkout callback
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayPaymentVerifyRequest {
    
    /**
     * Razorpay Order ID (order_xxx format)
     */
    @NotBlank(message = "Order ID is required")
    private String razorpayOrderId;
    
    /**
     * Razorpay Payment ID (pay_xxx format)
     */
    @NotBlank(message = "Payment ID is required")
    private String razorpayPaymentId;
    
    /**
     * Razorpay Signature for verification
     */
    @NotBlank(message = "Signature is required")
    private String razorpaySignature;
}
