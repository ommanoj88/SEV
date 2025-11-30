package com.evfleet.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for initiating an invoice payment
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaymentRequest {

    /**
     * ID of the invoice to pay
     */
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    /**
     * Payment amount in rupees
     * If null, full remaining amount will be charged
     * If specified, partial payment is initiated
     */
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    /**
     * Customer email for receipt
     */
    @Email(message = "Invalid email format")
    private String customerEmail;

    /**
     * Customer phone number
     */
    private String customerPhone;

    /**
     * Additional notes for the payment
     */
    private Map<String, String> notes;

    /**
     * Whether to auto-send receipt email after payment
     */
    @Builder.Default
    private boolean sendReceiptEmail = true;

    /**
     * Callback URL after successful payment
     */
    private String successUrl;

    /**
     * Callback URL after failed payment
     */
    private String failureUrl;
}
