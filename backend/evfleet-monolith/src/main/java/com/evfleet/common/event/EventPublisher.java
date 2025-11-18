package com.evfleet.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Event Publisher Service
 *
 * Central service for publishing domain events across modules.
 * Uses Spring's ApplicationEventPublisher for event propagation.
 *
 * Events are published asynchronously by default (see EventConfig).
 * Listeners in other modules can subscribe to these events.
 *
 * Usage:
 * <pre>
 * {@code
 * @Service
 * public class VehicleService {
 *     private final EventPublisher eventPublisher;
 *
 *     public void createVehicle(VehicleRequest request) {
 *         Vehicle vehicle = // ... create vehicle
 *         vehicleRepository.save(vehicle);
 *
 *         // Publish event for other modules
 *         eventPublisher.publish(
 *             new VehicleCreatedEvent(this, vehicle.getId(), vehicle.getVehicleNumber())
 *         );
 *     }
 * }
 * }
 * </pre>
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish a domain event
     *
     * @param event The domain event to publish
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {}", event);
        applicationEventPublisher.publishEvent(event);
        log.debug("Event published successfully: {}", event.getEventType());
    }

    /**
     * Publish a domain event with additional logging
     *
     * @param event The domain event to publish
     * @param context Additional context for logging
     */
    public void publish(DomainEvent event, String context) {
        log.info("Publishing event: {} - Context: {}", event.getEventType(), context);
        applicationEventPublisher.publishEvent(event);
        log.debug("Event published successfully: {}", event.getEventType());
    }
}
