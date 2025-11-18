package com.evfleet.fleet.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class VehicleCreatedEvent extends DomainEvent {
    private final Long vehicleId;
    private final String vehicleNumber;
    private final Long companyId;

    public VehicleCreatedEvent(Object source, Long vehicleId, String vehicleNumber, Long companyId) {
        super(source, null, companyId);
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.companyId = companyId;
    }
}
