package com.evfleet.charging.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ChargingSessionCompletedEvent extends DomainEvent {
    private final Long sessionId;
    private final Long vehicleId;
    private final BigDecimal energyConsumed;
    private final BigDecimal cost;

    public ChargingSessionCompletedEvent(Object source, Long sessionId, Long vehicleId,
                                        BigDecimal energyConsumed, BigDecimal cost) {
        super(source);
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.energyConsumed = energyConsumed;
        this.cost = cost;
    }
}
