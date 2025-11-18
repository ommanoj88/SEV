package com.evfleet.fleet.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class TripStartedEvent extends DomainEvent {
    private final Long tripId;
    private final Long vehicleId;
    private final Long driverId;

    public TripStartedEvent(Object source, Long tripId, Long vehicleId, Long driverId) {
        super(source);
        this.tripId = tripId;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
    }
}
