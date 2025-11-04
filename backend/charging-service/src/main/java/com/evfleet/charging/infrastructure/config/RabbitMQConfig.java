package com.evfleet.charging.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * RabbitMQ Configuration for Charging Service
 * Configures exchanges, queues, bindings, and message converters
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // Exchange Names
    public static final String CHARGING_EXCHANGE = "charging.events";
    public static final String DLX_EXCHANGE = "charging.dlx";

    // Queue Names
    public static final String SESSION_STARTED_QUEUE = "charging.session.started.queue";
    public static final String SESSION_COMPLETED_QUEUE = "charging.session.completed.queue";
    public static final String SESSION_FAILED_QUEUE = "charging.session.failed.queue";
    public static final String STATION_OCCUPIED_QUEUE = "charging.station.occupied.queue";
    public static final String STATION_AVAILABLE_QUEUE = "charging.station.available.queue";

    // Routing Keys
    public static final String SESSION_STARTED_ROUTING_KEY = "charging.session.started";
    public static final String SESSION_COMPLETED_ROUTING_KEY = "charging.session.completed";
    public static final String SESSION_FAILED_ROUTING_KEY = "charging.session.failed";
    public static final String STATION_OCCUPIED_ROUTING_KEY = "charging.station.occupied";
    public static final String STATION_AVAILABLE_ROUTING_KEY = "charging.station.available";

    // Dead Letter Queue Names
    public static final String SESSION_STARTED_DLQ = "charging.session.started.dlq";
    public static final String SESSION_COMPLETED_DLQ = "charging.session.completed.dlq";

    // ==================== Exchanges ====================

    @Bean
    public TopicExchange chargingExchange() {
        return ExchangeBuilder.topicExchange(CHARGING_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== Queues ====================

    @Bean
    public Queue sessionStartedQueue() {
        return QueueBuilder.durable(SESSION_STARTED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SESSION_STARTED_DLQ)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .build();
    }

    @Bean
    public Queue sessionCompletedQueue() {
        return QueueBuilder.durable(SESSION_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SESSION_COMPLETED_DLQ)
                .build();
    }

    @Bean
    public Queue sessionFailedQueue() {
        return QueueBuilder.durable(SESSION_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue stationOccupiedQueue() {
        return QueueBuilder.durable(STATION_OCCUPIED_QUEUE)
                .build();
    }

    @Bean
    public Queue stationAvailableQueue() {
        return QueueBuilder.durable(STATION_AVAILABLE_QUEUE)
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue sessionStartedDLQ() {
        return QueueBuilder.durable(SESSION_STARTED_DLQ).build();
    }

    @Bean
    public Queue sessionCompletedDLQ() {
        return QueueBuilder.durable(SESSION_COMPLETED_DLQ).build();
    }

    // ==================== Bindings ====================

    @Bean
    public Binding sessionStartedBinding() {
        return BindingBuilder
                .bind(sessionStartedQueue())
                .to(chargingExchange())
                .with(SESSION_STARTED_ROUTING_KEY);
    }

    @Bean
    public Binding sessionCompletedBinding() {
        return BindingBuilder
                .bind(sessionCompletedQueue())
                .to(chargingExchange())
                .with(SESSION_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding sessionFailedBinding() {
        return BindingBuilder
                .bind(sessionFailedQueue())
                .to(chargingExchange())
                .with(SESSION_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding stationOccupiedBinding() {
        return BindingBuilder
                .bind(stationOccupiedQueue())
                .to(chargingExchange())
                .with(STATION_OCCUPIED_ROUTING_KEY);
    }

    @Bean
    public Binding stationAvailableBinding() {
        return BindingBuilder
                .bind(stationAvailableQueue())
                .to(chargingExchange())
                .with(STATION_AVAILABLE_ROUTING_KEY);
    }

    // DLQ Bindings
    @Bean
    public Binding sessionStartedDLQBinding() {
        return BindingBuilder
                .bind(sessionStartedDLQ())
                .to(dlxExchange())
                .with(SESSION_STARTED_DLQ);
    }

    @Bean
    public Binding sessionCompletedDLQBinding() {
        return BindingBuilder
                .bind(sessionCompletedDLQ())
                .to(dlxExchange())
                .with(SESSION_COMPLETED_DLQ);
    }

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        template.setMandatory(true);

        // Confirmation Callback
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message published successfully");
            } else {
                log.error("Message publishing failed: {}", cause);
            }
        });

        // Return Callback
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {} from exchange {} with routing key {}",
                    returned.getMessage(),
                    returned.getExchange(),
                    returned.getRoutingKey());
        });

        return template;
    }

    // ==================== Listener Container Factory ====================

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor());

        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
