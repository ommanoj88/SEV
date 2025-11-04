package com.evfleet.driver.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRegistered {
    private String driverId;
    private String companyId;
    private String name;
    private String email;
    private LocalDateTime timestamp = LocalDateTime.now();

    public DriverRegistered(String driverId, String companyId, String name, String email) {
        this.driverId = driverId;
        this.companyId = companyId;
        this.name = name;
        this.email = email;
        this.timestamp = LocalDateTime.now();
    }
}
