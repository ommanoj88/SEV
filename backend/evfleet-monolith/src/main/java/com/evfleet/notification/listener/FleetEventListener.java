package com.evfleet.notification.listener;

import com.evfleet.common.event.EventListenerSupport;
import com.evfleet.fleet.event.BatteryLowEvent;
import com.evfleet.fleet.event.VehicleCreatedEvent;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to Fleet events and creates notifications
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FleetEventListener extends EventListenerSupport {

    private final NotificationRepository notificationRepository;

    @EventListener
    @Async
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        logEventReceived(event);

        try {
            // Create notification for vehicle registration
            Notification notification = Notification.builder()
                .userId(event.getCompanyId()) // In real app, would notify company admin
                .title("New Vehicle Registered")
                .message(String.format("Vehicle %s has been registered successfully",
                    event.getVehicleNumber()))
                .type(Notification.NotificationType.SUCCESS)
                .referenceId("VEHICLE_" + event.getVehicleId())
                .build();

            notificationRepository.save(notification);
            logEventProcessed(event);
        } catch (Exception e) {
            logEventError(event, e);
        }
    }

    @EventListener
    @Async
    public void handleBatteryLow(BatteryLowEvent event) {
        logEventReceived(event);

        try {
            // Create alert notification for low battery
            Notification notification = Notification.builder()
                .userId(1L) // In real app, would notify fleet manager
                .title("Low Battery Alert")
                .message(String.format("Vehicle %s has low battery: %.1f%%. Current location: %.6f, %.6f",
                    event.getVehicleNumber(),
                    event.getBatterySoc(),
                    event.getLatitude(),
                    event.getLongitude()))
                .type(Notification.NotificationType.ALERT)
                .referenceId("VEHICLE_" + event.getVehicleId())
                .build();

            notificationRepository.save(notification);
            logEventProcessed(event);
        } catch (Exception e) {
            logEventError(event, e);
        }
    }
}
