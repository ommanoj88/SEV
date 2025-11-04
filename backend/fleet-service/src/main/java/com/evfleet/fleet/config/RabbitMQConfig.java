package com.evfleet.fleet.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Fleet Service
 * Defines exchanges, queues, and bindings for event-driven communication
 */
@Configuration
public class RabbitMQConfig {

    // Exchange name
    public static final String FLEET_EXCHANGE = "fleet.exchange";

    // Queue names
    public static final String VEHICLE_LOCATION_QUEUE = "vehicle.location.queue";
    public static final String TRIP_COMPLETED_QUEUE = "trip.completed.queue";
    public static final String BATTERY_LOW_QUEUE = "battery.low.queue";

    // Routing keys
    public static final String VEHICLE_LOCATION_ROUTING_KEY = "vehicle.location.updated";
    public static final String TRIP_COMPLETED_ROUTING_KEY = "trip.completed";
    public static final String BATTERY_LOW_ROUTING_KEY = "battery.low";

    /**
     * Create fleet exchange
     */
    @Bean
    public TopicExchange fleetExchange() {
        return new TopicExchange(FLEET_EXCHANGE);
    }

    /**
     * Create vehicle location queue
     */
    @Bean
    public Queue vehicleLocationQueue() {
        return QueueBuilder.durable(VEHICLE_LOCATION_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }

    /**
     * Create trip completed queue
     */
    @Bean
    public Queue tripCompletedQueue() {
        return QueueBuilder.durable(TRIP_COMPLETED_QUEUE)
                .build();
    }

    /**
     * Create battery low queue
     */
    @Bean
    public Queue batteryLowQueue() {
        return QueueBuilder.durable(BATTERY_LOW_QUEUE)
                .build();
    }

    /**
     * Bind vehicle location queue to exchange
     */
    @Bean
    public Binding vehicleLocationBinding() {
        return BindingBuilder
                .bind(vehicleLocationQueue())
                .to(fleetExchange())
                .with(VEHICLE_LOCATION_ROUTING_KEY);
    }

    /**
     * Bind trip completed queue to exchange
     */
    @Bean
    public Binding tripCompletedBinding() {
        return BindingBuilder
                .bind(tripCompletedQueue())
                .to(fleetExchange())
                .with(TRIP_COMPLETED_ROUTING_KEY);
    }

    /**
     * Bind battery low queue to exchange
     */
    @Bean
    public Binding batteryLowBinding() {
        return BindingBuilder
                .bind(batteryLowQueue())
                .to(fleetExchange())
                .with(BATTERY_LOW_ROUTING_KEY);
    }

    /**
     * JSON message converter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
