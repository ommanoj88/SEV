package com.evfleet.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverBehaviorResponse {

    private String id;
    private String driverId;
    private String tripId;
    private Integer harshBraking;
    private Integer harshAcceleration;
    private Integer overspeeding;
    private Integer idleTime;
    private Integer score;
    private LocalDateTime timestamp;
}
