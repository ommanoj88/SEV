package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment receipt
 * Contains all details for generating a payment receipt
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptResponse {

    // ========== Receipt Information ==========
    
    /**
     * Unique receipt number
     */
    private String receiptNumber;

    /**
     * Receipt generation date/time
     */
    private LocalDateTime receiptDate;

    /**
     * Formatted receipt date for display
     */
    private String formattedReceiptDate;

    // ========== Invoice Information ==========
    
    /**
     * Invoice ID
     */
    private Long invoiceId;

    /**
     * Invoice number
     */
    private String invoiceNumber;

    // ========== Payment Information ==========
    
    /**
     * Payment record ID
     */
    private Long paymentId;

    /**
     * Transaction ID (Razorpay payment ID)
     */
    private String transactionId;

    /**
     * Razorpay order ID
     */
    private String razorpayOrderId;

    /**
     * Razorpay payment ID
     */
    private String razorpayPaymentId;

    /**
     * Amount paid in rupees
     */
    private BigDecimal amountPaid;

    /**
     * Payment method used (UPI, Card, Net Banking, etc.)
     */
    private String paymentMethod;

    /**
     * Currency code
     */
    private String currency;

    // ========== Invoice Summary ==========
    
    /**
     * Total invoice amount
     */
    private BigDecimal invoiceTotal;

    /**
     * Amount paid before this payment
     */
    private BigDecimal previouslyPaid;

    /**
     * Amount paid in this transaction
     */
    private BigDecimal totalPaidNow;

    /**
     * Remaining amount after this payment
     */
    private BigDecimal remainingAmount;

    /**
     * Whether invoice is fully paid
     */
    private boolean isFullyPaid;

    // ========== Company Information ==========
    
    /**
     * Company ID
     */
    private Long companyId;

    // ========== Additional Payment Details ==========
    
    /**
     * Bank name (for net banking/card payments)
     */
    private String bank;

    /**
     * Wallet name (for wallet payments)
     */
    private String wallet;

    /**
     * VPA (for UPI payments)
     */
    private String vpa;

    /**
     * Razorpay processing fee in rupees
     */
    private BigDecimal razorpayFee;

    /**
     * Formatted payment method for display
     */
    public String getFormattedPaymentMethod() {
        if (vpa != null && !vpa.isEmpty()) {
            return "UPI (" + vpa + ")";
        }
        if (wallet != null && !wallet.isEmpty()) {
            return "Wallet - " + wallet;
        }
        if (bank != null && !bank.isEmpty()) {
            return paymentMethod + " - " + bank;
        }
        return paymentMethod != null ? paymentMethod : "Razorpay";
    }

    /**
     * Get formatted amount with currency symbol
     */
    public String getFormattedAmountPaid() {
        return "₹" + String.format("%.2f", amountPaid);
    }

    /**
     * Get formatted remaining amount with currency symbol
     */
    public String getFormattedRemainingAmount() {
        return "₹" + String.format("%.2f", remainingAmount);
    }
}
