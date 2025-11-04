package com.evfleet.fleet.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publisher for fleet-related events to RabbitMQ
 */
@Component
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String FLEET_EXCHANGE = "fleet.exchange";
    private static final String VEHICLE_LOCATION_ROUTING_KEY = "vehicle.location.updated";
    private static final String TRIP_COMPLETED_ROUTING_KEY = "trip.completed";
    private static final String BATTERY_LOW_ROUTING_KEY = "battery.low";

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publish vehicle location update event
     */
    public void publishVehicleLocationUpdate(VehicleLocationEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(FLEET_EXCHANGE, VEHICLE_LOCATION_ROUTING_KEY, message);
            log.debug("Published vehicle location update event for vehicle ID: {}", event.getVehicleId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing vehicle location event", e);
        }
    }

    /**
     * Publish trip completed event
     */
    public void publishTripCompleted(TripCompletedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(FLEET_EXCHANGE, TRIP_COMPLETED_ROUTING_KEY, message);
            log.info("Published trip completed event for trip ID: {}", event.getTripId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing trip completed event", e);
        }
    }

    /**
     * Publish low battery alert event
     */
    public void publishLowBatteryAlert(Long vehicleId, Long companyId, Double batterySoc) {
        try {
            String severity = batterySoc < 10.0 ? "CRITICAL" : "WARNING";
            LowBatteryEvent event = new LowBatteryEvent(
                    vehicleId,
                    companyId,
                    batterySoc,
                    LocalDateTime.now(),
                    severity
            );
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(FLEET_EXCHANGE, BATTERY_LOW_ROUTING_KEY, message);
            log.warn("Published low battery alert for vehicle ID: {}, SOC: {}%, Severity: {}",
                    vehicleId, batterySoc, severity);
        } catch (JsonProcessingException e) {
            log.error("Error serializing low battery event", e);
        }
    }

    /**
     * Publish custom fleet event
     */
    public void publishCustomEvent(String routingKey, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(FLEET_EXCHANGE, routingKey, message);
            log.debug("Published custom event with routing key: {}", routingKey);
        } catch (JsonProcessingException e) {
            log.error("Error serializing custom event", e);
        }
    }
}
