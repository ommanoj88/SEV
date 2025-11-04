package com.evfleet.charging.application.service;

import com.evfleet.charging.application.command.EndChargingSessionCommand;
import com.evfleet.charging.application.command.StartChargingSessionCommand;
import com.evfleet.charging.domain.model.aggregate.ChargingSessionAggregate;
import com.evfleet.charging.domain.model.aggregate.ChargingStationAggregate;
import com.evfleet.charging.infrastructure.messaging.publisher.ChargingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Charging Session Saga Orchestrator
 * Implements the Saga pattern for distributed transactions in charging sessions
 *
 * Saga Steps:
 * 1. Reserve charging slot at station
 * 2. Start charging session
 * 3. Deduct credits from billing service (external call)
 * 4. Complete session
 *
 * Compensation Steps (if any step fails):
 * - Release reserved slot
 * - Refund credits
 * - Mark session as failed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingSessionSaga {

    private final ChargingEventPublisher eventPublisher;

    /**
     * Execute the saga for starting a charging session
     */
    @Transactional
    public String executeStartSessionSaga(StartChargingSessionCommand command) {
        String sessionId = UUID.randomUUID().toString();
        SagaContext context = new SagaContext(sessionId);

        try {
            // Step 1: Reserve slot
            log.info("[SAGA] Step 1: Reserving slot at station {}", command.getStationId());
            boolean slotReserved = reserveSlotStep(command.getStationId(), sessionId);
            context.markSlotReserved(slotReserved);

            if (!slotReserved) {
                throw new SagaException("Failed to reserve slot - station full");
            }

            // Step 2: Start session
            log.info("[SAGA] Step 2: Starting charging session");
            startSessionStep(sessionId, command.getVehicleId(), command.getStationId());
            context.markSessionStarted(true);

            // Step 3: Validate billing credits (simplified - would call billing service)
            log.info("[SAGA] Step 3: Validating billing credits");
            boolean creditsValid = validateCreditsStep(command.getUserId());
            context.markCreditsValidated(creditsValid);

            if (!creditsValid) {
                throw new SagaException("Insufficient credits for charging");
            }

            log.info("[SAGA] Charging session saga completed successfully. SessionId: {}", sessionId);
            return sessionId;

        } catch (Exception e) {
            log.error("[SAGA] Saga failed, executing compensation: {}", e.getMessage());
            compensate(context, command);
            throw new RuntimeException("Failed to start charging session: " + e.getMessage(), e);
        }
    }

    /**
     * Execute the saga for ending a charging session
     */
    @Transactional
    public void executeEndSessionSaga(EndChargingSessionCommand command) {
        SagaContext context = new SagaContext(command.getSessionId());

        try {
            // Step 1: Calculate cost
            log.info("[SAGA] Step 1: Calculating charging cost");
            BigDecimal cost = calculateCostStep(command.getEnergyConsumed());
            context.setCost(cost);

            // Step 2: Deduct from billing
            log.info("[SAGA] Step 2: Deducting cost from billing");
            boolean paymentSuccess = deductBillingStep(command.getSessionId(), cost);
            context.markPaymentProcessed(paymentSuccess);

            if (!paymentSuccess) {
                throw new SagaException("Payment processing failed");
            }

            // Step 3: Complete session
            log.info("[SAGA] Step 3: Completing charging session");
            completeSessionStep(command.getSessionId(), command.getEnergyConsumed(), cost);
            context.markSessionCompleted(true);

            // Step 4: Release slot
            log.info("[SAGA] Step 4: Releasing charging slot");
            releaseSlotStep(command.getSessionId());

            log.info("[SAGA] End session saga completed successfully");

        } catch (Exception e) {
            log.error("[SAGA] End session saga failed: {}", e.getMessage());
            compensateEndSession(context, command);
            throw new RuntimeException("Failed to end charging session: " + e.getMessage(), e);
        }
    }

    // Saga Steps Implementation

    private boolean reserveSlotStep(String stationId, String sessionId) {
        // Create aggregate and reserve slot
        // In production, load from repository
        ChargingStationAggregate station = new ChargingStationAggregate(
            stationId, "Station", null, 10
        );

        boolean reserved = station.reserveSlot(sessionId);

        if (reserved) {
            // Publish events
            station.getAndClearDomainEvents().forEach(event -> {
                if (event instanceof com.evfleet.charging.domain.model.event.StationOccupied) {
                    eventPublisher.publishStationOccupied(event);
                }
            });
        }

        return reserved;
    }

    private void startSessionStep(String sessionId, String vehicleId, String stationId) {
        ChargingSessionAggregate session = new ChargingSessionAggregate();
        session.startSession(sessionId, vehicleId, stationId);

        // Publish events
        session.getAndClearDomainEvents().forEach(event -> {
            if (event instanceof com.evfleet.charging.domain.model.event.ChargingSessionStarted) {
                eventPublisher.publishSessionStarted(event);
            }
        });
    }

    private boolean validateCreditsStep(String userId) {
        // In production: Call billing service via Feign client with circuit breaker
        // For now, return true
        return true;
    }

    private BigDecimal calculateCostStep(BigDecimal energyKwh) {
        // Simple cost calculation: ₹12 per kWh
        BigDecimal ratePerKwh = BigDecimal.valueOf(12.0);
        return energyKwh.multiply(ratePerKwh);
    }

    private boolean deductBillingStep(String sessionId, BigDecimal amount) {
        // In production: Call billing service
        log.info("Deducting {} from billing for session {}", amount, sessionId);
        return true;
    }

    private void completeSessionStep(String sessionId, BigDecimal energyKwh, BigDecimal cost) {
        // Load aggregate and complete
        ChargingSessionAggregate session = new ChargingSessionAggregate();
        // session would be loaded from repository with sessionId
        session.completeSession(energyKwh, cost);

        // Publish events
        session.getAndClearDomainEvents().forEach(event -> {
            if (event instanceof com.evfleet.charging.domain.model.event.ChargingSessionCompleted) {
                eventPublisher.publishSessionCompleted(event);
            }
        });
    }

    private void releaseSlotStep(String sessionId) {
        // In production: Load station aggregate and release slot
        log.info("Releasing slot for session {}", sessionId);
    }

    // Compensation Logic

    private void compensate(SagaContext context, StartChargingSessionCommand command) {
        log.warn("[COMPENSATION] Starting compensation for failed saga");

        if (context.isSessionStarted()) {
            log.warn("[COMPENSATION] Canceling started session");
            cancelSession(context.getSessionId());
        }

        if (context.isSlotReserved()) {
            log.warn("[COMPENSATION] Releasing reserved slot");
            releaseSlot(command.getStationId(), context.getSessionId());
        }
    }

    private void compensateEndSession(SagaContext context, EndChargingSessionCommand command) {
        log.warn("[COMPENSATION] Starting compensation for end session saga");

        if (context.isPaymentProcessed()) {
            log.warn("[COMPENSATION] Refunding payment");
            refundPayment(context.getSessionId(), context.getCost());
        }
    }

    private void cancelSession(String sessionId) {
        // Mark session as failed
        log.info("Session {} cancelled", sessionId);
    }

    private void releaseSlot(String stationId, String sessionId) {
        // Release the reserved slot
        log.info("Slot released at station {} for session {}", stationId, sessionId);
    }

    private void refundPayment(String sessionId, BigDecimal amount) {
        // Call billing service to refund
        log.info("Refunding {} for session {}", amount, sessionId);
    }

    // Saga Context - Tracks saga state

    @lombok.Data
    private static class SagaContext {
        private final String sessionId;
        private boolean slotReserved;
        private boolean sessionStarted;
        private boolean creditsValidated;
        private boolean paymentProcessed;
        private boolean sessionCompleted;
        private BigDecimal cost;

        public SagaContext(String sessionId) {
            this.sessionId = sessionId;
        }

        public void markSlotReserved(boolean reserved) {
            this.slotReserved = reserved;
        }

        public void markSessionStarted(boolean started) {
            this.sessionStarted = started;
        }

        public void markCreditsValidated(boolean validated) {
            this.creditsValidated = validated;
        }

        public void markPaymentProcessed(boolean processed) {
            this.paymentProcessed = processed;
        }

        public void markSessionCompleted(boolean completed) {
            this.sessionCompleted = completed;
        }
    }

    // Custom Exception
    private static class SagaException extends RuntimeException {
        public SagaException(String message) {
            super(message);
        }
    }
}
