package com.evfleet.billing.listener;

import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.charging.event.ChargingSessionCompletedEvent;
import com.evfleet.common.event.EventListenerSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to Charging events and creates billing records
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component("billingChargingEventListener")
@Slf4j
@RequiredArgsConstructor
public class ChargingEventListener extends EventListenerSupport {

    private final InvoiceRepository invoiceRepository;

    @EventListener
    @Async
    public void handleChargingSessionCompleted(ChargingSessionCompletedEvent event) {
        logEventReceived(event);

        try {
            // In real implementation, would aggregate charges and create monthly invoices
            log.info("Recording charging cost for billing: Session {} - Cost: ₹{}",
                event.getSessionId(),
                event.getCost());

            logEventProcessed(event);
        } catch (Exception e) {
            logEventError(event, e);
        }
    }
}
