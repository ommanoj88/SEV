package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for payment verification result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayPaymentVerifyResponse {
    
    /**
     * Whether verification was successful
     */
    private boolean success;
    
    /**
     * Razorpay Order ID
     */
    private String orderId;
    
    /**
     * Razorpay Payment ID
     */
    private String paymentId;
    
    /**
     * Result message
     */
    private String message;
    
    /**
     * Amount paid (in rupees)
     */
    private BigDecimal amountPaid;
    
    /**
     * Payment method used
     */
    private String paymentMethod;
    
    /**
     * Invoice ID that was paid
     */
    private Long invoiceId;
}
