package com.evfleet.maintenance.infrastructure.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "maintenance.events";

    public void publishMaintenanceScheduled(Object event) {
        log.info("Publishing MaintenanceScheduled event");
        rabbitTemplate.convertAndSend(EXCHANGE, "maintenance.scheduled", event);
    }

    public void publishMaintenanceCompleted(Object event) {
        log.info("Publishing MaintenanceCompleted event");
        rabbitTemplate.convertAndSend(EXCHANGE, "maintenance.completed", event);
    }

    public void publishBatteryHealthDegraded(Object event) {
        log.info("Publishing BatteryHealthDegraded event");
        rabbitTemplate.convertAndSend(EXCHANGE, "battery.health.degraded", event);
    }
}
