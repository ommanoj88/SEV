package com.evfleet.billing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebhookEvent Entity
 * 
 * Records all incoming webhook events from payment gateways
 * for audit trail and idempotent processing.
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "webhook_events", indexes = {
    @Index(name = "idx_we_event_id", columnList = "event_id"),
    @Index(name = "idx_we_payment_id", columnList = "payment_id"),
    @Index(name = "idx_we_order_id", columnList = "order_id"),
    @Index(name = "idx_we_event_type", columnList = "event_type"),
    @Index(name = "idx_we_status", columnList = "status"),
    @Index(name = "idx_we_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique event ID from Razorpay
     */
    @Column(name = "event_id", unique = true, nullable = false, length = 100)
    private String eventId;

    /**
     * Event type (e.g., payment.captured, payment.failed, refund.created)
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * Source gateway (RAZORPAY, STRIPE, etc.)
     */
    @Column(name = "source", nullable = false, length = 50)
    @Builder.Default
    private String source = "RAZORPAY";

    /**
     * Razorpay payment ID
     */
    @Column(name = "payment_id", length = 50)
    private String paymentId;

    /**
     * Razorpay order ID
     */
    @Column(name = "order_id", length = 50)
    private String orderId;

    /**
     * Raw webhook payload (JSON)
     */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    /**
     * Webhook signature from header
     */
    @Column(name = "signature", length = 500)
    private String signature;

    /**
     * Whether signature was valid
     */
    @Column(name = "signature_valid")
    @Builder.Default
    private Boolean signatureValid = false;

    /**
     * Processing status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WebhookStatus status = WebhookStatus.RECEIVED;

    /**
     * Error message if processing failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Processing start time
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Number of processing attempts
     */
    @Column(name = "attempts")
    @Builder.Default
    private Integer attempts = 0;

    /**
     * IP address of webhook sender
     */
    @Column(name = "source_ip", length = 50)
    private String sourceIp;

    /**
     * User agent of webhook sender
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Webhook processing status
     */
    public enum WebhookStatus {
        /** Webhook received but not yet processed */
        RECEIVED,
        /** Processing in progress */
        PROCESSING,
        /** Successfully processed */
        PROCESSED,
        /** Processing failed */
        FAILED,
        /** Duplicate event (already processed) */
        DUPLICATE,
        /** Signature verification failed */
        INVALID_SIGNATURE,
        /** Unknown event type - ignored */
        IGNORED
    }

    /**
     * Mark as processing
     */
    public void startProcessing() {
        this.status = WebhookStatus.PROCESSING;
        this.attempts = (this.attempts != null ? this.attempts : 0) + 1;
    }

    /**
     * Mark as successfully processed
     */
    public void markProcessed() {
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markFailed(String error) {
        this.status = WebhookStatus.FAILED;
        this.errorMessage = error;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark as duplicate
     */
    public void markDuplicate() {
        this.status = WebhookStatus.DUPLICATE;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark as invalid signature
     */
    public void markInvalidSignature() {
        this.status = WebhookStatus.INVALID_SIGNATURE;
        this.signatureValid = false;
        this.processedAt = LocalDateTime.now();
    }
}
