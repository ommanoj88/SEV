package com.evfleet.charging.domain.model.aggregate;

import com.evfleet.charging.domain.model.event.*;
import com.evfleet.charging.domain.model.valueobject.Energy;
import com.evfleet.charging.domain.model.valueobject.Price;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Charging Session Aggregate Root
 * Manages charging session lifecycle with event sourcing capabilities
 */
@Getter
public class ChargingSessionAggregate {
    private String sessionId;
    private String vehicleId;
    private String stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Energy energyConsumed;
    private Price cost;
    private String status;
    private List<Object> domainEvents = new ArrayList<>();

    public void startSession(String sessionId, String vehicleId, String stationId) {
        if (this.sessionId != null) {
            throw new IllegalStateException("Session already started");
        }

        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";

        domainEvents.add(new ChargingSessionStarted(sessionId, vehicleId, stationId, startTime));
    }

    public void completeSession(BigDecimal energyKwh, BigDecimal costAmount) {
        if (!"ACTIVE".equals(this.status)) {
            throw new IllegalStateException("Cannot complete inactive session");
        }

        this.endTime = LocalDateTime.now();
        this.energyConsumed = new Energy(energyKwh);
        this.cost = Price.inr(costAmount);
        this.status = "COMPLETED";

        domainEvents.add(new ChargingSessionCompleted(
            sessionId, vehicleId, stationId, startTime, endTime,
            energyKwh, costAmount
        ));
    }

    public void failSession(String reason) {
        this.status = "FAILED";
        this.endTime = LocalDateTime.now();

        domainEvents.add(new ChargingSessionFailed(sessionId, vehicleId, stationId, reason));
    }

    public List<Object> getAndClearDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
}
