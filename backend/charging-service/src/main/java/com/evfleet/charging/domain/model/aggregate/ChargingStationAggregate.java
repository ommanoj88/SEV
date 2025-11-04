package com.evfleet.charging.domain.model.aggregate;

import com.evfleet.charging.domain.model.event.StationAvailable;
import com.evfleet.charging.domain.model.event.StationOccupied;
import com.evfleet.charging.domain.model.valueobject.Location;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

/**
 * Charging Station Aggregate Root
 * Manages station availability and slot reservations
 */
@Getter
public class ChargingStationAggregate {
    private String stationId;
    private String name;
    private Location location;
    private Integer availableSlots;
    private Integer totalSlots;
    private String status;
    private List<Object> domainEvents = new ArrayList<>();

    public ChargingStationAggregate(String stationId, String name, Location location,
                                    Integer totalSlots) {
        this.stationId = stationId;
        this.name = name;
        this.location = location;
        this.totalSlots = totalSlots;
        this.availableSlots = totalSlots;
        this.status = "AVAILABLE";
    }

    public boolean reserveSlot(String sessionId) {
        if (availableSlots <= 0) {
            return false;
        }

        availableSlots--;

        if (availableSlots == 0) {
            this.status = "FULL";
        }

        domainEvents.add(new StationOccupied(stationId, sessionId, availableSlots));
        return true;
    }

    public void releaseSlot() {
        if (availableSlots < totalSlots) {
            availableSlots++;
            this.status = "AVAILABLE";
            domainEvents.add(new StationAvailable(stationId, availableSlots));
        }
    }

    public boolean hasAvailableSlots() {
        return availableSlots > 0;
    }

    public List<Object> getAndClearDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}
