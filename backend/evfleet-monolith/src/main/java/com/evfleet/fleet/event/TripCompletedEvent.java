package com.evfleet.fleet.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TripCompletedEvent extends DomainEvent {
    private final Long tripId;
    private final Long vehicleId;
    private final Double distance;
    private final Long duration;
    private final BigDecimal energyConsumed;

    public TripCompletedEvent(Object source, Long tripId, Long vehicleId,
                             Double distance, Long duration, BigDecimal energyConsumed) {
        super(source);
        this.tripId = tripId;
        this.vehicleId = vehicleId;
        this.distance = distance;
        this.duration = duration;
        this.energyConsumed = energyConsumed;
    }
}
