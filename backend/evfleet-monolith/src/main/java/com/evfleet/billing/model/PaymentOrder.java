package com.evfleet.billing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentOrder Entity
 * Tracks Razorpay payment orders and their lifecycle
 * 
 * Flow:
 * 1. Order created (CREATED)
 * 2. Payment attempted (ATTEMPTED)
 * 3. Payment captured (PAID) or failed (FAILED)
 * 4. Optional: Refund initiated (REFUND_INITIATED) → completed (REFUNDED)
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payment_orders", indexes = {
    @Index(name = "idx_po_razorpay_order_id", columnList = "razorpay_order_id"),
    @Index(name = "idx_po_razorpay_payment_id", columnList = "razorpay_payment_id"),
    @Index(name = "idx_po_invoice_id", columnList = "invoice_id"),
    @Index(name = "idx_po_company_id", columnList = "company_id"),
    @Index(name = "idx_po_status", columnList = "status"),
    @Index(name = "idx_po_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Razorpay Order ID (order_xxx format)
     */
    @Column(name = "razorpay_order_id", unique = true, nullable = false, length = 50)
    private String razorpayOrderId;

    /**
     * Razorpay Payment ID (pay_xxx format) - populated after payment
     */
    @Column(name = "razorpay_payment_id", unique = true, length = 50)
    private String razorpayPaymentId;

    /**
     * Razorpay Refund ID (rfnd_xxx format) - populated after refund
     */
    @Column(name = "razorpay_refund_id", length = 50)
    private String razorpayRefundId;

    /**
     * Reference to SEV Invoice
     */
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    /**
     * Company making the payment
     */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /**
     * Order amount in smallest currency unit (paise for INR)
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * Amount actually paid (could be less if partial)
     */
    @Column(name = "amount_paid")
    private Long amountPaid;

    /**
     * Amount refunded
     */
    @Column(name = "amount_refunded")
    @Builder.Default
    private Long amountRefunded = 0L;

    /**
     * Currency code (INR, USD, etc.)
     */
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    /**
     * Order receipt number (for merchant reference)
     */
    @Column(name = "receipt", length = 40)
    private String receipt;

    /**
     * Order status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    /**
     * Payment method used (after payment)
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Bank name (for netbanking/UPI)
     */
    @Column(name = "bank", length = 100)
    private String bank;

    /**
     * Wallet name (if wallet payment)
     */
    @Column(name = "wallet", length = 50)
    private String wallet;

    /**
     * VPA for UPI payments
     */
    @Column(name = "vpa", length = 100)
    private String vpa;

    /**
     * Customer email
     */
    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    /**
     * Customer phone
     */
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    /**
     * Razorpay fee charged (in paise)
     */
    @Column(name = "razorpay_fee")
    private Long razorpayFee;

    /**
     * Tax on Razorpay fee (in paise)
     */
    @Column(name = "razorpay_tax")
    private Long razorpayTax;

    /**
     * Error code if payment failed
     */
    @Column(name = "error_code", length = 100)
    private String errorCode;

    /**
     * Error description
     */
    @Column(name = "error_description", length = 500)
    private String errorDescription;

    /**
     * Reason for failure/refund
     */
    @Column(name = "error_reason", length = 255)
    private String errorReason;

    /**
     * Notes/metadata as JSON
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Payment captured timestamp
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Refund initiated timestamp
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /**
     * Order expiry time
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Number of payment attempts
     */
    @Column(name = "attempts")
    @Builder.Default
    private Integer attempts = 0;

    /**
     * Razorpay signature for verification
     */
    @Column(name = "signature", length = 255)
    private String signature;

    /**
     * Whether signature was verified
     */
    @Column(name = "signature_verified")
    @Builder.Default
    private Boolean signatureVerified = false;

    /**
     * Order status enum
     */
    public enum OrderStatus {
        /** Order created, awaiting payment */
        CREATED,
        /** Payment attempted but not completed */
        ATTEMPTED,
        /** Payment authorized (for manual capture) */
        AUTHORIZED,
        /** Payment captured successfully */
        PAID,
        /** Payment failed */
        FAILED,
        /** Payment expired */
        EXPIRED,
        /** Refund initiated */
        REFUND_INITIATED,
        /** Partial refund completed */
        PARTIALLY_REFUNDED,
        /** Full refund completed */
        REFUNDED,
        /** Order cancelled */
        CANCELLED
    }

    /**
     * Get amount in rupees (for display)
     */
    public BigDecimal getAmountInRupees() {
        if (amount == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
    }

    /**
     * Get paid amount in rupees
     */
    public BigDecimal getAmountPaidInRupees() {
        if (amountPaid == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amountPaid).divide(BigDecimal.valueOf(100));
    }

    /**
     * Get refunded amount in rupees
     */
    public BigDecimal getAmountRefundedInRupees() {
        if (amountRefunded == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amountRefunded).divide(BigDecimal.valueOf(100));
    }

    /**
     * Check if order is in final state
     */
    public boolean isTerminal() {
        return status == OrderStatus.PAID 
            || status == OrderStatus.FAILED 
            || status == OrderStatus.EXPIRED
            || status == OrderStatus.REFUNDED
            || status == OrderStatus.CANCELLED;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean isRefundable() {
        return status == OrderStatus.PAID 
            || status == OrderStatus.PARTIALLY_REFUNDED;
    }

    /**
     * Mark as paid
     */
    public void markAsPaid(String paymentId, Long paidAmount) {
        this.status = OrderStatus.PAID;
        this.razorpayPaymentId = paymentId;
        this.amountPaid = paidAmount;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String code, String description, String reason) {
        this.status = OrderStatus.FAILED;
        this.errorCode = code;
        this.errorDescription = description;
        this.errorReason = reason;
    }

    /**
     * Mark as refunded
     */
    public void markAsRefunded(String refundId, Long refundedAmount) {
        this.razorpayRefundId = refundId;
        this.amountRefunded = (this.amountRefunded != null ? this.amountRefunded : 0L) + refundedAmount;
        this.refundedAt = LocalDateTime.now();
        
        if (this.amountRefunded >= this.amountPaid) {
            this.status = OrderStatus.REFUNDED;
        } else {
            this.status = OrderStatus.PARTIALLY_REFUNDED;
        }
    }

    /**
     * Increment attempt count
     */
    public void incrementAttempts() {
        this.attempts = (this.attempts != null ? this.attempts : 0) + 1;
    }

    @PrePersist
    protected void onCreate() {
        if (currency == null) {
            currency = "INR";
        }
        if (status == null) {
            status = OrderStatus.CREATED;
        }
    }
}
