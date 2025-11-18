package com.evfleet.charging.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class ChargingSessionStartedEvent extends DomainEvent {
    private final Long sessionId;
    private final Long vehicleId;
    private final Long stationId;

    public ChargingSessionStartedEvent(Object source, Long sessionId, Long vehicleId, Long stationId) {
        super(source);
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
    }
}
