package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for created Razorpay order
 * Contains all info needed for client-side Razorpay checkout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
    
    /**
     * Razorpay Order ID (order_xxx format)
     */
    private String orderId;
    
    /**
     * SEV Invoice ID
     */
    private Long invoiceId;
    
    /**
     * Invoice number for display
     */
    private String invoiceNumber;
    
    /**
     * Order amount in main currency unit (rupees)
     */
    private BigDecimal amount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Receipt/reference number
     */
    private String receipt;
    
    /**
     * Order status
     */
    private String status;
    
    /**
     * Razorpay Key ID (for client checkout)
     */
    private String keyId;
    
    /**
     * Company name for Razorpay checkout
     */
    private String companyName;
    
    /**
     * Order description
     */
    private String description;
    
    /**
     * Callback URL after payment
     */
    private String callbackUrl;
    
    /**
     * Order expiry time
     */
    private LocalDateTime expiresAt;
    
    /**
     * Prefill email for checkout
     */
    private String prefillEmail;
    
    /**
     * Prefill phone for checkout
     */
    private String prefillPhone;
    
    /**
     * Prefill name for checkout
     */
    private String prefillName;
}
