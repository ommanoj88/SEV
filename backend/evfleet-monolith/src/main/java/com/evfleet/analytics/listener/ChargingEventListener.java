package com.evfleet.analytics.listener;

import com.evfleet.analytics.model.FleetSummary;
import com.evfleet.analytics.repository.FleetSummaryRepository;
import com.evfleet.charging.event.ChargingSessionCompletedEvent;
import com.evfleet.common.event.EventListenerSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Listens to Charging events and updates analytics
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component("analyticsChargingEventListener")
@Slf4j
@RequiredArgsConstructor
public class ChargingEventListener extends EventListenerSupport {

    private final FleetSummaryRepository fleetSummaryRepository;

    @EventListener
    @Async
    public void handleChargingSessionCompleted(ChargingSessionCompletedEvent event) {
        logEventReceived(event);

        try {
            // Update analytics with charging data
            // In real implementation, would aggregate by company and date
            log.info("Updating analytics for charging session: {} - Energy: {} kWh, Cost: ₹{}",
                event.getSessionId(),
                event.getEnergyConsumed(),
                event.getCost());

            logEventProcessed(event);
        } catch (Exception e) {
            logEventError(event, e);
        }
    }
}
