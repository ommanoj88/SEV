package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for invoice payment status
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaymentStatusResponse {

    /**
     * Invoice ID
     */
    private Long invoiceId;

    /**
     * Invoice number
     */
    private String invoiceNumber;

    /**
     * Current invoice status
     */
    private String invoiceStatus;

    /**
     * Total invoice amount
     */
    private BigDecimal totalAmount;

    /**
     * Amount paid so far
     */
    private BigDecimal paidAmount;

    /**
     * Remaining amount to be paid
     */
    private BigDecimal remainingAmount;

    /**
     * Total number of payment attempts
     */
    private int paymentCount;

    /**
     * Number of successful payments
     */
    private int successfulPayments;

    /**
     * Whether there's a pending payment order
     */
    private boolean hasPendingOrder;

    /**
     * Whether invoice is fully paid
     */
    private boolean isFullyPaid;

    /**
     * Whether invoice is overdue
     */
    private boolean isOverdue;

    /**
     * Payment progress percentage
     */
    public int getPaymentProgressPercent() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        if (paidAmount == null) {
            return 0;
        }
        return paidAmount.multiply(BigDecimal.valueOf(100))
                .divide(totalAmount, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    /**
     * Get formatted total amount
     */
    public String getFormattedTotalAmount() {
        return "₹" + String.format("%.2f", totalAmount);
    }

    /**
     * Get formatted paid amount
     */
    public String getFormattedPaidAmount() {
        return "₹" + String.format("%.2f", paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }

    /**
     * Get formatted remaining amount
     */
    public String getFormattedRemainingAmount() {
        return "₹" + String.format("%.2f", remainingAmount != null ? remainingAmount : totalAmount);
    }
}
