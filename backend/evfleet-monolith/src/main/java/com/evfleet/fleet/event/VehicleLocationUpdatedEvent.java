package com.evfleet.fleet.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class VehicleLocationUpdatedEvent extends DomainEvent {
    private final Long vehicleId;
    private final Double latitude;
    private final Double longitude;

    public VehicleLocationUpdatedEvent(Object source, Long vehicleId, Double latitude, Double longitude) {
        super(source);
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
