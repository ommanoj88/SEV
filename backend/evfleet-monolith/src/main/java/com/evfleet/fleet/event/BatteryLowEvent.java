package com.evfleet.fleet.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

@Getter
public class BatteryLowEvent extends DomainEvent {
    private final Long vehicleId;
    private final String vehicleNumber;
    private final Double batterySoc;
    private final Double latitude;
    private final Double longitude;

    public BatteryLowEvent(Object source, Long vehicleId, String vehicleNumber,
                          Double batterySoc, Double latitude, Double longitude) {
        super(source);
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.batterySoc = batterySoc;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
