package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for payment details fetched from Razorpay
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayPaymentDetailsResponse {
    
    /**
     * Razorpay Payment ID
     */
    private String paymentId;
    
    /**
     * Razorpay Order ID
     */
    private String orderId;
    
    /**
     * Payment status (created, authorized, captured, failed, refunded)
     */
    private String status;
    
    /**
     * Payment method (card, netbanking, upi, wallet)
     */
    private String method;
    
    /**
     * Amount in rupees
     */
    private BigDecimal amount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Bank name (for netbanking)
     */
    private String bank;
    
    /**
     * Wallet name
     */
    private String wallet;
    
    /**
     * VPA for UPI
     */
    private String vpa;
    
    /**
     * Customer email
     */
    private String email;
    
    /**
     * Customer contact
     */
    private String contact;
    
    /**
     * Razorpay fee in rupees
     */
    private BigDecimal fee;
    
    /**
     * Tax on fee in rupees
     */
    private BigDecimal tax;
    
    /**
     * Whether payment is captured
     */
    private Boolean captured;
    
    /**
     * Card details (if card payment)
     */
    private CardDetails card;
    
    /**
     * Payment created timestamp
     */
    private Long createdAt;
    
    /**
     * Error code if failed
     */
    private String errorCode;
    
    /**
     * Error description if failed
     */
    private String errorDescription;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardDetails {
        private String last4;
        private String network;
        private String type;
        private String issuer;
        private Boolean international;
        private Boolean emi;
    }
}
