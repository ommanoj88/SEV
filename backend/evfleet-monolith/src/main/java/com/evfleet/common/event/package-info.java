/**
 * Domain Event Infrastructure
 *
 * This package contains the core event infrastructure for inter-module communication.
 *
 * <h2>Event-Driven Architecture</h2>
 * Modules communicate via domain events instead of direct dependencies.
 * This maintains loose coupling while enabling modules to react to changes.
 *
 * <h2>Publishing Events</h2>
 * Use the {@link com.evfleet.common.event.EventPublisher} to publish events:
 * <pre>
 * {@code
 * @Service
 * @RequiredArgsConstructor
 * public class VehicleService {
 *     private final EventPublisher eventPublisher;
 *
 *     public void createVehicle(VehicleRequest request) {
 *         // ... business logic
 *         eventPublisher.publish(
 *             new VehicleCreatedEvent(this, vehicleId, vehicleNumber)
 *         );
 *     }
 * }
 * }
 * </pre>
 *
 * <h2>Listening to Events</h2>
 * Use @EventListener or @TransactionalEventListener:
 * <pre>
 * {@code
 * @Service
 * @Slf4j
 * public class NotificationEventListener {
 *
 *     @EventListener
 *     @Async
 *     public void handleVehicleCreated(VehicleCreatedEvent event) {
 *         log.info("Vehicle created: {}", event.getVehicleId());
 *         // Send notification
 *     }
 * }
 * }
 * </pre>
 *
 * <h2>Event Processing</h2>
 * - Events are processed asynchronously by default
 * - Use @Async on listener methods for parallel processing
 * - Use @TransactionalEventListener for transactional consistency
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
package com.evfleet.common.event;
