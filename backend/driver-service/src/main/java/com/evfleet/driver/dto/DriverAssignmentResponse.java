package com.evfleet.driver.dto;

import com.evfleet.driver.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentResponse {

    private String id;
    private String driverId;
    private String vehicleId;
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private AssignmentStatus status;
    private String assignedBy;
    private LocalDateTime createdAt;
}
