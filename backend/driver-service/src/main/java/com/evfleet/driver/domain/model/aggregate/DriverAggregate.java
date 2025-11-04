package com.evfleet.driver.domain.model.aggregate;

import com.evfleet.driver.domain.model.event.DriverRegistered;
import com.evfleet.driver.domain.model.event.DriverAssigned;
import com.evfleet.driver.domain.model.valueobject.PerformanceScore;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DriverAggregate {
    private String driverId;
    private String companyId;
    private String name;
    private String licenseNumber;
    private String email;
    private PerformanceScore performanceScore;
    private Integer totalTrips;
    private String currentVehicleId;
    private String status;
    private List<Object> domainEvents = new ArrayList<>();

    public void registerDriver(String driverId, String companyId, String name,
                              String licenseNumber, String email) {
        this.driverId = driverId;
        this.companyId = companyId;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.status = "ACTIVE";
        this.totalTrips = 0;
        this.performanceScore = PerformanceScore.initial();

        domainEvents.add(new DriverRegistered(driverId, companyId, name, email));
    }

    public void assignVehicle(String vehicleId) {
        if (this.currentVehicleId != null) {
            throw new IllegalStateException("Driver already assigned to vehicle: " + currentVehicleId);
        }

        this.currentVehicleId = vehicleId;
        domainEvents.add(new DriverAssigned(driverId, vehicleId, LocalDateTime.now()));
    }

    public void incrementTrips() {
        this.totalTrips++;
    }

    public void updatePerformanceScore(int score) {
        this.performanceScore = new PerformanceScore(score);
    }

    public List<Object> getAndClearDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}
