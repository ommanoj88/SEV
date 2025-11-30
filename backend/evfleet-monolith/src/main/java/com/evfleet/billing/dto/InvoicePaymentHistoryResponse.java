package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for invoice payment history
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaymentHistoryResponse {

    /**
     * Payment record ID
     */
    private Long paymentId;

    /**
     * Transaction ID
     */
    private String transactionId;

    /**
     * Payment amount
     */
    private BigDecimal amount;

    /**
     * Payment method used
     */
    private String paymentMethod;

    /**
     * Payment status
     */
    private String status;

    /**
     * Payment initiation date/time
     */
    private LocalDateTime paymentDate;

    /**
     * Payment processing date/time
     */
    private LocalDateTime processedAt;

    /**
     * Failure reason (if failed)
     */
    private String failureReason;

    /**
     * Get formatted amount
     */
    public String getFormattedAmount() {
        return "₹" + String.format("%.2f", amount);
    }

    /**
     * Check if payment was successful
     */
    public boolean isSuccessful() {
        return "COMPLETED".equals(status);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }
}
