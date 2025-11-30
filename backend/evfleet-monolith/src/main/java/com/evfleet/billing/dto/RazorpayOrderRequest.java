package com.evfleet.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating a Razorpay order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderRequest {
    
    /**
     * Invoice ID to create order for
     */
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;
    
    /**
     * Amount to charge (optional, defaults to invoice total)
     */
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    /**
     * Customer email for Razorpay prefill
     */
    private String customerEmail;
    
    /**
     * Customer phone for Razorpay prefill
     */
    private String customerPhone;
    
    /**
     * Customer name for Razorpay checkout
     */
    private String customerName;
    
    /**
     * Additional notes/metadata
     */
    private Map<String, String> notes;
}
