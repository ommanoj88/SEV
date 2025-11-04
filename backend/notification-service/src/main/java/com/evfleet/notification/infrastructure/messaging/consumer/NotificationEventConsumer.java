package com.evfleet.notification.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    @RabbitListener(queues = "notification.battery.low.queue")
    public void handleBatteryLow(String message) {
        log.info("Received battery low event: {}", message);
        // Send battery low notification
    }

    @RabbitListener(queues = "notification.maintenance.due.queue")
    public void handleMaintenanceDue(String message) {
        log.info("Received maintenance due event: {}", message);
        // Send maintenance reminder
    }

    @RabbitListener(queues = "notification.charging.completed.queue")
    public void handleChargingCompleted(String message) {
        log.info("Received charging completed event: {}", message);
        // Send charging receipt
    }

    @RabbitListener(queues = "notification.trip.completed.queue")
    public void handleTripCompleted(String message) {
        log.info("Received trip completed event: {}", message);
        // Send trip summary
    }
}
