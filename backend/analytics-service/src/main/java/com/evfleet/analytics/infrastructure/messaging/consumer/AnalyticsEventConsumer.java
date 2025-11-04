package com.evfleet.analytics.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    @RabbitListener(queues = "analytics.trip.completed.queue")
    public void handleTripCompleted(String message) {
        log.info("Received trip completed event: {}", message);
        // Update analytics
    }

    @RabbitListener(queues = "analytics.charging.completed.queue")
    public void handleChargingCompleted(String message) {
        log.info("Received charging completed event: {}", message);
        // Update energy cost analytics
    }

    @RabbitListener(queues = "analytics.maintenance.completed.queue")
    public void handleMaintenanceCompleted(String message) {
        log.info("Received maintenance completed event: {}", message);
        // Update maintenance cost analytics
    }
}
