package com.evfleet.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for handling successful payment callback
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessRequest {

    /**
     * Razorpay order ID
     */
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    /**
     * Razorpay payment ID
     */
    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    /**
     * Razorpay signature for verification
     */
    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
