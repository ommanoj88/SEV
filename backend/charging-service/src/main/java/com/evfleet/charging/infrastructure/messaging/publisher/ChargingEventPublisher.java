package com.evfleet.charging.infrastructure.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChargingEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "charging.events";

    public void publishSessionStarted(Object event) {
        log.info("Publishing ChargingSessionStarted event");
        rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.started", event);
    }

    public void publishSessionCompleted(Object event) {
        log.info("Publishing ChargingSessionCompleted event");
        rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.completed", event);
    }

    public void publishSessionFailed(Object event) {
        log.info("Publishing ChargingSessionFailed event");
        rabbitTemplate.convertAndSend(EXCHANGE, "charging.session.failed", event);
    }

    public void publishStationOccupied(Object event) {
        log.info("Publishing StationOccupied event");
        rabbitTemplate.convertAndSend(EXCHANGE, "charging.station.occupied", event);
    }

    public void publishStationAvailable(Object event) {
        log.info("Publishing StationAvailable event");
        rabbitTemplate.convertAndSend(EXCHANGE, "charging.station.available", event);
    }
}
