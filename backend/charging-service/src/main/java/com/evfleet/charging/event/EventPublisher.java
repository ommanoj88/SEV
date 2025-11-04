package com.evfleet.charging.event;

import com.evfleet.charging.entity.ChargingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "evfleet.events";

    public void publishChargingSessionStarted(ChargingSession session) {
        log.info("Publishing charging session started event: sessionId={}, vehicleId={}, stationId={}",
                session.getId(), session.getVehicleId(), session.getStationId());
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.started",
                new ChargingSessionStartedEvent(
                    session.getId().toString(),
                    session.getVehicleId().toString(),
                    session.getStationId().toString()));
        } catch (Exception e) {
            log.error("Error publishing charging session started event", e);
        }
    }

    public void publishChargingSessionCompleted(ChargingSession session) {
        log.info("Publishing charging session completed event: sessionId={}, vehicleId={}, energy={}",
                session.getId(), session.getVehicleId(), session.getEnergyConsumed());
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.completed",
                new ChargingSessionCompletedEvent(
                    session.getId().toString(),
                    session.getVehicleId().toString(),
                    session.getEnergyConsumed().doubleValue()));
        } catch (Exception e) {
            log.error("Error publishing charging session completed event", e);
        }
    }

    public void publishChargingSessionFailed(String sessionId, String reason) {
        log.info("Publishing charging session failed event: sessionId={}, reason={}", sessionId, reason);
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.failed",
                new ChargingSessionFailedEvent(sessionId, reason));
        } catch (Exception e) {
            log.error("Error publishing charging session failed event", e);
        }
    }

    // Event DTOs
    public static class ChargingSessionStartedEvent {
        private final String sessionId;
        private final String vehicleId;
        private final String stationId;

        public ChargingSessionStartedEvent(String sessionId, String vehicleId, String stationId) {
            this.sessionId = sessionId;
            this.vehicleId = vehicleId;
            this.stationId = stationId;
        }

        public String getSessionId() { return sessionId; }
        public String getVehicleId() { return vehicleId; }
        public String getStationId() { return stationId; }
    }

    public static class ChargingSessionCompletedEvent {
        private final String sessionId;
        private final String vehicleId;
        private final Double energyDelivered;

        public ChargingSessionCompletedEvent(String sessionId, String vehicleId, Double energyDelivered) {
            this.sessionId = sessionId;
            this.vehicleId = vehicleId;
            this.energyDelivered = energyDelivered;
        }

        public String getSessionId() { return sessionId; }
        public String getVehicleId() { return vehicleId; }
        public Double getEnergyDelivered() { return energyDelivered; }
    }

    public static class ChargingSessionFailedEvent {
        private final String sessionId;
        private final String reason;

        public ChargingSessionFailedEvent(String sessionId, String reason) {
            this.sessionId = sessionId;
            this.reason = reason;
        }

        public String getSessionId() { return sessionId; }
        public String getReason() { return reason; }
    }
}
