package com.evfleet.common.event;

import lombok.extern.slf4j.Slf4j;

/**
 * Base support class for event listeners
 *
 * Provides common functionality for event listener implementations.
 * Extend this class to get standard logging and error handling.
 *
 * Usage:
 * <pre>
 * {@code
 * @Service
 * public class NotificationEventListener extends EventListenerSupport {
 *
 *     @EventListener
 *     @Async
 *     public void handleVehicleCreated(VehicleCreatedEvent event) {
 *         logEventReceived(event);
 *         try {
 *             // Process event
 *             sendNotification(event);
 *             logEventProcessed(event);
 *         } catch (Exception e) {
 *             logEventError(event, e);
 *         }
 *     }
 * }
 * }
 * </pre>
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Slf4j
public abstract class EventListenerSupport {

    /**
     * Log when an event is received
     */
    protected void logEventReceived(DomainEvent event) {
        log.info("Received event: {} [id: {}]", event.getEventType(), event.getEventId());
    }

    /**
     * Log when an event is successfully processed
     */
    protected void logEventProcessed(DomainEvent event) {
        log.info("Successfully processed event: {} [id: {}]", event.getEventType(), event.getEventId());
    }

    /**
     * Log when an event processing fails
     */
    protected void logEventError(DomainEvent event, Exception e) {
        log.error("Failed to process event: {} [id: {}] - Error: {}",
                event.getEventType(), event.getEventId(), e.getMessage(), e);
    }

    /**
     * Log when an event is ignored/skipped
     */
    protected void logEventIgnored(DomainEvent event, String reason) {
        log.debug("Ignoring event: {} [id: {}] - Reason: {}",
                event.getEventType(), event.getEventId(), reason);
    }
}
