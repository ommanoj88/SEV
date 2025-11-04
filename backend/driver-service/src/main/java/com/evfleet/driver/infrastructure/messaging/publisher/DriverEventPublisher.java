package com.evfleet.driver.infrastructure.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "driver.events";

    public void publishDriverRegistered(Object event) {
        log.info("Publishing DriverRegistered event");
        rabbitTemplate.convertAndSend(EXCHANGE, "driver.registered", event);
    }

    public void publishDriverAssigned(Object event) {
        log.info("Publishing DriverAssigned event");
        rabbitTemplate.convertAndSend(EXCHANGE, "driver.assigned", event);
    }
}
