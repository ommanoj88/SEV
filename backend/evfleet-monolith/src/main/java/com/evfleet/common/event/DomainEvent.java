package com.evfleet.common.event;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events
 *
 * Domain events represent something that happened in a domain module
 * that other modules might be interested in. Events are the primary
 * mechanism for inter-module communication in the monolith.
 *
 * Usage:
 * <pre>
 * {@code
 * public class VehicleCreatedEvent extends DomainEvent {
 *     private final Long vehicleId;
 *     private final String vehicleNumber;
 *
 *     public VehicleCreatedEvent(Object source, Long vehicleId, String vehicleNumber) {
 *         super(source);
 *         this.vehicleId = vehicleId;
 *         this.vehicleNumber = vehicleNumber;
 *     }
 * }
 * }
 * </pre>
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Getter
public abstract class DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this event
     */
    private final String eventId;

    /**
     * Type of the event (class name)
     */
    private final String eventType;

    /**
     * Timestamp when the event occurred
     */
    private final LocalDateTime occurredAt;

    /**
     * Source object that published this event
     */
    private final transient Object source;

    /**
     * User ID who triggered this event (if applicable)
     */
    private Long userId;

    /**
     * Company ID associated with this event (if applicable)
     */
    private Long companyId;

    protected DomainEvent(Object source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.occurredAt = LocalDateTime.now();
        this.source = source;
    }

    protected DomainEvent(Object source, Long userId, Long companyId) {
        this(source);
        this.userId = userId;
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return String.format("%s[eventId=%s, occurredAt=%s]",
                eventType, eventId, occurredAt);
    }
}
