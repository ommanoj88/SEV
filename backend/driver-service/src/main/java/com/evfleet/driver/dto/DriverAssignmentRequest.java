package com.evfleet.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentRequest {

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @NotNull(message = "Shift start is required")
    private LocalDateTime shiftStart;

    private LocalDateTime shiftEnd;

    private String assignedBy;
}
