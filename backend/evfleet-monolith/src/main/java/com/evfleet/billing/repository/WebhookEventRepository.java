package com.evfleet.billing.repository;

import com.evfleet.billing.model.WebhookEvent;
import com.evfleet.billing.model.WebhookEvent.WebhookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * WebhookEventRepository
 * 
 * Provides database operations for webhook events with support for:
 * - Idempotency checking via event ID lookup
 * - Status-based querying for retry operations
 * - Analytics and monitoring queries
 * - Cleanup of old processed events
 * 
 * @author EV Fleet Management
 * @since 1.0.0
 */
@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    
    // ==================== Idempotency Operations ====================
    
    /**
     * Find a webhook event by its unique event ID from the payment gateway.
     * Used for idempotency checking to prevent duplicate processing.
     *
     * @param eventId The unique event identifier from Razorpay
     * @return Optional containing the event if found
     */
    Optional<WebhookEvent> findByEventId(String eventId);
    
    /**
     * Check if an event with the given ID already exists.
     * More efficient than findByEventId when you only need to check existence.
     *
     * @param eventId The unique event identifier from Razorpay
     * @return true if the event exists, false otherwise
     */
    boolean existsByEventId(String eventId);
    
    /**
     * Find an event by event ID and source for multi-gateway support.
     *
     * @param eventId The unique event identifier
     * @param source The payment gateway source (e.g., "RAZORPAY")
     * @return Optional containing the event if found
     */
    Optional<WebhookEvent> findByEventIdAndSource(String eventId, String source);
    
    // ==================== Status-Based Queries ====================
    
    /**
     * Find all events with a specific status.
     *
     * @param status The webhook status to filter by
     * @return List of events with the given status
     */
    List<WebhookEvent> findByStatus(WebhookStatus status);
    
    /**
     * Find all events with a specific status, paginated.
     *
     * @param status The webhook status to filter by
     * @param pageable Pagination parameters
     * @return Page of events with the given status
     */
    Page<WebhookEvent> findByStatus(WebhookStatus status, Pageable pageable);
    
    /**
     * Find events that failed and are eligible for retry.
     * Events with fewer attempts than the max and in FAILED status.
     *
     * @param status The status to filter by (typically FAILED)
     * @param maxAttempts Maximum number of retry attempts
     * @return List of events eligible for retry
     */
    @Query("SELECT w FROM WebhookEvent w WHERE w.status = :status AND w.attempts < :maxAttempts ORDER BY w.createdAt ASC")
    List<WebhookEvent> findRetryableEvents(
            @Param("status") WebhookStatus status,
            @Param("maxAttempts") Integer maxAttempts
    );
    
    /**
     * Find events that have been stuck in PROCESSING status.
     * Used to detect and recover from orphaned processing jobs.
     *
     * @param status The status (typically PROCESSING)
     * @param beforeTime Events created before this time
     * @return List of stuck events
     */
    @Query("SELECT w FROM WebhookEvent w WHERE w.status = :status AND w.createdAt < :beforeTime")
    List<WebhookEvent> findStuckEvents(
            @Param("status") WebhookStatus status,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    // ==================== Payment-Related Queries ====================
    
    /**
     * Find all webhook events for a specific payment ID.
     *
     * @param paymentId The Razorpay payment ID
     * @return List of events related to the payment
     */
    List<WebhookEvent> findByPaymentIdOrderByCreatedAtDesc(String paymentId);
    
    /**
     * Find all webhook events for a specific order ID.
     *
     * @param orderId The Razorpay order ID
     * @return List of events related to the order
     */
    List<WebhookEvent> findByOrderIdOrderByCreatedAtDesc(String orderId);
    
    /**
     * Find events by event type for analytics.
     *
     * @param eventType The type of event (e.g., "payment.captured")
     * @param pageable Pagination parameters
     * @return Page of events of the given type
     */
    Page<WebhookEvent> findByEventType(String eventType, Pageable pageable);
    
    /**
     * Find events by type and status.
     *
     * @param eventType The type of event
     * @param status The webhook status
     * @return List of matching events
     */
    List<WebhookEvent> findByEventTypeAndStatus(String eventType, WebhookStatus status);
    
    // ==================== Analytics Queries ====================
    
    /**
     * Count events by status for dashboard metrics.
     *
     * @param status The status to count
     * @return Number of events with the given status
     */
    long countByStatus(WebhookStatus status);
    
    /**
     * Count events by event type.
     *
     * @param eventType The event type to count
     * @return Number of events of the given type
     */
    long countByEventType(String eventType);
    
    /**
     * Count failed events that have exceeded max retry attempts.
     *
     * @param status The status (typically FAILED)
     * @param maxAttempts Maximum number of retry attempts
     * @return Number of permanently failed events
     */
    @Query("SELECT COUNT(w) FROM WebhookEvent w WHERE w.status = :status AND w.attempts >= :maxAttempts")
    long countPermanentlyFailedEvents(
            @Param("status") WebhookStatus status,
            @Param("maxAttempts") Integer maxAttempts
    );
    
    /**
     * Count events created within a time range.
     *
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return Number of events in the range
     */
    @Query("SELECT COUNT(w) FROM WebhookEvent w WHERE w.createdAt BETWEEN :startTime AND :endTime")
    long countEventsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Count events by status within a time range for monitoring.
     *
     * @param status The status to count
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return Number of events with the status in the range
     */
    @Query("SELECT COUNT(w) FROM WebhookEvent w WHERE w.status = :status AND w.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusInTimeRange(
            @Param("status") WebhookStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get all distinct event types in the system.
     *
     * @return List of unique event types
     */
    @Query("SELECT DISTINCT w.eventType FROM WebhookEvent w")
    List<String> findDistinctEventTypes();
    
    // ==================== Signature-Related Queries ====================
    
    /**
     * Find events with invalid signatures for security monitoring.
     *
     * @param signatureValid Whether the signature was valid
     * @param pageable Pagination parameters
     * @return Page of events with the given signature validity
     */
    Page<WebhookEvent> findBySignatureValid(Boolean signatureValid, Pageable pageable);
    
    /**
     * Count events with invalid signatures for security alerts.
     *
     * @param signatureValid Whether the signature was valid
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return Number of events with invalid signatures
     */
    @Query("SELECT COUNT(w) FROM WebhookEvent w WHERE w.signatureValid = :signatureValid AND w.createdAt BETWEEN :startTime AND :endTime")
    long countBySignatureValidInTimeRange(
            @Param("signatureValid") Boolean signatureValid,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    // ==================== Cleanup Operations ====================
    
    /**
     * Find old completed events eligible for cleanup.
     *
     * @param status The status (typically COMPLETED)
     * @param beforeTime Events processed before this time
     * @param pageable Pagination parameters
     * @return Page of old events to clean up
     */
    @Query("SELECT w FROM WebhookEvent w WHERE w.status = :status AND w.processedAt < :beforeTime")
    Page<WebhookEvent> findOldCompletedEvents(
            @Param("status") WebhookStatus status,
            @Param("beforeTime") LocalDateTime beforeTime,
            Pageable pageable
    );
    
    /**
     * Delete old completed events for storage cleanup.
     * Should be used with caution and typically scheduled during off-peak hours.
     *
     * @param status The status of events to delete
     * @param beforeTime Delete events processed before this time
     * @return Number of events deleted
     */
    @Modifying
    @Query("DELETE FROM WebhookEvent w WHERE w.status = :status AND w.processedAt < :beforeTime")
    int deleteOldEvents(
            @Param("status") WebhookStatus status,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    /**
     * Archive old events by marking them as archived.
     * Alternative to deletion for audit compliance.
     *
     * @param newStatus The new status to set (e.g., ARCHIVED)
     * @param currentStatus The current status of events to archive
     * @param beforeTime Archive events processed before this time
     * @return Number of events archived
     */
    @Modifying
    @Query("UPDATE WebhookEvent w SET w.status = :newStatus WHERE w.status = :currentStatus AND w.processedAt < :beforeTime")
    int archiveOldEvents(
            @Param("newStatus") WebhookStatus newStatus,
            @Param("currentStatus") WebhookStatus currentStatus,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    // ==================== Source IP Analytics ====================
    
    /**
     * Find events from a specific IP for security monitoring.
     *
     * @param sourceIp The source IP address
     * @param pageable Pagination parameters
     * @return Page of events from the given IP
     */
    Page<WebhookEvent> findBySourceIp(String sourceIp, Pageable pageable);
    
    /**
     * Count events from a specific IP in a time range.
     * Used to detect potential abuse or attacks.
     *
     * @param sourceIp The source IP address
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return Number of events from the IP in the range
     */
    @Query("SELECT COUNT(w) FROM WebhookEvent w WHERE w.sourceIp = :sourceIp AND w.createdAt BETWEEN :startTime AND :endTime")
    long countBySourceIpInTimeRange(
            @Param("sourceIp") String sourceIp,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
